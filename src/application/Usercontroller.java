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

public class Usercontroller {

    @FXML private TextField tfUsername;
    @FXML private PasswordField tfPassword;
    @FXML private ComboBox<String> cbRole;
    @FXML private TableView<User> userTable;
    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colUsername;
    @FXML private TableColumn<User, String> colPassword;
    @FXML private TableColumn<User, String> colRole;

    @FXML
    public void initialize() {
        // Roles stored as uppercase in DB to match login switch
        cbRole.setItems(FXCollections.observableArrayList("ADMIN", "INSTRUCTOR", "STUDENT"));

        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

        loadUsers();
        userTable.setOnMouseClicked(e -> populateFields());
    }

    private void loadUsers() {
        ObservableList<User> users = FXCollections.observableArrayList();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM users")) {
            while (rs.next()) {
                users.add(new User(
                    rs.getInt("id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        userTable.setItems(users);
    }

    private void populateFields() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            tfUsername.setText(selected.getUsername());
            tfPassword.setText(selected.getPassword());
            cbRole.setValue(selected.getRole());
        }
    }

    @FXML
    public void addUser() {
        if (tfUsername.getText().isEmpty() || tfPassword.getText().isEmpty() || cbRole.getValue() == null) {
            showAlert("Error", "Please fill all fields.");
            return;
        }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "INSERT INTO users (username, password, role) VALUES (?, ?, ?)")) {
            stmt.setString(1, tfUsername.getText());
            stmt.setString(2, tfPassword.getText());
            stmt.setString(3, cbRole.getValue());
            stmt.executeUpdate();
            clearFields(); loadUsers();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML
    public void updateUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Error", "Select a user first."); return; }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                 "UPDATE users SET username=?, password=?, role=? WHERE id=?")) {
            stmt.setString(1, tfUsername.getText());
            stmt.setString(2, tfPassword.getText());
            stmt.setString(3, cbRole.getValue());
            stmt.setInt(4, selected.getId());
            stmt.executeUpdate();
            clearFields(); loadUsers();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML
    public void deleteUser() {
        User selected = userTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showAlert("Error", "Select a user first."); return; }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM users WHERE id=?")) {
            stmt.setInt(1, selected.getId());
            stmt.executeUpdate();
            clearFields(); loadUsers();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML
    public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void clearFields() {
        tfUsername.clear();
        tfPassword.clear();
        cbRole.setValue(null);
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title); a.setContentText(msg); a.showAndWait();
    }
}
