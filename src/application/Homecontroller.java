package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Homecontroller {

    // ── Card + nav bar buttons ────────────────────────────────
    @FXML public void goStudentAction(ActionEvent e)        throws IOException { nav("Student.fxml", e); }
    @FXML public void goInstructorAction(ActionEvent e)     throws IOException { nav("Instructor.fxml", e); }
    @FXML public void goCourseAction(ActionEvent e)         throws IOException { nav("Course.fxml", e); }
    @FXML public void goEnrollmentAction(ActionEvent e)     throws IOException { nav("Enrollment.fxml", e); }
    @FXML public void goDepartmentAction(ActionEvent e)     throws IOException { nav("Department.fxml", e); }
    @FXML public void goClassSectionAction(ActionEvent e)   throws IOException { nav("ClassSection.fxml", e); }
    @FXML public void goCourseOfferingAction(ActionEvent e) throws IOException { nav("CourseOffering.fxml", e); }
    @FXML public void goUserAction(ActionEvent e)           throws IOException { nav("User.fxml", e); }
    @FXML public void goDashboard(ActionEvent e)            throws IOException { nav("Dashbord.fxml", e); }

    @FXML public void goLogout(ActionEvent e) throws IOException {
        SessionManager.clear();
        nav("Sample.fxml", e);
    }

    // ── Helper ────────────────────────────────────────────────
    private void nav(String fxml, ActionEvent e) throws IOException {
        Parent root = FXMLLoader.load(getClass().getResource(fxml));
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }
}
