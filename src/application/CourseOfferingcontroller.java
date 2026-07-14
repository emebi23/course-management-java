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

public class CourseOfferingcontroller {

    // ── Filter controls ───────────────────────────────────────
    @FXML private ComboBox<String> deptCombo;
    @FXML private ComboBox<String> yearCombo;
    @FXML private TextField        semesterField;
    @FXML private TextField        acadYearField;

    // ── Section list (multi-select) ───────────────────────────
    @FXML private ListView<String> sectionListView;
    @FXML private Label            sectionCountLabel;

    // ── Course list (multi-select) ────────────────────────────
    @FXML private ListView<String> courseListView;
    @FXML private Label            courseCountLabel;

    @FXML private Label            statusLabel;

    // ── Offering table ────────────────────────────────────────
    @FXML private TableView<CourseOffering>            offeringTable;
    @FXML private TableColumn<CourseOffering, Integer> colOfferingId;
    @FXML private TableColumn<CourseOffering, String>  colClassDisplay;
    @FXML private TableColumn<CourseOffering, String>  colCourseId;
    @FXML private TableColumn<CourseOffering, String>  colCourseName;
    @FXML private TableColumn<CourseOffering, String>  colInstructorName;
    @FXML private TableColumn<CourseOffering, String>  colSemester;
    @FXML private TableColumn<CourseOffering, String>  colAcademicYear;
    @FXML private TableColumn<CourseOffering, Integer> colEnrolledCount;

    // lookup maps: display label → ID
    private final Map<String, String> deptMap    = new LinkedHashMap<>();
    private final Map<String, String> sectionMap = new LinkedHashMap<>(); // label → class_id
    private final Map<String, String> courseMap  = new LinkedHashMap<>(); // label → course_id

    private ObservableList<CourseOffering> offeringList = FXCollections.observableArrayList();

