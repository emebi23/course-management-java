package application;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;

import java.io.IOException;

public class Homecontroller {

    @FXML private MenuItem homeMenuItem;
    @FXML private MenuItem userMenuItem;
    @FXML private MenuItem studentMenuItem;
    @FXML private MenuItem instructorMenuItem;
    @FXML private MenuItem adminMenuItem;
    @FXML private MenuItem courseMenuItem;
    @FXML private MenuItem enrollMenuItem;
    @FXML private MenuItem logoutMenuItem;

    
    ImageView  myImageView;
    
    
    @FXML
    public void goHome(ActionEvent event) throws IOException {
        System.out.println("Already on home page.");
        // Optionally reload home view if needed
    }

    @FXML
    public void goUser(ActionEvent event) throws IOException {
        navigate("User.fxml", event);
    }

    @FXML
    public void goStudent(ActionEvent event) throws IOException {
        navigate("Student.fxml", event);
    }

    @FXML
    public void goInstructor(ActionEvent event) throws IOException {
        navigate("Instructor.fxml", event);
    }

    @FXML
    public void goAdmin(ActionEvent event) throws IOException {
        navigate("Admin.fxml", event);
    }

    @FXML
    public void goCourse(ActionEvent event) throws IOException {
        navigate("Course.fxml", event);
    }

    @FXML
    public void goEnrollment(ActionEvent event) throws IOException {
        navigate("Enrollment.fxml", event);
        
        
    } 
    
    @FXML
    public void goDashboard(ActionEvent event) throws IOException {
        navigate("Dashbord.fxml", event);
    }
    
    @FXML
    public void goLogout(ActionEvent event) throws IOException {
        navigate("Sample.fxml", event);
    }
   
    

    // Shared method to change scenes
    private void navigate(String fxmlFile, ActionEvent event) throws IOException {
    	
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
        Parent root = loader.load();

        // Get stage from menu's window
        Stage stage = (Stage) ((MenuItem) event.getSource()).getParentPopup().getOwnerWindow();
        stage.setScene(new Scene(root));
        stage.show();
    }

    
    
}
  
 