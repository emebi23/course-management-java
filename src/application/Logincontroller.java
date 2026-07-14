package application;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.sql.*;

public class Logincontroller {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showAlert("Please enter username and password.");
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                int userId  = rs.getInt("id");
                SessionManager.setUser(username, role, userId);

                String fxml;
                switch (role.toUpperCase()) {
                    case "ADMIN":       fxml = "Home.fxml";            break;
                    case "INSTRUCTOR":  fxml = "InstructorHome.fxml";  break;
                    case "STUDENT":     fxml = "StudentDashboard.fxml"; break;
                    default:
                        showAlert("Unknown role: " + role);
                        return;
                }

                Parent root = FXMLLoader.load(getClass().getResource(fxml));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
                stage.show();
            } else {
                showAlert("Incorrect username or password.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Database error: " + e.getMessage());
        }
    }

    @FXML
    private void handleReset() {
        usernameField.clear();
        passwordField.clear();
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setHeaderText("Login Failed");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
