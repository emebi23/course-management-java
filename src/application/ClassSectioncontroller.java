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
import java.util.LinkedHashMap;
import java.util.Map;

public class ClassSectioncontroller {

    @FXML private TextField        classIdField;
    @FXML private TextField        yearLevelField;
    @FXML private TextField        sectionField;
    @FXML private ComboBox<String> deptCombo;
    @FXML private TextField        acadYearField;

    @FXML private TableView<ClassSection>            classTable;
    @FXML private TableColumn<ClassSection, String>  colClassId;
    @FXML private TableColumn<ClassSection, Integer> colYearLevel;
    @FXML private TableColumn<ClassSection, String>  colSection;
    @FXML private TableColumn<ClassSection, String>  colDeptName;
    @FXML private TableColumn<ClassSection, String>  colAcademicYear;

    private final Map<String, String>    deptMap   = new LinkedHashMap<>();
    private ObservableList<ClassSection> classList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        colClassId.setCellValueFactory(new PropertyValueFactory<>("classId"));
        colYearLevel.setCellValueFactory(new PropertyValueFactory<>("yearLevel"));
        colSection.setCellValueFactory(new PropertyValueFactory<>("section"));
        colDeptName.setCellValueFactory(new PropertyValueFactory<>("deptName"));
        colAcademicYear.setCellValueFactory(new PropertyValueFactory<>("academicYear"));

        loadDeptCombo();
        loadClasses();

        classTable.setOnMouseClicked(e -> {
            ClassSection cs = classTable.getSelectionModel().getSelectedItem();
            if (cs == null) return;
            classIdField.setText(cs.getClassId());
            yearLevelField.setText(String.valueOf(cs.getYearLevel()));
            sectionField.setText(cs.getSection());
            acadYearField.setText(cs.getAcademicYear());
            for (String key : deptMap.keySet()) {
                if (deptMap.get(key).equals(cs.getDeptId())) {
                    deptCombo.setValue(key); break;
                }
            }
        });
    }

    private void loadDeptCombo() {
        deptMap.clear();
        ObservableList<String> items = FXCollections.observableArrayList();
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(
                 "SELECT dept_id, dept_name FROM department ORDER BY dept_name")) {
            while (rs.next()) {
                String key = rs.getString("dept_id") + " – " + rs.getString("dept_name");
                deptMap.put(key, rs.getString("dept_id"));
                items.add(key);
            }
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        deptCombo.setItems(items);
    }

    private void loadClasses() {
        classList.clear();
        String sql =
            "SELECT cs.class_id, cs.year_level, cs.section, cs.dept_id, " +
            "       IFNULL(d.dept_name,'—') AS dept_name, cs.academic_year " +
            "FROM class_section cs " +
            "LEFT JOIN department d ON cs.dept_id = d.dept_id " +
            "ORDER BY cs.year_level, cs.section";
        try (Connection c = DBConnection.getConnection();
             Statement s = c.createStatement();
             ResultSet rs = s.executeQuery(sql)) {
            while (rs.next())
                classList.add(new ClassSection(
                    rs.getString("class_id"),  rs.getInt("year_level"),
                    rs.getString("section"),   rs.getString("dept_id"),
                    rs.getString("dept_name"), rs.getString("academic_year")));
        } catch (SQLException e) { alert("Error", e.getMessage()); }
        classTable.setItems(classList);
    }

    @FXML private void handleAdd() {
        String id   = classIdField.getText().trim();
        String yr   = yearLevelField.getText().trim();
        String sec  = sectionField.getText().trim();
        String acYr = acadYearField.getText().trim();
        if (id.isEmpty() || yr.isEmpty() || sec.isEmpty()) {
            alert("Error", "Class ID, Year Level and Section are required."); return;
        }
        String deptId = deptMap.get(deptCombo.getValue());
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "INSERT INTO class_section (class_id,year_level,section,dept_id,academic_year) " +
                 "VALUES (?,?,?,?,?)")) {
            s.setString(1, id);
            s.setInt(2, Integer.parseInt(yr));
            s.setString(3, sec);
            if (deptId != null) s.setString(4, deptId); else s.setNull(4, Types.VARCHAR);
            s.setString(5, acYr);
            s.executeUpdate();
            alert("Success", "Class added."); clear(); loadClasses();
        } catch (NumberFormatException e) {
            alert("Error", "Year Level must be a number (1, 2, 3...).");
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleUpdate() {
        String id = classIdField.getText().trim();
        if (id.isEmpty()) { alert("Error", "Select a class row first."); return; }
        String deptId = deptMap.get(deptCombo.getValue());
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE class_section SET year_level=?,section=?,dept_id=?,academic_year=? " +
                 "WHERE class_id=?")) {
            s.setInt(1, Integer.parseInt(yearLevelField.getText().trim()));
            s.setString(2, sectionField.getText().trim());
            if (deptId != null) s.setString(3, deptId); else s.setNull(3, Types.VARCHAR);
            s.setString(4, acadYearField.getText().trim());
            s.setString(5, id);
            s.executeUpdate();
            alert("Success", "Class updated."); clear(); loadClasses();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handleDelete() {
        String id = classIdField.getText().trim();
        if (id.isEmpty()) { alert("Error", "Select a class row first."); return; }
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "DELETE FROM class_section WHERE class_id=?")) {
            s.setString(1, id); s.executeUpdate();
            alert("Success", "Class deleted."); clear(); loadClasses();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML private void handlePromote() {
        ClassSection sel = classTable.getSelectionModel().getSelectedItem();
        if (sel == null) { alert("Error", "Select a class row to promote."); return; }
        int newYear = sel.getYearLevel() + 1;
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setHeaderText("Promote Class " + sel.getClassId() + "?");
        confirm.setContentText(
            "Year " + sel.getYearLevel() + " – " + sel.getSection() +
            " will become Year " + newYear + ".");
        if (confirm.showAndWait().filter(r -> r == ButtonType.OK).isEmpty()) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE class_section SET year_level=? WHERE class_id=?")) {
            s.setInt(1, newYear); s.setString(2, sel.getClassId());
            s.executeUpdate();
            alert("Promoted", "Class promoted to Year " + newYear + ".");
            clear(); loadClasses();
        } catch (SQLException e) { alert("Error", e.getMessage()); }
    }

    @FXML public void goHome(ActionEvent event) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource("Home.fxml"));
        Stage stage = (Stage)((Node)event.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
    }

    private void clear() {
        classIdField.clear(); yearLevelField.clear();
        sectionField.clear(); acadYearField.clear();
        deptCombo.setValue(null);
    }
    private void alert(String t, String m) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setHeaderText(t); a.setContentText(m); a.showAndWait();
    }
}