    // ── init ──────────────────────────────────────────────────
    @FXML
    public void initialize() {
        colOfferingId.setCellValueFactory(new PropertyValueFactory<>("offeringId"));
        colClassDisplay.setCellValueFactory(new PropertyValueFactory<>("classDisplay"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colInstructorName.setCellValueFactory(new PropertyValueFactory<>("instructorName"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colAcademicYear.setCellValueFactory(new PropertyValueFactory<>("academicYear"));
        colEnrolledCount.setCellValueFactory(new PropertyValueFactory<>("enrolledCount"));

        sectionListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        courseListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        loadDeptCombo();
        loadYearCombo();
        loadCourseList();
        loadOfferings();

        // update section count label when selection changes
        sectionListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> updateSectionCount());
        courseListView.getSelectionModel().selectedItemProperty().addListener(
            (obs, o, n) -> updateCourseCount());
    }

    // ── Load department combo ─────────────────────────────────
    private void loadDeptCombo() {
        deptMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT dept_id, dept_name FROM department ORDER BY dept_name")) {
            while (rs.next()) {
                String key = rs.getString("dept_id") + " – " + rs.getString("dept_name");
                deptMap.put(key, rs.getString("dept_id"));
                items.add(key);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        deptCombo.setItems(items);
    }

    // ── Load year combo (1-6) ─────────────────────────────────
    private void loadYearCombo() {
        ObservableList<String> items = FXCollections.observableArrayList(
            "1","2","3","4","5","6");
        yearCombo.setItems(items);
    }

    // ── Step 1: Find matching sections ───────────────────────
    @FXML
    private void handleFindSections() {
        String deptKey = deptCombo.getValue();
        String yearStr = yearCombo.getValue();

        sectionMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();

        String sql = "SELECT class_id, year_level, section, academic_year " +
                     "FROM class_section WHERE 1=1 ";
        List<Object> params = new ArrayList<>();

        if (deptKey != null) {
            sql += "AND dept_id = ? ";
            params.add(deptMap.get(deptKey));
        }
        if (yearStr != null && !yearStr.isEmpty()) {
            sql += "AND year_level = ? ";
            params.add(Integer.parseInt(yearStr));
        }
        sql += "ORDER BY year_level, section";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof String) ps.setString(i+1, (String) p);
                else ps.setInt(i+1, (Integer) p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id  = rs.getString("class_id");
                String key = id + "  –  Year " + rs.getInt("year_level") +
                             " Sec " + rs.getString("section") +
                             "  (" + rs.getString("academic_year") + ")";
                sectionMap.put(key, id);
                items.add(key);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }

        sectionListView.setItems(items);
        // Select all by default
        sectionListView.getSelectionModel().selectAll();
        updateSectionCount();

        if (items.isEmpty()) {
            sectionCountLabel.setText("No sections found. Check department and year.");
            status("No matching sections found.");
        } else {
            status("Found " + items.size() + " section(s). Deselect any you don't want, then select courses and click Enroll.");
        }
    }

    // ── Load course list ──────────────────────────────────────
    private void loadCourseList() {
        courseMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        String sql =
            "SELECT c.course_id, c.course_name, IFNULL(i.name,'No instructor') AS inst " +
            "FROM course c " +
            "LEFT JOIN instructor i ON c.instructor_id = i.instructor_id " +
            "ORDER BY c.course_name";
        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String id  = rs.getString("course_id");
                String key = id + "  –  " + rs.getString("course_name") +
                             "  (" + rs.getString("inst") + ")";
                courseMap.put(key, id);
                items.add(key);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        courseListView.setItems(items);
    }

    // ── Load offering table ───────────────────────────────────
    private void loadOfferings() {
        offeringList.clear();
        String sql =
            "SELECT co.offering_id, co.class_id, " +
            "       CONCAT('Year ',cs.year_level,' Sec ',cs.section," +
            "              '  (',IFNULL(d.dept_name,'—'),')'," +
            "              '  ',IFNULL(co.academic_year,'')) AS class_display, " +
            "       co.course_id, c.course_name, " +
            "       co.instructor_id, IFNULL(i.name,'—') AS instructor_name, " +
            "       co.semester, co.academic_year, " +
            "       COUNT(DISTINCT e.enrollment_id) AS enrolled_count " +
            "FROM course_offering co " +
            "JOIN  class_section cs ON co.class_id       = cs.class_id " +
            "LEFT JOIN department  d  ON cs.dept_id      = d.dept_id " +
            "LEFT JOIN course      c  ON co.course_id    = c.course_id " +
            "LEFT JOIN instructor  i  ON co.instructor_id= i.instructor_id " +
            "LEFT JOIN students    st ON st.class_id     = co.class_id " +
            "LEFT JOIN enrollment  e  ON e.student_id    = st.student_id " +
            "                       AND e.course_id      = co.course_id " +
            "                       AND e.semester       = co.semester " +
            "GROUP BY co.offering_id, co.class_id, class_display, " +
            "         co.course_id, c.course_name, co.instructor_id, " +
            "         instructor_name, co.semester, co.academic_year " +
            "ORDER BY co.academic_year, co.semester, class_display";

        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next())
                offeringList.add(new CourseOffering(
                    rs.getInt("offering_id"),
                    rs.getString("class_id"),      rs.getString("class_display"),
                    rs.getString("course_id"),     rs.getString("course_name"),
                    rs.getString("instructor_id"), rs.getString("instructor_name"),
                    rs.getString("semester"),      rs.getString("academic_year"),
                    rs.getInt("enrolled_count")));
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        offeringTable.setItems(offeringList);
    }

    // ── Main Enroll action ────────────────────────────────────
    @FXML
    private void handleEnroll() {
        List<String> selectedSectionKeys = new ArrayList<>(
            sectionListView.getSelectionModel().getSelectedItems());
        List<String> selectedCourseKeys  = new ArrayList<>(
            courseListView.getSelectionModel().getSelectedItems());
        String semester = semesterField.getText().trim();
        String acYear   = acadYearField.getText().trim();

        if (selectedSectionKeys.isEmpty()) {
            alert("Error","No sections selected.\nClick 'Find Matching Sections' first."); return; }
        if (selectedCourseKeys.isEmpty()) {
            alert("Error","No courses selected.\nHold Ctrl and click courses in the list."); return; }
        if (semester.isEmpty()) {
            alert("Error","Please enter a semester (e.g. Semester 1)."); return; }

        // Collect section IDs
        List<String> classIds = new ArrayList<>();
        for (String k : selectedSectionKeys) classIds.add(sectionMap.get(k));

        // Collect course IDs
        List<String> courseIds = new ArrayList<>();
        for (String k : selectedCourseKeys) courseIds.add(courseMap.get(k));

        int totalEnrolled = 0, totalSkipped = 0;
        int sectionsProcessed = 0;
        StringBuilder detail = new StringBuilder();

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                for (String classId : classIds) {

                    // Get all students in this section
                    List<String> students = new ArrayList<>();
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT student_id FROM students WHERE class_id = ?")) {
                        ps.setString(1, classId);
                        ResultSet rs = ps.executeQuery();
                        while (rs.next()) students.add(rs.getString("student_id"));
                    }

                    int sectionEnrolled = 0;

                    for (String courseId : courseIds) {
                        // Get instructor for course
                        String instrId = null;
                        try (PreparedStatement ps = conn.prepareStatement(
                                "SELECT instructor_id FROM course WHERE course_id=?")) {
                            ps.setString(1, courseId);
                            ResultSet rs = ps.executeQuery();
                            if (rs.next()) instrId = rs.getString("instructor_id");
                        }

                        // Insert course_offering (skip duplicate silently)
                        try (PreparedStatement ps = conn.prepareStatement(
                                "INSERT IGNORE INTO course_offering " +
                                "(class_id,course_id,instructor_id,semester,academic_year) " +
                                "VALUES (?,?,?,?,?)")) {
                            ps.setString(1, classId);
                            ps.setString(2, courseId);
                            if (instrId != null) ps.setString(3, instrId);
                            else ps.setNull(3, Types.VARCHAR);
                            ps.setString(4, semester);
                            ps.setString(5, acYear);
                            ps.executeUpdate();
                        }

                        // Auto-enroll students
                        if (!students.isEmpty()) {
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "INSERT IGNORE INTO enrollment " +
                                    "(student_id,course_id,semester,academic_year) " +
                                    "VALUES (?,?,?,?)")) {
                                for (String sid : students) {
                                    ps.setString(1, sid);
                                    ps.setString(2, courseId);
                                    ps.setString(3, semester);
                                    ps.setString(4, acYear);
                                    int rows = ps.executeUpdate();
                                    if (rows > 0) { sectionEnrolled++; totalEnrolled++; }
                                    else totalSkipped++;
                                }
                            }
                        }
                    }

                    detail.append("  • ").append(classId)
                          .append(" (").append(students.size()).append(" students)")
                          .append(" → ").append(sectionEnrolled).append(" new enrollments\n");
                    sectionsProcessed++;
                }

