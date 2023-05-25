package controller;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import model.UserSession;
import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.util.ResourceBundle;

import static utility.AppointmentUtility.*;
import static utility.GeneralUtilityFunctions.*;

/** This creates the AddAppointmentController class which is used to create appointments for customers.
 */
public class AddAppointmentController implements Initializable {
    @FXML private ComboBox<String> userIDComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private ComboBox<String> endTimeComboBox;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private TextField appointmentTitleText;
    @FXML private TextField appointmentDescriptionText;
    @FXML private TextField appointmentLocationText;
    @FXML private ComboBox<String> contactComboBox;
    @FXML private TextField appointmentTypeText;

    String userName = UserSession.getUserName();

    /** This method saves a new appointment to the database using user inputted values, and returns the user to the main screen if input validation succeeds.
     *
     * @param event Save button is pressed by the user
     * @throws SQLException
     * @throws IOException
     */
    public void onActionSaveAppointment(ActionEvent event) throws SQLException, IOException {
        String appointmentTitle = appointmentTitleText.getText();
        if (!validateAppointmentTitle(appointmentTitle)) {
            return;
        }

        String appointmentDescription = appointmentDescriptionText.getText();
        if (!validateAppointmentDescription(appointmentDescription)) {
            return;
        }

        String appointmentLocation = appointmentLocationText.getText();
        if (!validateAppointmentLocation(appointmentLocation)) {
            return;
        }

        String selectedContact = contactComboBox.getValue();
        int contactID;
        if (selectedContact != null) {
            contactID = Integer.parseInt(selectedContact.split(" - ")[0]);
        } else {
            showInformationDialog("Error!", "Data Entry Error", "Select which contact the appointment is with.");
            return;
        }

        String appointmentType = appointmentTypeText.getText();
        if (!validateAppointmentType(appointmentType)) {
            return;
        }

        LocalDateTime appointmentStartDateTime = getAppointmentStartDateTime(startDatePicker, startTimeComboBox);
        LocalDateTime appointmentEndDateTime = getAppointmentStartDateTime(endDatePicker, endTimeComboBox);

        if (!validateAppointmentDateTime(appointmentStartDateTime, appointmentEndDateTime)) {
            return;
        }

        if (!isAppointmentWithinBusinessHours(appointmentStartDateTime, appointmentEndDateTime)) {
            return;
        }

        if (appointmentStartDateTime.isBefore(LocalDateTime.now())) {
            showInformationDialog("Error!", "Invalid Appointment Time", "The appointment start time cannot be in the past.");
            return;
        }

        if (Duration.between(appointmentStartDateTime, appointmentEndDateTime).toMinutes() < 15) {
            showInformationDialog("Error!", "Invalid Appointment Duration", "The appointment duration must be at least 15 minutes.");
            return;
        }

        String selectedCustomer = customerComboBox.getValue();
        int customerID;
        if (selectedCustomer != null) {
            customerID = Integer.parseInt(selectedCustomer.split(" - ")[0]);
        } else {
            showInformationDialog("Error!", "Data Entry Error", "Select which customer the appointment is with.");
            return;
        }

        int selectedUserID;
        String selectedUser = userIDComboBox.getValue();
        if (selectedUser != null) {
            selectedUserID = Integer.parseInt(selectedUser.split(" - ")[0]);
        } else {
            showInformationDialog("Error!", "Data Entry Error", "User drop down cannot be empty.");
            return;
        }

        if (isAppointmentOverlappingContact(contactID, appointmentStartDateTime, appointmentEndDateTime)) {
            showInformationDialog("Error!", "Appointment Overlapping", "The appointment is overlapping with an existing appointment for the assigned contact.");
            return;
        }

        if (isAppointmentOverlappingCustomer(customerID, appointmentStartDateTime, appointmentEndDateTime)) {
            showInformationDialog("Error!", "Appointment Overlapping", "The appointment is overlapping with an existing appointment for the customer.");
            return;
        }

        String sqlInsert = "INSERT INTO APPOINTMENTS (Title, Description, Location, Type, Start, End, Create_Date, Created_By, Last_Update, Last_Updated_By, Customer_ID, User_ID, Contact_ID) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";

        PreparedStatement statement = connection.prepareStatement(sqlInsert);
        statement.setString(1, appointmentTitle);
        statement.setString(2, appointmentDescription);
        statement.setString(3, appointmentLocation);
        statement.setString(4, appointmentType);
        statement.setTimestamp(5, Timestamp.valueOf(appointmentStartDateTime));
        statement.setTimestamp(6, Timestamp.valueOf(appointmentEndDateTime));
        statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        statement.setString(8, userName);
        statement.setTimestamp(9, Timestamp.valueOf(LocalDateTime.now()));
        statement.setString(10, userName);
        statement.setInt(11, customerID);
        statement.setInt(12, selectedUserID);
        statement.setInt(13, contactID);

        statement.execute();
        loadScene("/view/Main.fxml", "Scheduling Application", event);
        databaseDisconnect();
    }

    /** This method returns the application to the Main.fxml screen. No information that was input is saved.
     *
     * @param event user clicks the Cancel button
     */
    public void onActionCancel(ActionEvent event) throws IOException {
        loadScene("/view/Main.fxml", "Scheduling Application", event);
    }

    /** This method initializes the AddAppointmentController class, and sets the options within the start date/time, end date/time, contacts, and customers combo boxes on the screen.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseConnect();
        ObservableList<String> timeOptions = populateTimeOptions();
        startTimeComboBox.setItems(timeOptions);
        endTimeComboBox.setItems(timeOptions);

        try {
            ObservableList<String> contactOptions = getContactNameAndIDFromDatabase();
            contactComboBox.setItems(contactOptions);

            ObservableList<String> customerOptions = getCustomersFromDatabase();
            customerComboBox.setItems(customerOptions);

            ObservableList<String> userOptions = getUsersFromDatabase();
            userIDComboBox.setItems(userOptions);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
