
	
	package application;

	import javafx.collections.FXCollections;
	import javafx.collections.ObservableList;
	import javafx.fxml.FXML;
	import javafx.scene.control.*;
	import javafx.scene.control.cell.PropertyValueFactory;

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

	    private Connection connect() {
	        try {
	            return DriverManager.getConnection("jdbc:mysql://localhost:3306/cms", "root", "Zeamanuelgech@23");
	        } catch (SQLException e) {
	            e.printStackTrace();
	            return null;
	        }
	    }

	    @FXML
	    public void initialize() {
	        cbRole.setItems(FXCollections.observableArrayList("student", "instructor"));

	        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
	        colUsername.setCellValueFactory(new PropertyValueFactory<>("username"));
	        colPassword.setCellValueFactory(new PropertyValueFactory<>("password"));
	        colRole.setCellValueFactory(new PropertyValueFactory<>("role"));

	        loadUsers();
	        userTable.setOnMouseClicked(e -> populateFields());
	    }

	    private void loadUsers() {
	        ObservableList<User> users = FXCollections.observableArrayList();
	        String query = "SELECT * FROM users";

	        try (Connection conn = connect(); Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
	            while (rs.next()) {
	                users.add(new User(
	                    rs.getInt("id"),
	                    rs.getString("username"),
	                    rs.getString("password"),
	                    rs.getString("role")
	                ));
	            }
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }

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
	        String query = "INSERT INTO users (username, password, role) VALUES (?, ?, ?)";

	        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
	            pstmt.setString(1, tfUsername.getText());
	            pstmt.setString(2, tfPassword.getText());
	            pstmt.setString(3, cbRole.getValue());
	            pstmt.executeUpdate();
	            clearFields();
	            loadUsers();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    @FXML
	    public void updateUser() {
	        User selected = userTable.getSelectionModel().getSelectedItem();
	        if (selected == null) return;

	        String query = "UPDATE users SET username = ?, password = ?, role = ? WHERE id = ?";

	        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
	            pstmt.setString(1, tfUsername.getText());
	            pstmt.setString(2, tfPassword.getText());
	            pstmt.setString(3, cbRole.getValue());
	            pstmt.setInt(4, selected.getId());
	            pstmt.executeUpdate();
	            clearFields();
	            loadUsers();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    @FXML
	    public void deleteUser() {
	        User selected = userTable.getSelectionModel().getSelectedItem();
	        if (selected == null) return;

	        String query = "DELETE FROM users WHERE id = ?";

	        try (Connection conn = connect(); PreparedStatement pstmt = conn.prepareStatement(query)) {
	            pstmt.setInt(1, selected.getId());
	            pstmt.executeUpdate();
	            clearFields();
	            loadUsers();
	        } catch (SQLException e) {
	            e.printStackTrace();
	        }
	    }

	    private void clearFields() {
	        tfUsername.clear();
	        tfPassword.clear();
	        cbRole.setValue(null);
	    }
	}
