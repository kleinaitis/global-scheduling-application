package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import model.UserSession;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.ResourceBundle;
import static utility.GeneralUtilityFunctions.*;

/** This creates the LoginScreenController class which is used to log into the application.
 */
public class LoginScreenController implements Initializable {
    @FXML private Label titleTextLabel;
    @FXML private Label languageLabel;
    @FXML private Button loginButton;
    @FXML private Button exitButton;
    @FXML private RadioButton englishRadioButton;
    @FXML private RadioButton frenchRadioButton;
    @FXML private Label usernameLabel;
    @FXML private Label passwordLabel;
    @FXML private Label locationLabel;
    @FXML private TextField usernameText;
    @FXML private TextField passwordText;
    @FXML private TextField timeZoneText;

    /** This method is used to attempt to log the user into the application based on the provided credentials.
     * If the login is successful, the successful login attempt is logged, the user is directed to the main screen, and their upcoming appointments are checked.
     * If the login is unsuccessful, an information dialog displays an error message, and the unsuccessful login attempt is logged.
     *
    * @param event The login button is pressed by the user
    * @throws RuntimeException
    */
    public void onActionLogin(ActionEvent event) {
        try {
            String loginUsername = usernameText.getText();
            String loginPassword = passwordText.getText();
            databaseConnect();

            String sqlSelect = "SELECT * FROM users WHERE user_name = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setString(1, loginUsername);
            statement.setString(2, loginPassword);

            ResultSet resultSet = statement.executeQuery();
            UserSession.setUserName(loginUsername);

            if (resultSet.next()) {
                int userID = resultSet.getInt("User_ID");
                UserSession.setUserID(userID);

                resultSet.close();
                statement.close();

                logUserActivity("Successful", loginUsername);
                loadScene("/view/Main.fxml", "Scheduling Application", event);
                checkUpcomingAppointments(LocalDateTime.now());
            } else {
                showLocalizedInformationDialog("information.dialog.title", "information.dialog.header", "information.dialog.message", frenchRadioButton.isSelected());
                logUserActivity("Unsuccessful", loginUsername);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void setLanguageSelected() {
        Locale userLocale = Locale.getDefault();
        String userLanguage = userLocale.getLanguage();
        ZoneId userTimeZone = ZoneId.systemDefault();

        timeZoneText.setText(userTimeZone.toString());

        ResourceBundle bundle;
        if (userLanguage.equals("fr") || frenchRadioButton.isSelected()) {
            frenchRadioButton.setSelected(true);
            bundle = ResourceBundle.getBundle("resources.language_fr", Locale.FRENCH);
        } else {
            englishRadioButton.setSelected(true);
            bundle = ResourceBundle.getBundle("resources.language_en", Locale.ENGLISH);
        }
        titleTextLabel.setText(bundle.getString("title.label"));
        languageLabel.setText(bundle.getString("language.label"));
        usernameLabel.setText(bundle.getString("username.label"));
        passwordLabel.setText(bundle.getString("password.label"));
        locationLabel.setText(bundle.getString("location.label"));
        loginButton.setText(bundle.getString("login.button"));
        exitButton.setText(bundle.getString("exit.button"));
    }

    /** This method logs the user activity by recording the date/time, username, and login status in a log file.
     *
     * @param status   The status of the user activity (Successful or Unsuccessful)
     * @param username The username of the user performing the activity
     */
    private void logUserActivity(String status, String username) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("login_activity.txt", true))) {
            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            String logEntry = String.format("Date: %s | Time: %s | User: %s | Status: %s%n",
                    now.format(formatter), now.format(formatter), username, status);
            writer.write(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**  This method is used to exit the application. The user is asked to confirm their decision before the application is terminated.
     */
    @FXML
    public void onActionExit() {
        if (showLocalizedConfirmationDialog("confirmation.dialog.header", "confirmation.dialog.message", frenchRadioButton.isSelected())) {
            System.exit(0);
        }
    }

    /** Lambdas #4/5 - This method initializes the LoginScreenController class and sets the selected language based on the user's computer settings or the selected radio button. It also creates event handlers for the French and English radio buttons to update the language selection. The lambdas are used as event handlers here which allows for a more readable and concise way to handle each of the events.
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        setLanguageSelected();
        frenchRadioButton.setOnAction(event -> setLanguageSelected());
        englishRadioButton.setOnAction(event -> setLanguageSelected());
    }
}