                conn.commit();

                String summary =
                    "✔  Done!\n" +
                    "Sections processed : " + sectionsProcessed + "\n" +
                    "Courses assigned   : " + selectedCourseKeys.size() + "\n" +
                    "New enrollments    : " + totalEnrolled + "\n" +
                    (totalSkipped > 0 ? "Already enrolled (skipped): " + totalSkipped + "\n" : "") +
                    "\n" + detail.toString().trim();

                status(summary);
                loadOfferings();

            } catch (SQLException ex) {
                conn.rollback();
                alert("Error", ex.getMessage());
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    // ── Remove offering ───────────────────────────────────────
    @FXML
    private void handleDelete() {
        CourseOffering sel = offeringTable.getSelectionModel().getSelectedItem();
        if (sel == null) { alert("Error","Select an offering row from the table first."); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Remove offering?");
        confirm.setContentText(
            "Remove: " + sel.getCourseName() + "\nFrom: " + sel.getClassDisplay() +
            "\n\nEnrollment records are kept.");
        if (confirm.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) return;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "DELETE FROM course_offering WHERE offering_id=?")) {
            s.setInt(1, sel.getOfferingId());
            s.executeUpdate();
            status("Offering removed.");
            loadOfferings();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // ── Helpers ───────────────────────────────────────────────
    private void updateSectionCount() {
        int n = sectionListView.getSelectionModel().getSelectedItems().size();
        if (sectionCountLabel != null)
            sectionCountLabel.setText(n + " section(s) selected");
    }

    private void updateCourseCount() {
        int n = courseListView.getSelectionModel().getSelectedItems().size();
        if (courseCountLabel != null)
            courseCountLabel.setText(n + " course(s) selected");
    }

    private void status(String msg) {
        if (statusLabel != null) statusLabel.setText(msg);
    }

    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(t); a.setContentText(m); a.showAndWait();
    }
}
