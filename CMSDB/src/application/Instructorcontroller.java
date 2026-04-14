package application;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.sql.*;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.Node;
import javafx.stage.Stage;
import java.io.IOException;

public class Instructorcontroller {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField departmentField;
    @FXML private Button addButton;
    @FXML private Button updateButton;
    @FXML private Button deleteButton;

    private final String DB_URL = "jdbc:mysql://localhost:3306/CMS";
    private final String DB_USER = "root";
    private final String DB_PASS = "Zeamanuelgech@23";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    private boolean validateFields(String id, String name, String department) {
        if (id.isEmpty() || name.isEmpty() || department.isEmpty()) {
            showAlert("Error", "Please fill all fields.");
            return false;
        }
        return true;
    }

    private int parseId(String id) throws NumberFormatException {
        return Integer.parseInt(id);
    }

    private void executeUpdate(String sql, Object... params) {
        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            int rows = stmt.executeUpdate();
            showAlert("Success", rows + " instructor(s) affected.");
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleAdd() {
        String id = idField.getText();
        String name = nameField.getText();
        String department = departmentField.getText();

        if (!validateFields(id, name, department)) return;

        String sql = "INSERT INTO instructor (id, name, department) VALUES (?, ?, ?)";
        executeUpdate(sql, parseId(id), name, department);
    }

    @FXML
    private void handleUpdate() {
        String id = idField.getText();
        String name = nameField.getText();
        String department = departmentField.getText();

        if (!validateFields(id, name, department)) return;

        String sql = "UPDATE instructor SET name = ?, department = ? WHERE id = ?";
        executeUpdate(sql, name, department, parseId(id));
    }

    @FXML
    private void handleDelete() {
        String id = idField.getText();

        if (id.isEmpty()) {
            showAlert("Error", "Please enter the Instructor ID to delete.");
            return;
        }

        String sql = "DELETE FROM instructor WHERE id = ?";
        executeUpdate(sql, parseId(id));
    }

    @FXML
    public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
    
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
