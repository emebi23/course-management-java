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

public class Instructorcontroller {

    @FXML private TextField idField;
    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private TextField specializationField;
    @FXML private TextField searchField;

    @FXML private TableView<Instructor> instructorTable;
    @FXML private TableColumn<Instructor, String> colId;
    @FXML private TableColumn<Instructor, String> colName;
    @FXML private TableColumn<Instructor, String> colEmail;
    @FXML private TableColumn<Instructor, String> colPhone;
    @FXML private TableColumn<Instructor, String> colSpec;

    private ObservableList<Instructor> instructorList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colId.setCellValueFactory(new PropertyValueFactory<>("instructorId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("name"));
        colEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colPhone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        colSpec.setCellValueFactory(new PropertyValueFactory<>("specialization"));
        loadInstructors();
        instructorTable.setOnMouseClicked(e -> populateFields());
    }

    private void loadInstructors() {
        instructorList.clear();
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM instructor")) {
            while (rs.next()) {
                instructorList.add(new Instructor(
                    rs.getString("instructor_id"), rs.getString("name"),
                    rs.getString("email"),         rs.getString("phone"),
                    rs.getString("specialization")
                ));
            }
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
        instructorTable.setItems(instructorList);
    }

    private void populateFields() {
        Instructor i = instructorTable.getSelectionModel().getSelectedItem();
        if (i != null) {
            idField.setText(i.getInstructorId());
            nameField.setText(i.getName());
            emailField.setText(i.getEmail());
            phoneField.setText(i.getPhone());
            specializationField.setText(i.getSpecialization());
        }
    }

    @FXML private void handleAdd() {
        if (idField.getText().isEmpty() || nameField.getText().isEmpty()) { showAlert("Error","ID and Name required."); return; }
        String instructorId = idField.getText().trim();
        String name         = nameField.getText().trim();
        String email        = emailField.getText().trim();
        String phone        = phoneField.getText().trim();
        String spec         = specializationField.getText().trim();

        // ── Validation ────────────────────────────────────────
        String errors = Validator.runAll(
            Validator.validatePhone(phone),
            Validator.validateEmail(email),
            Validator.checkPhoneDuplicate(phone, "instructor", "instructor_id", null),
            Validator.checkEmailDuplicate(email, "instructor", "instructor_id", null)
        );
        if (errors != null) { showAlert("Validation Error", errors); return; }

        // Default login: username = instructor_id (lowercase), password = "1234"
        String username = instructorId.toLowerCase();
        String password = "1234";

        try (Connection conn = DBConnection.getConnection()) {
            // 1. Check if username already exists
            try (PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM users WHERE username = ?")) {
                check.setString(1, username);
                ResultSet rs = check.executeQuery();
                if (rs.next()) {
                    showAlert("Error", "A user with username '" + username + "' already exists.");
                    return;
                }
            }

            // 2. Insert into users table and get generated user_id
            int newUserId = -1;
            try (PreparedStatement userStmt = conn.prepareStatement(
                    "INSERT INTO users (username, password, role) VALUES (?, ?, 'INSTRUCTOR')",
                    java.sql.Statement.RETURN_GENERATED_KEYS)) {
                userStmt.setString(1, username);
                userStmt.setString(2, password);
                userStmt.executeUpdate();
                ResultSet keys = userStmt.getGeneratedKeys();
                if (keys.next()) newUserId = keys.getInt(1);
            }

            // 3. Insert into instructor table with linked user_id
            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO instructor (instructor_id,name,email,phone,specialization,user_id) VALUES (?,?,?,?,?,?)")) {
                stmt.setString(1, instructorId);
                stmt.setString(2, name);
                stmt.setString(3, email);
                stmt.setString(4, phone);
                stmt.setString(5, spec);
                stmt.setInt(6, newUserId);
                stmt.executeUpdate();
            }

            showAlert("Success",
                "Instructor added successfully!\n\nLogin credentials:\n  Username: " + username + "\n  Password: " + password);
            clearFields();
            loadInstructors();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        if (idField.getText().isEmpty()) { showAlert("Error","Select an instructor first."); return; }
        String id    = idField.getText().trim();
        String email = emailField.getText().trim();
        String phone = phoneField.getText().trim();

        // ── Validation (exclude current record from duplicate check) ──
        String errors = Validator.runAll(
            Validator.validatePhone(phone),
            Validator.validateEmail(email),
            Validator.checkPhoneDuplicate(phone, "instructor", "instructor_id", id),
            Validator.checkEmailDuplicate(email, "instructor", "instructor_id", id)
        );
        if (errors != null) { showAlert("Validation Error", errors); return; }

        String sql = "UPDATE instructor SET name=?,email=?,phone=?,specialization=? WHERE instructor_id=?";
        try (Connection conn = DBConnection.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1,nameField.getText()); stmt.setString(2,email);
            stmt.setString(3,phone); stmt.setString(4,specializationField.getText());
            stmt.setString(5,id);
            stmt.executeUpdate();
            showAlert("Success","Instructor updated."); clearFields(); loadInstructors();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML private void handleDelete() {
        if (idField.getText().isEmpty()) { showAlert("Error","Select an instructor first."); return; }
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM instructor WHERE instructor_id=?")) {
            stmt.setString(1, idField.getText());
            stmt.executeUpdate();
            showAlert("Success","Instructor deleted."); clearFields(); loadInstructors();
        } catch (SQLException e) { showAlert("Error", e.getMessage()); }
    }

    @FXML private void handleSearch() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadInstructors(); return; }
        ObservableList<Instructor> filtered = FXCollections.observableArrayList();
        for (Instructor i : instructorList) {
            if (i.getInstructorId().contains(keyword) || i.getName().toLowerCase().contains(keyword.toLowerCase()))
                filtered.add(i);
        }
        instructorTable.setItems(filtered);
    }

    @FXML public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void clearFields() {
        idField.clear(); nameField.clear(); emailField.clear(); phoneField.clear(); specializationField.clear();
    }

    private void showAlert(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(title); a.setContentText(msg); a.showAndWait();
    }
}
