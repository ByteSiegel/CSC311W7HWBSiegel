package com.example.module03_basicgui_db_interface;

import com.example.module03_basicgui_db_interface.db.ConnDbOps;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.sql.*;
import java.util.ResourceBundle;

/**
 * Controller class for the main application GUI.
 */
public class DB_GUI_Controller implements Initializable {

    private final ObservableList<Person> data = FXCollections.observableArrayList();

    @FXML
    private TextField name_field, email_field, phone_field, address_field;
    @FXML
    private PasswordField password_field;
    @FXML
    private TableView<Person> tv;
    @FXML
    private TableColumn<Person, Integer> tv_id;
    @FXML
    private TableColumn<Person, String> tv_name, tv_email, tv_phone, tv_address;
    @FXML
    private ImageView img_view;

    final ConnDbOps dbOps = new ConnDbOps();
    private String profilePicturePath = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        dbOps.connectToDatabase();
        tv_id.setCellValueFactory(new PropertyValueFactory<>("id"));
        tv_name.setCellValueFactory(new PropertyValueFactory<>("name"));
        tv_email.setCellValueFactory(new PropertyValueFactory<>("email"));
        tv_phone.setCellValueFactory(new PropertyValueFactory<>("phone"));
        tv_address.setCellValueFactory(new PropertyValueFactory<>("address"));

        tv.setItems(data);
        loadUsersFromDatabase();

        // Set the default image
        Image defaultImage = new Image(getClass().getResource("/com/example/module03_basicgui_db_interface/profile.png").toExternalForm());
        img_view.setImage(defaultImage);

        // Make the ImageView circular (optional)
        Circle clip = new Circle(img_view.getFitWidth() / 2, img_view.getFitHeight() / 2, img_view.getFitWidth() / 2);
        img_view.setClip(clip);

