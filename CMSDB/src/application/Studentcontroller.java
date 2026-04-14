package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

//import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.*;

import javafx.event.ActionEvent;
import javafx.scene.Node;




public class Studentcontroller {

    @FXML
    private TextField idField;
    @FXML
    private TextField nameField;
    @FXML
    private TextField emailField;
    @FXML
    private Button addButton;
    @FXML
    private Button updateButton;
    @FXML
    private Button deleteButton;

    // MySQL connection details
    private final String DB_URL = "jdbc:mysql://localhost:3306/CMS";
    private final String DB_USER = "root";
    private final String DB_PASS = "Zeamanuelgech@23";

    private Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }

    @FXML
    private void handleAdd() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        String sql = "INSERT INTO students (id, name, email) VALUES (?,?,?)";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));
            stmt.setString(2, name);
            stmt.setString(3, email);

            int rows = stmt.executeUpdate();
            showAlert("Success", rows + " student added.");
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleUpdate() {
        String id = idField.getText();
        String name = nameField.getText();
        String email = emailField.getText();

        if (id.isEmpty() || name.isEmpty() || email.isEmpty()) {
            showAlert("Error", "Please fill in all fields.");
            return;
        }

        String sql = "UPDATE students SET name = ?, email = ?  WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            stmt.setString(2, email);
            stmt.setInt(3, Integer.parseInt(id));

            int rows = stmt.executeUpdate();
            showAlert("Success", rows + " student updated.");
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        String id = idField.getText();

        if (id.isEmpty()) {
            showAlert("Error", "Please enter the Student ID to delete.");
            return;
        }

        String sql = "DELETE FROM students WHERE id = ?";

        try (Connection conn = connect(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, Integer.parseInt(id));

            int rows = stmt.executeUpdate();
            showAlert("Success", rows + " student deleted.");
        } catch (SQLException | NumberFormatException e) {
            showAlert("Error", e.getMessage());
        }
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
