package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class Dashboardcontroller {

    @FXML private Label studentCountLabel;
    @FXML private Label instructorCountLabel;
    @FXML private Label courseCountLabel;
    @FXML private Label userCountLabel;
    @FXML private Label enrollmentCountLabel;


    private Connection connect() {
        try {
            // Change DB name, user, password if needed
            return DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "pass123");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @FXML
    public void initialize() {
        loadCounts();
    }

    private void loadCounts() {
        Connection conn = connect();
        if (conn == null) return;

        try {
            studentCountLabel.setText(getCount(conn, "students"));
            instructorCountLabel.setText(getCount(conn, "instructor"));
            courseCountLabel.setText(getCount(conn, "course"));
            userCountLabel.setText(getCount(conn, "users"));
            enrollmentCountLabel.setText(getCount(conn, "enrollment"));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCount(Connection conn, String tableName) {
        String sql = "SELECT COUNT(*) AS total FROM " + tableName;
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getString("total");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "0";
    }
    
    @FXML
    public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }
}