        // Change cursor on hover
        img_view.setOnMouseEntered(event -> img_view.setStyle("-fx-cursor: hand;"));
        img_view.setOnMouseExited(event -> img_view.setStyle("-fx-cursor: default;"));
        addLiveValidation();
    }
    private void addLiveValidation() {
        // Validate the name field
        name_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.isEmpty()) {
                setInvalid(name_field);
            } else {
                setValid(name_field);
            }
        });

        // Validate the email field with regex
        email_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (isValidEmail(newValue)) {
                setValid(email_field);
            } else {
                setInvalid(email_field);
            }
        });

        // Validate password length
        password_field.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue.length() < 6) {
                setInvalid(password_field);
            } else {
                setValid(password_field);
            }
        });
    }

    // Helper methods to apply styles
    private void setValid(TextField field) {
        field.getStyleClass().removeAll("text-field-invalid");
        if (!field.getStyleClass().contains("text-field-valid")) {
            field.getStyleClass().add("text-field-valid");
        }
    }

    private void setInvalid(TextField field) {
        field.getStyleClass().removeAll("text-field-valid");
        if (!field.getStyleClass().contains("text-field-invalid")) {
            field.getStyleClass().add("text-field-invalid");
        }
    }


    /**
     * Loads users from the database into the TableView.
     */
    private void loadUsersFromDatabase() {
        data.clear();
        String sql = "SELECT * FROM users";
        try (Connection conn = DriverManager.getConnection(dbOps.DB_URL, dbOps.USERNAME, dbOps.PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Person person = new Person();
                person.setId(rs.getInt("id"));
                person.setName(rs.getString("name"));
                person.setEmail(rs.getString("email"));
                person.setPhone(rs.getString("phone"));
                person.setAddress(rs.getString("address"));
                person.setProfilePicture(rs.getString("profile_picture"));
                data.add(person);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a new record to the database and updates the TableView.
     */
    @FXML
    protected void addNewRecord() {
        String name = name_field.getText();
        String email = email_field.getText();
        String phone = phone_field.getText();
        String address = address_field.getText();
        String password = password_field.getText();

        // Input validation
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showAlert("Validation Error", "Name, Email, and Password are required.");
            return;
        }

        if (!isValidEmail(email)) {
            showAlert("Validation Error", "Please enter a valid email address.");
            return;
        }

        if (password.length() < 6) {
            showAlert("Validation Error", "Password must be at least 6 characters long.");
            return;
        }

        // Insert user into database in a background thread
        new Thread(() -> {
            dbOps.insertUser(name, email, phone, address, password, profilePicturePath);
            Platform.runLater(() -> {
                loadUsersFromDatabase();
                clearForm();
            });
        }).start();
    }

    /**
     * Clears the input form and resets the image to the default profile picture.
     */
    @FXML
    protected void clearForm() {
        System.out.println("clearForm() method called");
        name_field.clear();
        email_field.clear();
        phone_field.clear();
        address_field.clear();
        password_field.clear();

        // Reset profile picture path
        profilePicturePath = null;

        // Reset the image to the default profile picture
        URL imageUrl = getClass().getResource("/com/example/module03_basicgui_db_interface/profile.png");
        if (imageUrl != null) {
            Image defaultImage = new Image(imageUrl.toExternalForm());
            img_view.setImage(defaultImage);
            System.out.println("Default image set in clearForm()");
        } else {
            System.out.println("Default image not found in clearForm()");
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    protected void closeApplication() {
        System.exit(0);
    }

    /**
     * Edits the selected record.
     */
    @FXML
    protected void editRecord() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson == null) {
            showAlert("Selection Error", "No user selected for editing.");
            return;
        }

        int id = selectedPerson.getId();
        String name = name_field.getText();
        String email = email_field.getText();
        String phone = phone_field.getText();
        String address = address_field.getText();
        String password = password_field.getText();

        // Input validation
        if (name.isEmpty() || email.isEmpty()) {
            showAlert("Validation Error", "Name and Email are required.");
            return;
        }

        new Thread(() -> {
            dbOps.updateUser(id, name, email, phone, address, password, profilePicturePath);
            Platform.runLater(() -> {
                loadUsersFromDatabase();
                clearForm();
            });
        }).start();
    }

    /**
     * Deletes the selected record.
     */
    @FXML
    protected void deleteRecord() {
        Person selectedPerson = tv.getSelectionModel().getSelectedItem();
        if (selectedPerson == null) {
            showAlert("Selection Error", "No user selected for deletion.");
            return;
        }

        int id = selectedPerson.getId();

        new Thread(() -> {
            dbOps.deleteUser(id);
            Platform.runLater(() -> {
                loadUsersFromDatabase();
                clearForm();
            });
        }).start();
    }

    /**
     * Opens a file chooser to select an image for the profile picture.
     */
    @FXML
    protected void showImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        File file = fileChooser.showOpenDialog(img_view.getScene().getWindow());
        if (file != null) {
            profilePicturePath = file.getAbsolutePath();
            img_view.setImage(new Image(file.toURI().toString()));
        }
    }

    /**
     * Loads the selected item's data into the form.
     */
    @FXML
    protected void selectedItemTV() {
        Person p = tv.getSelectionModel().getSelectedItem();
        if (p != null) {
            name_field.setText(p.getName());
            email_field.setText(p.getEmail());
            phone_field.setText(p.getPhone());
            address_field.setText(p.getAddress());
            // Password handling should be secure; typically you wouldn't display it
            // Load profile picture if available
            if (p.getProfilePicture() != null) {
                File file = new File(p.getProfilePicture());
                if (file.exists()) {
                    img_view.setImage(new Image(file.toURI().toString()));
                    profilePicturePath = p.getProfilePicture();
                } else {
                    // Set default image
                    URL imageUrl = getClass().getResource("/com/example/module03_basicgui_db_interface/profile.png");
                    if (imageUrl != null) {
                        Image defaultImage = new Image(imageUrl.toExternalForm());
                        img_view.setImage(defaultImage);
                        profilePicturePath = null;
                    } else {
                        System.out.println("Default image not found in selectedItemTV()");
                    }
                }
            } else {
                // Set default image
                URL imageUrl = getClass().getResource("/com/example/module03_basicgui_db_interface/profile.png");
                if (imageUrl != null) {
                    Image defaultImage = new Image(imageUrl.toExternalForm());
                    img_view.setImage(defaultImage);
                    profilePicturePath = null;
                } else {
                    System.out.println("Default image not found in selectedItemTV()");
                }
            }
        }
    }

    /**
     * Displays an alert dialog with the given title and message.
     *
     * @param title   The title of the alert dialog.
     * @param message The message to display.
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Validates the email format.
     *
     * @param email The email address to validate.
     * @return True if valid, false otherwise.
     */
    private boolean isValidEmail(String email) {
        // Simple email validation pattern
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        return email.matches(emailRegex);
    }

    /**
     * Switches the application to the dark theme.
     */
    @FXML
    protected void switchToDarkTheme() {
        tv.getScene().getStylesheets().clear();
        tv.getScene().getStylesheets().add(getClass().getResource("/com/example/module03_basicgui_db_interface/dark-theme.css").toExternalForm());
    }

    /**
     * Switches the application to the light theme.
     */
    @FXML
    protected void switchToLightTheme() {
        tv.getScene().getStylesheets().clear();
        tv.getScene().getStylesheets().add(getClass().getResource("/com/example/module03_basicgui_db_interface/light-theme.css").toExternalForm());
    }

    /**
     * Displays the challenges faced during the integration process.
     */


    @FXML
    protected void showChallenges() {
        // Create bold text for challenges and regular text for solutions
        Text challenge1 = new Text("- Ensuring seamless integration between the UI and database operations: ");
        challenge1.setStyle("-fx-font-weight: bold;");
        Text solution1 = new Text("Achieved by using a dedicated `ConnDbOps` class for database operations, " +
                "which encapsulates connection logic and database queries.\n");

        Text challenge2 = new Text("- Implementing theme switching without restarting the application: ");
        challenge2.setStyle("-fx-font-weight: bold;");
        Text solution2 = new Text("Handled by dynamically loading stylesheets in the `switchToDarkTheme()` " +
                "and `switchToLightTheme()` methods, allowing real-time theme changes.\n");

        Text challenge3 = new Text("- Validating user input to maintain data integrity: ");
        challenge3.setStyle("-fx-font-weight: bold;");
        Text solution3 = new Text("Implemented input validation in methods like `addNewRecord()` with checks " +
                "for non-empty fields, valid email format, and password length.\n");

        Text challenge4 = new Text("- Handling file selection and image loading for profile pictures: ");
        challenge4.setStyle("-fx-font-weight: bold;");
        Text solution4 = new Text("Used a `FileChooser` to select images, and displayed the selected profile picture " +
                "using `ImageView`'s `setImage()` method. The circular clipping effect on profile pictures " +
                "was applied using a `Circle` shape.\n");

        Text challenge5 = new Text("- Managing multi-threading when performing database operations to keep the UI responsive: ");
        challenge5.setStyle("-fx-font-weight: bold;");
        Text solution5 = new Text("Executed database operations on background threads using `new Thread(() -> {...}).start()`, " +
                "and updated the UI safely by wrapping changes in `Platform.runLater()` blocks.");

        // Combine all text into a TextFlow for formatting
        TextFlow textFlow = new TextFlow(
                challenge1, solution1,
                challenge2, solution2,
                challenge3, solution3,
                challenge4, solution4,
                challenge5, solution5
        );

        // Set the preferred size of the TextFlow for better display
        textFlow.setPrefWidth(600);
        textFlow.setPrefHeight(400);

        // Wrap the TextFlow in a ScrollPane to handle long content
        ScrollPane scrollPane = new ScrollPane(textFlow);
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);

        // Create the alert dialog
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Challenges Faced");
        alert.setHeaderText(null);
        alert.getDialogPane().setContent(scrollPane);

        // Show the alert dialog
        alert.showAndWait();
    }


    /**
     * Displays information about the application.
     */
    @FXML
    protected void showAbout() {
        String aboutInfo = "User Management Application\n" +
                "Version 1.0\n" +
                "Developed by Benjamin Siegel\n" +
                "Forced to make this by my professor.";

        showAlert("About", aboutInfo);
    }
}
