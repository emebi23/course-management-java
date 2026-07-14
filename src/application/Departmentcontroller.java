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

public class Departmentcontroller {

    @FXML private TextField deptIdField;
    @FXML private TextField deptNameField;

    @FXML private TableView<Department>            deptTable;
    @FXML private TableColumn<Department, String>  colDeptId;
    @FXML private TableColumn<Department, String>  colDeptName;

    private ObservableList<Department> list = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colDeptId.setCellValueFactory(new PropertyValueFactory<>("deptId"));
        colDeptName.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        load();
        deptTable.setOnMouseClicked(e -> {
            Department d = deptTable.getSelectionModel().getSelectedItem();
            if (d != null) {
                deptIdField.setText(d.getDeptId());
                deptNameField.setText(d.getDeptName());
            }
        });
    }

    private void load() {
        list.clear();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery("SELECT * FROM department ORDER BY dept_name")) {
            while (rs.next())
                list.add(new Department(rs.getString("dept_id"), rs.getString("dept_name")));
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        deptTable.setItems(list);
    }

    @FXML private void handleAdd() {
        String id = deptIdField.getText().trim();
        String nm = deptNameField.getText().trim();
        if (id.isEmpty() || nm.isEmpty()) { alert("Error","ID and Name required."); return; }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "INSERT INTO department (dept_id, dept_name) VALUES (?,?)")) {
            s.setString(1, id); s.setString(2, nm);
            s.executeUpdate();
            alert("Success","Department added."); clear(); load();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        String id = deptIdField.getText().trim();
        if (id.isEmpty()) { alert("Error","Select a department first."); return; }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE department SET dept_name=? WHERE dept_id=?")) {
            s.setString(1, deptNameField.getText().trim()); s.setString(2, id);
            s.executeUpdate();
            alert("Success","Department updated."); clear(); load();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleDelete() {
        String id = deptIdField.getText().trim();
        if (id.isEmpty()) { alert("Error","Select a department first."); return; }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "DELETE FROM department WHERE dept_id=?")) {
            s.setString(1, id); s.executeUpdate();
            alert("Success","Department deleted."); clear(); load();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void clear() { deptIdField.clear(); deptNameField.clear(); }
    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(t); a.setContentText(m); a.showAndWait();
    }
}
