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

public class Gradecontroller {

    @FXML private TableView<Grade>           gradeTable;
    @FXML private TableColumn<Grade, String> colStudentId;
    @FXML private TableColumn<Grade, String> colStudentName;
    @FXML private TableColumn<Grade, String> colCourseId;
    @FXML private TableColumn<Grade, String> colCourseName;
    @FXML private TableColumn<Grade, String> colGrade;
    @FXML private TableColumn<Grade, String> colGradePoint;

    @FXML private TextField gradeField;
    @FXML private Label     instructorLabel;
    @FXML private Label     statusLabel;

    private ObservableList<Grade> gradeList = FXCollections.observableArrayList();
    private String instructorId;

    @FXML
    public void initialize() {
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colStudentName.setCellValueFactory(new PropertyValueFactory<>("studentName"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colGradePoint.setCellValueFactory(new PropertyValueFactory<>("gradePoint"));

        instructorId = getInstructorIdForUser(SessionManager.getUserId());

        if (instructorLabel != null)
            instructorLabel.setText("Instructor: " + SessionManager.getUsername());

        loadGrades();

        gradeTable.setOnMouseClicked(e -> {
            Grade selected = gradeTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String g = selected.getGrade();
                gradeField.setText(g.equals("Not graded yet") ? "" : g);
                if (statusLabel != null)
                    statusLabel.setText("Selected: " + selected.getStudentName()
                        + " — " + selected.getCourseName());
            }
        });
    }

    private String getInstructorIdForUser(int userId) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT instructor_id FROM instructor WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getString("instructor_id");
        } catch (SQLException e) { e.printStackTrace(); }
        return null;
    }

    private void loadGrades() {
        gradeList.clear();

        // Check if a specific class was selected from the tree
        String classId   = SessionManager.getSelectedClassId();
        String className = SessionManager.getSelectedClassName();

        // Update header to show what is being displayed
        if (instructorLabel != null) {
            instructorLabel.setText("Instructor: " + SessionManager.getUsername() +
                (classId != null && !classId.isEmpty()
                    ? "   |   Section: " + className
                    : "   |   All Students"));
        }

        StringBuilder sql = new StringBuilder(
            "SELECT e.enrollment_id, e.student_id, s.name AS student_name, " +
            "       e.course_id, c.course_name, c.credit_hour, " +
            "       IFNULL(g.grade_id, 0)             AS grade_id, " +
            "       IFNULL(g.grade, 'Not graded yet')  AS grade " +
            "FROM enrollment e " +
            "JOIN students s ON e.student_id = s.student_id " +
            "JOIN course   c ON e.course_id  = c.course_id " +
            "LEFT JOIN grade g ON g.enrollment_id = e.enrollment_id " +
            "WHERE c.instructor_id = ? ");

        // If a class was selected, filter to only those students
        if (classId != null && !classId.isEmpty())
            sql.append("AND s.class_id = ? ");

        sql.append("ORDER BY s.name, c.course_id");

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql.toString())) {
            stmt.setString(1, instructorId);
            if (classId != null && !classId.isEmpty())
                stmt.setString(2, classId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String grade = rs.getString("grade");
                double gp    = getGradePoint(grade);
                String gpStr = grade.equals("Not graded yet") ? "—"
                               : String.format("%.1f", gp);
                gradeList.add(new Grade(
                    rs.getInt("grade_id"),
                    rs.getInt("enrollment_id"),
                    rs.getString("student_id"),
                    rs.getString("student_name"),
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    grade, gpStr, "—"
                ));
            }
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
        gradeTable.setItems(gradeList);
    }

    @FXML
    private void handleSaveGrade() {
        Grade selected = gradeTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Error","Click a student row first."); return; }

        String newGrade = gradeField.getText().trim().toUpperCase();
        if (newGrade.isEmpty()) { showAlert("Error","Enter a grade (e.g. A, B+, A-)."); return; }
        if (!isValidGrade(newGrade)) {
            showAlert("Error","Invalid grade.\nAllowed: A+  A  A-  B+  B  B-  C+  C  C-  D  F");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            if (selected.getGradeId() > 0) {
                PreparedStatement stmt = conn.prepareStatement(
                    "UPDATE grade SET grade=? WHERE grade_id=?");
                stmt.setString(1, newGrade);
                stmt.setInt(2, selected.getGradeId());
                stmt.executeUpdate();
            } else {
                PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO grade (enrollment_id, grade) VALUES (?,?)");
                stmt.setInt(1, selected.getEnrollmentId());
                stmt.setString(2, newGrade);
                stmt.executeUpdate();
            }

            gradeField.clear();
            loadGrades();

            if (statusLabel != null)
                statusLabel.setText("✔ Saved " + newGrade + " for " +
                    selected.getStudentName());

            showAlert("Grade Saved",
                "Student : " + selected.getStudentName() +
                "\nCourse  : " + selected.getCourseName() +
                "\nGrade   : " + newGrade +
                "\nGrade Pt: " + String.format("%.1f", getGradePoint(newGrade)));

        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    private boolean isValidGrade(String g) {
        switch (g) {
            case "A+": case "A": case "A-":
            case "B+": case "B": case "B-":
            case "C+": case "C": case "C-":
            case "D":  case "F": return true;
            default: return false;
        }
    }

    private double getGradePoint(String grade) {
        if (grade == null) return 0.0;
        switch (grade.trim()) {
            case "A+": case "A": return 4.0;
            case "A-": return 3.7; case "B+": return 3.5;
            case "B":  return 3.0; case "B-": return 2.7;
            case "C+": return 2.5; case "C":  return 2.0;
            case "C-": return 1.7; case "D":  return 1.0;
            case "F":  return 0.0; default:   return 0.0;
        }
    }

    @FXML
    public void goBack(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("InstructorHome.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title); a.setContentText(msg); a.showAndWait();
    }
}
