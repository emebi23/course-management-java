package application;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class InstructorHomecontroller {

    @FXML private Label welcomeLabel;
    @FXML private Label instructorInfoLabel;
    @FXML private Label selectionLabel;

    // Courses table
    @FXML private TableView<CourseRow>            courseTable;
    @FXML private TableColumn<CourseRow, String>  colCourseId;
    @FXML private TableColumn<CourseRow, String>  colCourseName;
    @FXML private TableColumn<CourseRow, Integer> colCreditHour;
    @FXML private TableColumn<CourseRow, Integer> colEnrolledCount;

    // Students TreeView
    @FXML private TreeView<String> studentTreeView;

    private String instructorId;

    // Maps built while constructing tree: section label → class_id
    private final Map<String, String> sectionClassIdMap = new LinkedHashMap<>();

    // ── Course row model ──────────────────────────────────────
    public static class CourseRow {
        private final String courseId, courseName;
        private final int    creditHour, enrolledCount;
        public CourseRow(String i, String n, int c, int e) {
            courseId=i; courseName=n; creditHour=c; enrolledCount=e; }
        public String getCourseId()      { return courseId; }
        public String getCourseName()    { return courseName; }
        public int    getCreditHour()    { return creditHour; }
        public int    getEnrolledCount() { return enrolledCount; }
    }

    @FXML
    public void initialize() {
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colCreditHour.setCellValueFactory(new PropertyValueFactory<>("creditHour"));
        colEnrolledCount.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));

        // Clear any previous class filter
        SessionManager.clearSelectedClass();

        instructorId = getInstructorId(SessionManager.getUserId());

        if (instructorId != null) {
            welcomeLabel.setText("Welcome, " + SessionManager.getUsername() + "!");
            loadInstructorInfo();
            loadMyCourses();
            loadStudentTree();
        } else {
            welcomeLabel.setText("Welcome, " + SessionManager.getUsername() + "!");
            if (instructorInfoLabel != null)
                instructorInfoLabel.setText(
                    "No instructor profile linked. Contact Admin.");
        }

        // Listen for tree selection
        studentTreeView.getSelectionModel().selectedItemProperty()
            .addListener((obs, oldVal, newVal) -> handleTreeSelection(newVal));
    }

    private String getInstructorId(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement s = conn.prepareStatement(
                 "SELECT instructor_id FROM instructor WHERE user_id=?")) {
            s.setInt(1, userId);
            ResultSet rs = s.executeQuery();
            if (rs.next()) return rs.getString("instructor_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void loadInstructorInfo() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement s = conn.prepareStatement(
                 "SELECT name, email, specialization FROM instructor WHERE instructor_id=?")) {
            s.setString(1, instructorId);
            ResultSet rs = s.executeQuery();
            if (rs.next() && instructorInfoLabel != null)
                instructorInfoLabel.setText(
                    "ID: " + instructorId + "  |  " + rs.getString("name") +
                    "  |  " + rs.getString("specialization") +
                    "  |  " + rs.getString("email"));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadMyCourses() {
        ObservableList<CourseRow> list = FXCollections.observableArrayList();
        String sql =
            "SELECT c.course_id, c.course_name, c.credit_hour, " +
            "       COUNT(e.enrollment_id) AS enrolled_count " +
            "FROM course c " +
            "LEFT JOIN enrollment e ON c.course_id = e.course_id " +
            "WHERE c.instructor_id = ? " +
            "GROUP BY c.course_id, c.course_name, c.credit_hour";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, instructorId);
            ResultSet rs = s.executeQuery();
            while (rs.next())
                list.add(new CourseRow(
                    rs.getString("course_id"), rs.getString("course_name"),
                    rs.getInt("credit_hour"),  rs.getInt("enrolled_count")));
        } catch (SQLException e) { e.printStackTrace(); }
        courseTable.setItems(list);
    }

    // ── Build TreeView: Dept → Section → Students ─────────────
    private void loadStudentTree() {
        sectionClassIdMap.clear();

        TreeItem<String> root = new TreeItem<>("root");
        root.setExpanded(true);

        String sql =
            "SELECT DISTINCT " +
            "       IFNULL(d.dept_name,'No Department') AS dept_name, " +
            "       IFNULL(cs.class_id,'')              AS class_id, " +
            "       IFNULL(cs.section,'?')              AS section, " +
            "       IFNULL(cs.year_level,0)             AS year_level, " +
            "       s.student_id, s.name AS student_name, " +
            "       c.course_name, " +
            "       IFNULL(g.grade,'Not graded yet')    AS grade " +
            "FROM enrollment e " +
            "JOIN students      s  ON e.student_id     = s.student_id " +
            "JOIN course        c  ON e.course_id      = c.course_id " +
            "LEFT JOIN class_section cs ON s.class_id  = cs.class_id " +
            "LEFT JOIN department    d  ON cs.dept_id   = d.dept_id " +
            "LEFT JOIN grade         g  ON g.enrollment_id = e.enrollment_id " +
            "WHERE c.instructor_id = ? " +
            "ORDER BY dept_name, year_level, section, student_name, c.course_name";

        // dept → sectionLabel → list of student lines
        // Also store sectionLabel → class_id
        Map<String, Map<String, List<String>>> tree = new LinkedHashMap<>();
        Map<String, String> sectionToClassId = new LinkedHashMap<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement s = conn.prepareStatement(sql)) {
            s.setString(1, instructorId);
            ResultSet rs = s.executeQuery();
            while (rs.next()) {
                String dept     = rs.getString("dept_name");
                String classId  = rs.getString("class_id");
                String secLabel = "Year " + rs.getInt("year_level") +
                                  "  –  Section " + rs.getString("section");
                String student  = rs.getString("student_id") + "   " +
                                  rs.getString("student_name") +
                                  "   [" + rs.getString("course_name") + "]" +
                                  "   Grade: " + rs.getString("grade");

                // dept key includes section for uniqueness across depts
                String deptSecKey = dept + "||" + secLabel;
                sectionToClassId.put(deptSecKey, classId);

                tree.computeIfAbsent(dept, k -> new LinkedHashMap<>())
                    .computeIfAbsent(secLabel, k -> new ArrayList<>())
                    .add(student);
            }
        } catch (SQLException e) { e.printStackTrace(); }

        for (Map.Entry<String, Map<String, List<String>>> deptEntry : tree.entrySet()) {
            String deptName = deptEntry.getKey();

            TreeItem<String> deptNode = new TreeItem<>("📚  " + deptName);
            deptNode.setExpanded(true);

            for (Map.Entry<String, List<String>> secEntry : deptEntry.getValue().entrySet()) {
                String secLabel = secEntry.getKey();
                String deptSecKey = deptName + "||" + secLabel;
                String classId  = sectionToClassId.getOrDefault(deptSecKey, "");

                // Store in map for lookup when clicked
                String treeLabel = "👥  " + secLabel +
                                   "  (" + secEntry.getValue().size() + " students)";
                sectionClassIdMap.put(treeLabel, classId);

                TreeItem<String> secNode = new TreeItem<>(treeLabel);
                secNode.setExpanded(true);

                for (String studentLine : secEntry.getValue())
                    secNode.getChildren().add(new TreeItem<>("      👤  " + studentLine));

                deptNode.getChildren().add(secNode);
            }
            root.getChildren().add(deptNode);
        }

        if (root.getChildren().isEmpty())
            root.getChildren().add(new TreeItem<>("No enrolled students found."));

        studentTreeView.setRoot(root);
        studentTreeView.setShowRoot(false);

        // Cell factory for styling
        studentTreeView.setCellFactory(tv -> new TreeCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                TreeItem<String> ti = getTreeItem();
                if (ti == null) { setStyle(""); return; }

                boolean isDept    = ti.getParent() != null &&
                                    ti.getParent() == studentTreeView.getRoot();
                boolean isSection = !isDept && !ti.getChildren().isEmpty();

                if (isDept) {
                    setStyle("-fx-background-color: #1a5276; -fx-text-fill: white;" +
                             "-fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 6 8;");
                } else if (isSection) {
                    setStyle("-fx-background-color: #d6eaf8; -fx-text-fill: #1a5276;" +
                             "-fx-font-weight: bold; -fx-font-size: 12px; -fx-padding: 4 8;" +
                             "-fx-border-color: #1a5276; -fx-border-width: 0 0 1 3;");
                } else {
                    setStyle("-fx-background-color: white; -fx-text-fill: #333;" +
                             "-fx-font-size: 12px; -fx-padding: 3 8;");
                }
            }
        });
    }

    // ── Handle tree click ─────────────────────────────────────
    private void handleTreeSelection(TreeItem<String> selected) {
        if (selected == null) return;
        String label = selected.getValue();

        if (sectionClassIdMap.containsKey(label)) {
            // Section node clicked
            String classId = sectionClassIdMap.get(label);
            SessionManager.setSelectedClass(classId, label.replace("👥  ","").trim());
            if (selectionLabel != null)
                selectionLabel.setText(
                    "✔ Selected: " + label.replace("👥  ","") +
                    "  →  Click 'Manage / Set Grades' to grade only these students");
        } else {
            // Dept or student node clicked — clear filter
            SessionManager.clearSelectedClass();
            if (selectionLabel != null)
                selectionLabel.setText(
                    "👆  Click a Section row (blue) to filter, then click Manage / Set Grades");
        }
    }

    // ── Navigation ────────────────────────────────────────────
    @FXML
    public void goGrades(ActionEvent event) throws IOException {
        if (SessionManager.getSelectedClassId() == null) {
            Alert a = new Alert(Alert.AlertType.WARNING);
            a.setHeaderText("No section selected");
            a.setContentText("Click a section row (blue) in the tree first,\n" +
                             "then click Manage / Set Grades.\n\n" +
                             "Or use 'Show All My Students' to see everyone.");
            a.showAndWait();
            return;
        }
        navigate("Grade.fxml", event);
    }

    @FXML
    public void goGradesAll(ActionEvent event) throws IOException {
        SessionManager.clearSelectedClass();
        navigate("Grade.fxml", event);
    }

    @FXML
    public void goLogout(ActionEvent event) throws IOException {
        SessionManager.clear();
        navigate("Sample.fxml", event);
    }

    private void navigate(String fxml, ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
