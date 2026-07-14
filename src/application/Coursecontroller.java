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
import java.util.LinkedHashMap;
import java.util.Map;

public class Coursecontroller {

    @FXML private TextField   courseIdField;
    @FXML private TextField   courseNameField;
    @FXML private TextField   creditHourField;
    @FXML private ComboBox<String> instructorCombo;   // "INS001 – Dr. Amde"
    @FXML private TextField   searchField;

    @FXML private TableView<CourseRow>               courseTable;
    @FXML private TableColumn<CourseRow, String>     colCourseId;
    @FXML private TableColumn<CourseRow, String>     colCourseName;
    @FXML private TableColumn<CourseRow, Integer>    colCreditHour;
    @FXML private TableColumn<CourseRow, String>     colInstructorId;
    @FXML private TableColumn<CourseRow, String>     colInstructorName;

    // Map: "INS001 – Dr. Amde"  →  "INS001"
    private final Map<String, String> instructorMap = new LinkedHashMap<>();

    private ObservableList<CourseRow> courseList = FXCollections.observableArrayList();

    // ── inner model that carries instructor name ───────────────
    public static class CourseRow {
        private final String courseId, courseName, instructorId, instructorName;
        private final int    creditHour;

        public CourseRow(String courseId, String courseName,
                         int creditHour, String instructorId, String instructorName) {
            this.courseId       = courseId;
            this.courseName     = courseName;
            this.creditHour     = creditHour;
            this.instructorId   = instructorId != null ? instructorId : "";
            this.instructorName = instructorName != null ? instructorName : "—";
        }
        public String getCourseId()       { return courseId; }
        public String getCourseName()     { return courseName; }
        public int    getCreditHour()     { return creditHour; }
        public String getInstructorId()   { return instructorId; }
        public String getInstructorName() { return instructorName; }
    }

    // ── initialize ────────────────────────────────────────────
    @FXML
    public void initialize() {
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colCreditHour.setCellValueFactory(new PropertyValueFactory<>("creditHour"));
        colInstructorId.setCellValueFactory(new PropertyValueFactory<>("instructorId"));
        colInstructorName.setCellValueFactory(new PropertyValueFactory<>("instructorName"));

        loadInstructorCombo();   // fill the dropdown with ALL instructors
        loadCourses();

        courseTable.setOnMouseClicked(e -> populateFields());
    }

    // ── load ALL instructors into combo ───────────────────────
    private void loadInstructorCombo() {
        instructorMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();

        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT instructor_id, name FROM instructor ORDER BY name")) {
            while (rs.next()) {
                String id   = rs.getString("instructor_id");
                String name = rs.getString("name");
                String key  = id + " – " + name;
                instructorMap.put(key, id);
                items.add(key);
            }
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }

        instructorCombo.setItems(items);
    }

    // ── load courses table with instructor name ───────────────
    private void loadCourses() {
        courseList.clear();
        String sql =
            "SELECT c.course_id, c.course_name, c.credit_hour, " +
            "       c.instructor_id, IFNULL(i.name, '—') AS instructor_name " +
            "FROM course c " +
            "LEFT JOIN instructor i ON c.instructor_id = i.instructor_id " +
            "ORDER BY c.course_id";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                courseList.add(new CourseRow(
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("credit_hour"),
                    rs.getString("instructor_id"),
                    rs.getString("instructor_name")
                ));
            }
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
        courseTable.setItems(courseList);
    }

    // ── click row → fill fields ───────────────────────────────
    private void populateFields() {
        CourseRow c = courseTable.getSelectionModel().getSelectedItem();
        if (c == null) return;
        courseIdField.setText(c.getCourseId());
        courseNameField.setText(c.getCourseName());
        creditHourField.setText(String.valueOf(c.getCreditHour()));

        // Select matching instructor in combo
        for (String key : instructorMap.keySet()) {
            if (instructorMap.get(key).equals(c.getInstructorId())) {
                instructorCombo.setValue(key);
                break;
            }
        }
    }

    // ── Add ───────────────────────────────────────────────────
    @FXML
    private void handleAdd() {
        if (courseIdField.getText().isEmpty() || courseNameField.getText().isEmpty()) {
            showAlert("Error", "Course ID and Name are required.");
            return;
        }
        String instructorId = getSelectedInstructorId();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO course (course_id, course_name, credit_hour, instructor_id) " +
                 "VALUES (?,?,?,?)")) {
            stmt.setString(1, courseIdField.getText().trim());
            stmt.setString(2, courseNameField.getText().trim());
            stmt.setInt(3, parseCreditHour());
            if (instructorId != null) stmt.setString(4, instructorId);
            else                      stmt.setNull(4, java.sql.Types.VARCHAR);
            stmt.executeUpdate();
            showAlert("Success", "Course added.");
            clearFields();
            loadCourses();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    // ── Update ────────────────────────────────────────────────
    @FXML
    private void handleUpdate() {
        if (courseIdField.getText().isEmpty()) {
            showAlert("Error", "Select a course row first.");
            return;
        }
        String instructorId = getSelectedInstructorId();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE course SET course_name=?, credit_hour=?, instructor_id=? " +
                 "WHERE course_id=?")) {
            stmt.setString(1, courseNameField.getText().trim());
            stmt.setInt(2, parseCreditHour());
            if (instructorId != null) stmt.setString(3, instructorId);
            else                      stmt.setNull(3, java.sql.Types.VARCHAR);
            stmt.setString(4, courseIdField.getText().trim());
            stmt.executeUpdate();
            showAlert("Success", "Course updated.");
            clearFields();
            loadCourses();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    // ── Delete ────────────────────────────────────────────────
    @FXML
    private void handleDelete() {
        if (courseIdField.getText().isEmpty()) {
            showAlert("Error", "Select a course row first.");
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "DELETE FROM course WHERE course_id=?")) {
            stmt.setString(1, courseIdField.getText().trim());
            stmt.executeUpdate();
            showAlert("Success", "Course deleted.");
            clearFields();
            loadCourses();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    // ── Search ────────────────────────────────────────────────
    @FXML
    private void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadCourses(); return; }
        ObservableList<CourseRow> filtered = FXCollections.observableArrayList();
        for (CourseRow c : courseList) {
            if (c.getCourseId().toLowerCase().contains(kw)
             || c.getCourseName().toLowerCase().contains(kw)
             || c.getInstructorName().toLowerCase().contains(kw)) {
                filtered.add(c);
            }
        }
        courseTable.setItems(filtered);
    }

    // ── Nav ───────────────────────────────────────────────────
    @FXML
    public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    // ── helpers ───────────────────────────────────────────────
    private String getSelectedInstructorId() {
        String key = instructorCombo.getValue();
        return (key != null) ? instructorMap.get(key) : null;
    }

    private int parseCreditHour() {
        try { return Integer.parseInt(creditHourField.getText().trim()); }
        catch (NumberFormatException e) { return 0; }
    }

    private void clearFields() {
        courseIdField.clear();
        courseNameField.clear();
        creditHourField.clear();
        instructorCombo.setValue(null);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title);
        a.setContentText(msg);
        a.showAndWait();
    }
}
