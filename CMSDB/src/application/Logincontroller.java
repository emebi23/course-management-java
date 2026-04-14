package application;


import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.sql.*;

public class Logincontroller {

    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;

 

    @FXML
    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (checkCredentials(username, password)) {
            try {
                Parent root = FXMLLoader.load(getClass().getResource("/application/Home.fxml"));
                Stage stage = (Stage) usernameField.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (Exception e) {
            	System.out.println(" Error loading Home.fxml:");
                e.printStackTrace();
            }
        } else {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setHeaderText("Login Failed");
            alert.setContentText("Incorrect username or password");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleReset() {
        usernameField.clear();
        passwordField.clear();
    }

    private boolean checkCredentials(String username, String password) {
    	
        try (
        		//Class.forName("com.mysql.jdbc.Driver");
        		Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/CMS","root", "Zeamanuelgech@23"))
        		{
            String sql = "SELECT * FROM users WHERE username=? AND password=?";
           
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1,username);
            stmt.setString(2,password);
           
            ResultSet rs = stmt.executeQuery();
         // Check if a match was found
            boolean result = rs.next();  //true if record found

            // Clean up
            rs.close();
            stmt.close();
            conn.close();

            return result;
          
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}