package application;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
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
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.*;
import java.util.*;

public class Enrollmentcontroller {

    // ── Left controls ─────────────────────────────────────────
    @FXML private ComboBox<String>  courseCombo;
    @FXML private TextField         semesterField;
    @FXML private TextField         academicYearField;
    @FXML private ComboBox<String>  filterDeptCombo;
    @FXML private ComboBox<String>  filterYearCombo;
    @FXML private TextField         searchField;
    @FXML private ListView<StudentItem> studentListView;
    @FXML private Label             selectedCountLabel;
    @FXML private Label             statusLabel;

    // ── Right controls ────────────────────────────────────────
    @FXML private TextField         tableSearchField;
    @FXML private TableView<Enrollment>            enrollmentTable;
    @FXML private TableColumn<Enrollment, Integer> colEnrollId;
    @FXML private TableColumn<Enrollment, String>  colStudentId;
    @FXML private TableColumn<Enrollment, String>  colStudentName;
    @FXML private TableColumn<Enrollment, String>  colCourseId;
    @FXML private TableColumn<Enrollment, String>  colCourseName;
    @FXML private TableColumn<Enrollment, String>  colSemester;
    @FXML private TableColumn<Enrollment, String>  colAcademicYear;

    // ── Data ──────────────────────────────────────────────────
    private final Map<String, String>      courseMap   = new LinkedHashMap<>();
    private final Map<String, String>      deptMap     = new LinkedHashMap<>();
    private ObservableList<StudentItem>    studentItems= FXCollections.observableArrayList();
    private ObservableList<Enrollment>     enrollmentList = FXCollections.observableArrayList();

    // ── Inner model: one row in the checkbox list ─────────────
    public static class StudentItem {
        private final BooleanProperty checked = new SimpleBooleanProperty(false);
        private final String studentId;
        private final String displayLabel;

        public StudentItem(String studentId, String displayLabel) {
            this.studentId    = studentId;
            this.displayLabel = displayLabel;
        }
        public BooleanProperty checkedProperty() { return checked; }
        public boolean  isChecked()   { return checked.get(); }
        public void     setChecked(boolean v) { checked.set(v); }
        public String   getStudentId()    { return studentId; }
        public String   getDisplayLabel() { return displayLabel; }
        @Override public String toString() { return displayLabel; }
    }

