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

public class Studentcontroller {

    @FXML private TextField        idField;
    @FXML private TextField        nameField;
    @FXML private ComboBox<String> genderCombo;
    @FXML private TextField        emailField;
    @FXML private TextField        phoneField;
    @FXML private ComboBox<String> classCombo;
    @FXML private TextField        searchField;

    @FXML private TableView<Student>            studentTable;
    @FXML private TableColumn<Student, String>  colId;
    @FXML private TableColumn<Student, String>  colName;
    @FXML private TableColumn<Student, String>  colGender;
    @FXML private TableColumn<Student, String>  colEmail;
    @FXML private TableColumn<Student, String>  colPhone;
    @FXML private TableColumn<Student, String>  colClassName;

    @FXML private TableView<Grade>            gradeViewTable;
    @FXML private TableColumn<Grade, String>  colGvCourse;
    @FXML private TableColumn<Grade, String>  colGvCourseName;
    @FXML private TableColumn<Grade, String>  colGvGrade;
    @FXML private TableColumn<Grade, String>  colGvGradePoint;
    @FXML private Label gpaViewLabel;
    @FXML private Label selectedStudentLabel;

    private final Map<String, String> classMap  = new LinkedHashMap<>();
    private ObservableList<Student>   studentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colGender.setCellValueFactory(new PropertyValueFactory<>("gender"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colClassName.setCellValueFactory(new PropertyValueFactory<>("department"));

        colGvCourse.setCellValueFactory(new PropertyValueFactory<>("courseId"));
        colGvCourseName.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        colGvGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));
        colGvGradePoint.setCellValueFactory(new PropertyValueFactory<>("gradePoint"));

        // Gender choices
        genderCombo.setItems(FXCollections.observableArrayList("Male", "Female"));

        loadClassCombo();
        loadStudents();

