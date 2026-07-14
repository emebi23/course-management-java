package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import javafx.event.ActionEvent;
import java.io.IOException;
import java.sql.*;


import javafx.scene.Node;

public class Coursecontroller {

    @FXML private TextField codeField;
    @FXML private TextField titleField;
    @FXML private TextField instructorField;
    @FXML private Button addButton;

    // MySQL connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/CMS";
    private final String DB_USER = "root";
    private final String DB_PASS = "Zeamanuelgech@23"; 

    // Connect to MySQL
    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @FXML
    private void handleAdd() {
        String code = codeField.getText();
        String title = titleField.getText();
        String instructor = instructorField.getText();

        if (code.isEmpty() || title.isEmpty() || instructor.isEmpty()) {
            showAlert("Validation Error", "Please fill in all fields.");
            return;
        }

        String sql = "INSERT INTO course (coursecode, title, instructor) VALUES (?, ?, ?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            stmt.setString(2, title);
            stmt.setString(3, instructor);
            stmt.executeUpdate();
            showAlert("Success", "Course added successfully.");
            clearFields();
        } catch (SQLException e) {
            showAlert("Database Error", e.getMessage());
        }
    }
    @FXML
    private void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void clearFields() {
        codeField.clear();
        titleField.clear();
        instructorField.clear();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
