package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;
import application.Enrollment;


import java.sql.*;

public class Enrollmentcontroller {

    @FXML private TextField studentIdField;
    @FXML private TextField courseCodeField;
    @FXML private TextField gradeField;
    @FXML private Button enrollButton;

    private final String DB_URL = "jdbc:mysql://localhost:3306/CMS";
    private final String DB_USER = "root";
    private final String DB_PASS = "Zeamanuelgech@23";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    //table view
    
    
    @FXML
    private TableView<Enrollment> enrollmentTable;

    @FXML
    private TableColumn<Enrollment, Integer> colId;

    @FXML
    private TableColumn<Enrollment, String> colStudentId;

    @FXML
    private TableColumn<Enrollment, String> colCourseCode;

    @FXML
    private TableColumn<Enrollment, String> colGrade;

    private ObservableList<Enrollment> enrollmentList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colStudentId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));

        loadEnrollments();
    }

    private void loadEnrollments() {
        String query = "SELECT * FROM enrollment";

        try (Connection conn = connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            enrollmentList.clear();

            while (rs.next()) {
                int id = rs.getInt("id");
                String studentId = rs.getString("student_id");  
                String courseCode = rs.getString("course_code"); 
                String grade = rs.getString("grade");

                enrollmentList.add(new Enrollment(id, studentId, courseCode, grade));
            }

            enrollmentTable.setItems(enrollmentList);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
      
    
    
    @FXML
    private void handleEnroll() {
        String studentId = studentIdField.getText();
        String courseCode = courseCodeField.getText();
        String grade = gradeField.getText();

        if (studentId.isEmpty() || courseCode.isEmpty()) {
            showAlert("Validation Error", "Student ID and Course Code are required.");
            return;
        }

        String sql = "INSERT INTO enrollment (student_id, course_code, grade) VALUES (?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, studentId);
            stmt.setString(2, courseCode);
            stmt.setString(3, grade.isEmpty() ? null : grade);
            stmt.executeUpdate();
            showAlert("Success", "Enrollment added successfully.");
            clearFields();
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }
    
    @FXML
    public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void clearFields() {
        studentIdField.clear();
        courseCodeField.clear();
        gradeField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

   