    // ── init ──────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Table columns
        colEnrollId.setCellValueFactory(new PropertyValueFactory<>("enrollmentId"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colAcademicYear.setCellValueFactory(new PropertyValueFactory<>("academicYear"));

        // Checkbox cell factory
        studentListView.setCellFactory(lv -> new ListCell<StudentItem>() {
            private final CheckBox cb = new CheckBox();
            private StudentItem current;
            {
                cb.setOnAction(e -> {
                    if (current != null) {
                        current.setChecked(cb.isSelected());
                        updateCount();
                    }
                });
            }
            @Override
            protected void updateItem(StudentItem item, boolean empty) {
                super.updateItem(item, empty);
                current = null;
                if (empty || item == null) {
                    setGraphic(null); setText(null);
                } else {
                    current = item;
                    cb.setSelected(item.isChecked());
                    cb.setText(item.getDisplayLabel());
                    setGraphic(cb); setText(null);
                }
            }
        });

        loadCourseCombo();
        loadDeptFilterCombo();
        loadYearFilterCombo();
        loadStudentList(null, null, null);
        loadEnrollments();

        enrollmentTable.setOnMouseClicked(e -> {
            Enrollment sel = enrollmentTable.getSelectionModel().getSelectedItem();
            if (sel == null) return;
            semesterField.setText(sel.getSemester());
            academicYearField.setText(sel.getAcademicYear());
            for (String k : courseMap.keySet())
                if (courseMap.get(k).equals(sel.getCourseId())) {
                    courseCombo.setValue(k); break; }
        });
    }

    // ── Load combos ───────────────────────────────────────────
    private void loadCourseCombo() {
        courseMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT c.course_id, c.course_name, IFNULL(i.name,'No instructor') AS inst " +
                 "FROM course c LEFT JOIN instructor i ON c.instructor_id=i.instructor_id " +
                 "ORDER BY c.course_name")) {
            while (rs.next()) {
                String key = rs.getString("course_id") + " – " +
                             rs.getString("course_name") + "  (" + rs.getString("inst") + ")";
                courseMap.put(key, rs.getString("course_id"));
                items.add(key);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        courseCombo.setItems(items);
    }

    private void loadDeptFilterCombo() {
        deptMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        items.add("All Departments");
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
        filterDeptCombo.setItems(items);
        filterDeptCombo.setValue("All Departments");
    }

    private void loadYearFilterCombo() {
        filterYearCombo.setItems(FXCollections.observableArrayList(
            "Any Year","1","2","3","4","5","6"));
        filterYearCombo.setValue("Any Year");
    }

    // ── Load students with filters ────────────────────────────
    private void loadStudentList(String deptId, String yearLevel, String nameKw) {
        studentItems.clear();

        StringBuilder sql = new StringBuilder(
            "SELECT s.student_id, s.name, " +
            "       IFNULL(cs.year_level,'?') AS yr, " +
            "       IFNULL(cs.section,'') AS sec, " +
            "       IFNULL(d.dept_name,'—') AS dept_name " +
            "FROM students s " +
            "LEFT JOIN class_section cs ON s.class_id = cs.class_id " +
            "LEFT JOIN department    d  ON cs.dept_id  = d.dept_id " +
            "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (deptId != null) {
            sql.append("AND cs.dept_id = ? "); params.add(deptId); }
        if (yearLevel != null) {
            sql.append("AND cs.year_level = ? "); params.add(Integer.parseInt(yearLevel)); }
        if (nameKw != null && !nameKw.isEmpty()) {
            sql.append("AND (s.name LIKE ? OR s.student_id LIKE ?) ");
            params.add("%" + nameKw + "%");
            params.add("%" + nameKw + "%");
        }
        sql.append("ORDER BY s.name");

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                Object p = params.get(i);
                if (p instanceof Integer) ps.setInt(i+1, (Integer) p);
                else ps.setString(i+1, (String) p);
            }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id  = rs.getString("student_id");
                String lbl = id + " – " + rs.getString("name") +
                             "  [Year " + rs.getString("yr") + " " +
                             rs.getString("sec") + " | " +
                             rs.getString("dept_name") + "]";
                studentItems.add(new StudentItem(id, lbl));
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }

