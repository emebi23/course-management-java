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

public class StudentDashboardcontroller {

    // Profile labels
    @FXML private Label welcomeLabel;
    @FXML private Label studentIdLabel;
    @FXML private Label nameLabel;
    @FXML private Label emailLabel;
    @FXML private Label departmentLabel;

    // Grade table
    @FXML private TableView<GradeRow>           gradeTable;
    @FXML private TableColumn<GradeRow, String> colSemester;
    @FXML private TableColumn<GradeRow, String> colCourseId;
    @FXML private TableColumn<GradeRow, String> colCourseName;
    @FXML private TableColumn<GradeRow, Integer> colCredit;
    @FXML private TableColumn<GradeRow, String> colGrade;
    @FXML private TableColumn<GradeRow, String> colGradePoint;
    @FXML private TableColumn<GradeRow, String> colCgpa;   // ← new column

    // GPA summary (right panel)
    @FXML private Label gpaLabel;
    @FXML private Label totalCreditsLabel;
    @FXML private Label cgpaLabel;

    private String studentId;

    // ── inner model ───────────────────────────────────────────
    public static class GradeRow {
        private final String semester, courseId, courseName, grade, gradePoint, cgpa;
        private final int credit;

        public GradeRow(String semester, String courseId, String courseName,
                        int credit, String grade, double gradePoint, String cgpa) {
            this.semester   = semester;
            this.courseId   = courseId;
            this.courseName = courseName;
            this.credit     = credit;
            this.grade      = grade;
            this.gradePoint = grade.equals("Not graded yet") ? "—"
                              : String.format("%.1f", gradePoint);
            this.cgpa       = cgpa;
        }
        public String  getSemester()   { return semester; }
        public String  getCourseId()   { return courseId; }
        public String  getCourseName() { return courseName; }
        public int     getCredit()     { return credit; }
        public String  getGrade()      { return grade; }
        public String  getGradePoint() { return gradePoint; }
        public String  getCgpa()       { return cgpa; }
    }

    @FXML
    public void initialize() {
        colSemester.setCellValueFactory(new PropertyValueFactory<>("semester"));
        colCourseId.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colCredit.setCellValueFactory(new PropertyValueFactory<>("credit"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colGradePoint.setCellValueFactory(new PropertyValueFactory<>("gradePoint"));
        colCgpa.setCellValueFactory(new PropertyValueFactory<>("cgpa"));

        // Highlight CGPA column cells
        colCgpa.setCellFactory(col -> new TableCell<GradeRow, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(val);
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #8e44ad; -fx-alignment: CENTER;");
                }
            }
        });

        loadStudentProfile();
        loadMyGrades();
    }

    private void loadStudentProfile() {
        int userId = SessionManager.getUserId();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM students WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                studentId = rs.getString("student_id");
                welcomeLabel.setText("Welcome, " + rs.getString("name") + "!");
                studentIdLabel.setText(studentId);
                nameLabel.setText(rs.getString("name"));
                emailLabel.setText(rs.getString("email") != null ? rs.getString("email") : "—");
                departmentLabel.setText(rs.getString("department") != null ? rs.getString("department") : "—");
            } else {
                welcomeLabel.setText("Welcome, " + SessionManager.getUsername() + "!");
                studentIdLabel.setText("—");
                nameLabel.setText(SessionManager.getUsername());
                emailLabel.setText("—");
                departmentLabel.setText("—");
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadMyGrades() {
        if (studentId == null) return;

        // ── Step 1: fetch all rows ────────────────────────────
        String sql =
            "SELECT e.semester, e.course_id, c.course_name, c.credit_hour, " +
            "       IFNULL(g.grade, 'Not graded yet') AS grade " +
            "FROM enrollment e " +
            "JOIN course c ON e.course_id = c.course_id " +
            "LEFT JOIN grade g ON g.enrollment_id = e.enrollment_id " +
            "WHERE e.student_id = ? " +
            "ORDER BY e.semester, e.course_id";

        // Collect raw data first, then compute running CGPA
        record RawRow(String sem, String cid, String cname, int credit, String grade) {}
        List<RawRow> raw = new ArrayList<>();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                raw.add(new RawRow(
                    rs.getString("semester") != null ? rs.getString("semester") : "—",
                    rs.getString("course_id"),
                    rs.getString("course_name"),
                    rs.getInt("credit_hour"),
                    rs.getString("grade")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }

        // ── Step 2: compute cumulative CGPA row by row ────────
        ObservableList<GradeRow> rows = FXCollections.observableArrayList();
        Map<String, Double>  semPoints  = new LinkedHashMap<>();
        Map<String, Integer> semCredits = new LinkedHashMap<>();
        double runningPoints  = 0;
        int    runningCredits = 0;

        for (RawRow r : raw) {
            double gp = getGradePoint(r.grade());

            String cgpaStr;
            if (!r.grade().equals("Not graded yet")) {
                runningPoints  += gp * r.credit();
                runningCredits += r.credit();
                cgpaStr = String.format("%.2f", runningPoints / runningCredits);

                // also track per-semester for side panel
                semPoints.merge(r.sem(),  gp * r.credit(), Double::sum);
                semCredits.merge(r.sem(), r.credit(),      Integer::sum);
            } else {
                cgpaStr = "—";
            }

            rows.add(new GradeRow(r.sem(), r.cid(), r.cname(),
                                  r.credit(), r.grade(), gp, cgpaStr));
        }

        gradeTable.setItems(rows);

        // ── Step 3: right-panel GPA summary ───────────────────
        StringBuilder sb = new StringBuilder();
        for (String sem : semPoints.keySet()) {
            double semGpa = semPoints.get(sem) / semCredits.get(sem);
            sb.append(sem).append("  →  GPA: ")
              .append(String.format("%.2f", semGpa))
              .append("   (Credits: ").append(semCredits.get(sem)).append(")\n");
        }
        gpaLabel.setText(sb.length() > 0 ? sb.toString().trim() : "No graded courses yet");

        if (runningCredits > 0) {
            cgpaLabel.setText(String.format("%.2f", runningPoints / runningCredits));
            totalCreditsLabel.setText(String.valueOf(runningCredits));
        } else {
            cgpaLabel.setText("—");
            totalCreditsLabel.setText("0");
        }
    }

    private double getGradePoint(String grade) {
        if (grade == null) return 0.0;
        switch (grade.trim()) {
            case "A+": case "A": return 4.0;
            case "A-": return 3.7;
            case "B+": return 3.5;
            case "B":  return 3.0;
            case "B-": return 2.7;
            case "C+": return 2.5;
            case "C":  return 2.0;
            case "C-": return 1.7;
            case "D":  return 1.0;
            case "F":  return 0.0;
            default:   return 0.0;
        }
    }

    @FXML
    public void handleLogout(ActionEvent event) throws IOException {
        SessionManager.clear();
        Parent root = FXMLLoader.load(getClass().getResource("Sample.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