        studentTable.setOnMouseClicked(e -> {
            populateFields();
            loadSelectedStudentGrades();
        });
    }

    private void loadClassCombo() {
        classMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        String sql =
            "SELECT cs.class_id, cs.year_level, cs.section, " +
            "       IFNULL(d.dept_name,'—') AS dept_name, cs.academic_year " +
            "FROM class_section cs " +
            "LEFT JOIN department d ON cs.dept_id = d.dept_id " +
            "ORDER BY cs.year_level, cs.section";
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                String id  = rs.getString("class_id");
                String key = "Year " + rs.getInt("year_level") + " – " +
                             rs.getString("section") + "  (" +
                             rs.getString("dept_name") + ")  " +
                             rs.getString("academic_year");
                classMap.put(key, id);
                items.add(key);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        classCombo.setItems(items);
    }

    private void loadStudents() {
        studentList.clear();
        String sql =
            "SELECT s.student_id, s.name, s.gender, s.email, s.phone, s.class_id, " +
            "       CONCAT('Year ',cs.year_level,' – ',cs.section," +
            "              ' (',IFNULL(d.dept_name,'—'),')') AS class_display " +
            "FROM students s " +
            "LEFT JOIN class_section cs ON s.class_id = cs.class_id " +
            "LEFT JOIN department    d  ON cs.dept_id  = d.dept_id " +
            "ORDER BY s.name";
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next()) {
                studentList.add(new Student(
                    rs.getString("student_id"), rs.getString("name"),
                    rs.getString("gender"),     rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("class_display") != null
                        ? rs.getString("class_display") : "—"));
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        studentTable.setItems(studentList);
    }

    private void populateFields() {
        Student s = studentTable.getSelectionModel().getSelectedItem();
        if (s == null) return;
        idField.setText(s.getStudentId());
        nameField.setText(s.getName());
        genderCombo.setValue(s.getGender());
        emailField.setText(s.getEmail());
        phoneField.setText(s.getPhone());
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(
                 "SELECT class_id FROM students WHERE student_id=?")) {
            ps.setString(1, s.getStudentId());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String cid = rs.getString("class_id");
                for (String key : classMap.keySet())
                    if (classMap.get(key).equals(cid)) { classCombo.setValue(key); break; }
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    private void loadSelectedStudentGrades() {
        Student s = studentTable.getSelectionModel().getSelectedItem();
        if (s == null) return;
        selectedStudentLabel.setText("Grades for: " + s.getName() + " (" + s.getStudentId() + ")");
        ObservableList<Grade> grades = FXCollections.observableArrayList();
        String sql =
            "SELECT e.course_id, c.course_name, c.credit_hour, " +
            "       IFNULL(g.grade,'Not graded yet') AS grade, " +
            "       IFNULL(g.grade_id,0) AS grade_id, e.enrollment_id " +
            "FROM enrollment e " +
            "JOIN course c ON e.course_id = c.course_id " +
            "LEFT JOIN grade g ON g.enrollment_id = e.enrollment_id " +
            "WHERE e.student_id = ? ORDER BY e.course_id";
        double totalPts = 0; int totalCr = 0;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, s.getStudentId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String grade = rs.getString("grade");
                double gp    = getGradePoint(grade);
                int credit   = rs.getInt("credit_hour");
                String gpStr = grade.equals("Not graded yet") ? "—" : String.format("%.1f", gp);
                grades.add(new Grade(rs.getInt("grade_id"), rs.getInt("enrollment_id"),
                    s.getStudentId(), s.getName(),
                    rs.getString("course_id"), rs.getString("course_name"),
                    grade, gpStr, "—"));
                if (!grade.equals("Not graded yet")) { totalPts += gp * credit; totalCr += credit; }
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        gradeViewTable.setItems(grades);
        gpaViewLabel.setText(totalCr > 0
            ? String.format("GPA: %.2f  |  Credits: %d", totalPts / totalCr, totalCr)
            : "GPA: Not available (no graded courses yet)");
    }

    @FXML private void handleAdd() {
        String studentId = idField.getText().trim();
        String name      = nameField.getText().trim();
        String email     = emailField.getText().trim();
        String phone     = phoneField.getText().trim();
        String gender    = genderCombo.getValue();

        if (studentId.isEmpty() || name.isEmpty()) {
            alert("Error", "ID and Name are required."); return; }

        // ── Validation ────────────────────────────────────────
        String errors = Validator.runAll(
            Validator.validatePhone(phone),
            Validator.validateEmail(email),
            Validator.checkPhoneDuplicate(phone, "students", "student_id", null),
            Validator.checkEmailDuplicate(email, "students", "student_id", null)
        );
        if (errors != null) { alert("Validation Error", errors); return; }

        String classId  = classMap.get(classCombo.getValue());
        String username = studentId.toLowerCase();
        String password = "1234";

        try (Connection conn = DBConnection.getConnection()) {
            int userId;
            try (PreparedStatement chk = conn.prepareStatement(
                    "SELECT id FROM users WHERE username=?")) {
                chk.setString(1, username);
                ResultSet ex = chk.executeQuery();
                if (ex.next()) {
                    userId = ex.getInt("id");
                } else {
                    try (PreparedStatement ins = conn.prepareStatement(
                            "INSERT INTO users (username,password,role) VALUES (?,?,'STUDENT')",
                            Statement.RETURN_GENERATED_KEYS)) {
                        ins.setString(1, username); ins.setString(2, password);
                        ins.executeUpdate();
                        ResultSet k = ins.getGeneratedKeys(); k.next(); userId = k.getInt(1);
                    }
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO students " +
                    "(student_id,name,gender,email,phone,class_id,user_id) VALUES (?,?,?,?,?,?,?)")) {
                ps.setString(1, studentId); ps.setString(2, name);
                ps.setString(3, gender != null ? gender : "");
                ps.setString(4, email); ps.setString(5, phone);
                if (classId != null) ps.setString(6, classId); else ps.setNull(6, Types.VARCHAR);
                ps.setInt(7, userId);
                ps.executeUpdate();
            }
            alert("Student Added",
                "Name     : " + name +
                "\nUsername : " + username +
                "\nPassword : " + password +
                "\nClass    : " + (classCombo.getValue() != null ? classCombo.getValue() : "—"));
            clearFields(); loadStudents();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        String id    = idField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();
        String gender = genderCombo.getValue();
        if (id.isEmpty()) { alert("Error","Select a student first."); return; }

        String errors = Validator.runAll(
            Validator.validatePhone(phone),
            Validator.validateEmail(email),
            Validator.checkPhoneDuplicate(phone, "students", "student_id", id),
            Validator.checkEmailDuplicate(email, "students", "student_id", id)
        );
        if (errors != null) { alert("Validation Error", errors); return; }

        String classId = classMap.get(classCombo.getValue());
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE students SET name=?,gender=?,email=?,phone=?,class_id=? " +
                 "WHERE student_id=?")) {
            s.setString(1, nameField.getText());
            s.setString(2, gender != null ? gender : "");
            s.setString(3, email); s.setString(4, phone);
            if (classId != null) s.setString(5, classId); else s.setNull(5, Types.VARCHAR);
            s.setString(6, id);
            s.executeUpdate();
            alert("Success","Student updated."); clearFields(); loadStudents();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleDelete() {
        String id = idField.getText().trim();
        if (id.isEmpty()) { alert("Error","Select a student first."); return; }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "DELETE FROM students WHERE student_id=?")) {
            s.setString(1, id); s.executeUpdate();
            alert("Success","Student deleted."); clearFields(); loadStudents();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleSearch() {
        String kw = searchField.getText().trim().toLowerCase();
        if (kw.isEmpty()) { loadStudents(); return; }
        ObservableList<Student> filtered = FXCollections.observableArrayList();
        for (Student s : studentList)
            if (s.getStudentId().toLowerCase().contains(kw)
             || s.getName().toLowerCase().contains(kw))
                filtered.add(s);
        studentTable.setItems(filtered);
    }

    @FXML public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
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

    private void clearFields() {
        idField.clear(); nameField.clear(); genderCombo.setValue(null);
        emailField.clear(); phoneField.clear(); classCombo.setValue(null);
    }
    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(t); a.setContentText(m); a.showAndWait();
    }
}