        studentListView.setItems(studentItems);
        updateCount();
        status(studentItems.size() + " student(s) found — tick the ones you want to enroll.");
    }

    // ── Filter button ─────────────────────────────────────────
    @FXML
    private void handleSearch() {
        String deptKey  = filterDeptCombo.getValue();
        String yearVal  = filterYearCombo.getValue();
        String nameKw   = searchField.getText().trim();
        String deptId   = (deptKey == null || deptKey.equals("All Departments"))
                          ? null : deptMap.get(deptKey);
        String year     = (yearVal == null || yearVal.equals("Any Year")) ? null : yearVal;
        loadStudentList(deptId, year, nameKw.isEmpty() ? null : nameKw);
    }

    // ── Select All / Clear ────────────────────────────────────
    @FXML
    private void handleSelectAll() {
        for (StudentItem item : studentItems) item.setChecked(true);
        // refresh list to redraw checkboxes
        studentListView.setItems(null);
        studentListView.setItems(studentItems);
        updateCount();
    }

    @FXML
    private void handleClearSelection() {
        for (StudentItem item : studentItems) item.setChecked(false);
        studentListView.setItems(null);
        studentListView.setItems(studentItems);
        updateCount();
    }

    // ── Filter enrollment table ───────────────────────────────
    @FXML
    private void handleTableSearch() {
        String kw = tableSearchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { enrollmentTable.setItems(enrollmentList); return; }
        ObservableList<Enrollment> filtered = FXCollections.observableArrayList();
        for (Enrollment e : enrollmentList)
            if (e.getStudentId().toLowerCase().contains(kw)
             || e.getStudentName().toLowerCase().contains(kw)
             || e.getCourseId().toLowerCase().contains(kw)
             || e.getCourseName().toLowerCase().contains(kw))
                filtered.add(e);
        enrollmentTable.setItems(filtered);
    }

    // ── Enroll checked students ───────────────────────────────
    @FXML
    private void handleEnroll() {
        String courseKey = courseCombo.getValue();
        String semester  = semesterField.getText().trim();
        String acYear    = academicYearField.getText().trim();

        if (courseKey == null) { alert("Error","Select a course."); return; }
        if (semester.isEmpty()) { alert("Error","Enter a semester."); return; }

        // Collect checked students
        List<StudentItem> checked = new ArrayList<>();
        for (StudentItem item : studentItems)
            if (item.isChecked()) checked.add(item);

        if (checked.isEmpty()) {
            alert("Error","No students selected.\n" +
                "Tick the checkboxes next to the students you want to enroll."); return; }

        String courseId = courseMap.get(courseKey);
        int enrolled = 0, skipped = 0;

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT IGNORE INTO enrollment " +
                    "(student_id,course_id,semester,academic_year) VALUES (?,?,?,?)")) {
                for (StudentItem item : checked) {
                    ps.setString(1, item.getStudentId());
                    ps.setString(2, courseId);
                    ps.setString(3, semester);
                    ps.setString(4, acYear);
                    int rows = ps.executeUpdate();
                    if (rows > 0) enrolled++; else skipped++;
                }
                conn.commit();
            } catch (SQLException ex) {
                conn.rollback();
                alert("Error", ex.getMessage()); return;
            } finally { conn.setAutoCommit(true); }
        } catch (SQLException e) { alert("Error", e.getMessage()); return; }

        String cname = courseKey.contains("–")
            ? courseKey.split("–")[1].trim().split("\\(")[0].trim() : courseKey;
        String msg = "✔  Enrolled " + enrolled + " student(s) into " + cname;
        if (skipped > 0) msg += "\n(" + skipped + " already enrolled — skipped)";
        status(msg);

        // Uncheck all after enroll
        handleClearSelection();
        loadEnrollments();
    }

    // ── Remove enrollment ─────────────────────────────────────
    @FXML
    private void handleDelete() {
        Enrollment sel = enrollmentTable.getSelectionModel().getSelectedItem();
        if (sel == null) { alert("Error","Select a row in the table to remove."); return; }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "DELETE FROM enrollment WHERE enrollment_id=?")) {
            s.setInt(1, sel.getEnrollmentId());
            s.executeUpdate();
            status("Enrollment removed."); loadEnrollments();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    // ── Load enrollment table ─────────────────────────────────
    private void loadEnrollments() {
        enrollmentList.clear();
        String sql =
            "SELECT e.enrollment_id, e.student_id, s.name AS student_name, " +
            "       e.course_id, c.course_name, e.semester, e.academic_year " +
            "FROM enrollment e " +
            "JOIN students s ON e.student_id = s.student_id " +
            "JOIN course   c ON e.course_id  = c.course_id " +
            "ORDER BY s.name, c.course_name";
        try (Connection c = DBConnection.getConnection();
             Statement s  = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next())
                enrollmentList.add(new Enrollment(
                    rs.getInt("enrollment_id"),
                    rs.getString("student_id"),  rs.getString("student_name"),
                    rs.getString("course_id"),   rs.getString("course_name"),
                    rs.getString("semester"),    rs.getString("academic_year")));
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        enrollmentTable.setItems(enrollmentList);
    }

    @FXML public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void updateCount() {
        long n = studentItems.stream().filter(StudentItem::isChecked).count();
        if (selectedCountLabel != null)
            selectedCountLabel.setText(n + " selected");
    }
    private void status(String msg) { if (statusLabel != null) statusLabel.setText(msg); }
    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(t); a.setContentText(m); a.showAndWait(); }
}
