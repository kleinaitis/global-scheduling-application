package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import model.Appointments;
import model.Contacts;
import model.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import static utility.AppointmentUtility.*;
import static utility.GeneralUtilityFunctions.*;

/** This creates the EditAppointmentController class which is used to update existing appointment's information.
 */
public class EditAppointmentController implements Initializable {
    @FXML private ComboBox<String> userIDComboBox;
    @FXML private DatePicker startDatePicker;
    @FXML private DatePicker endDatePicker;
    @FXML private ComboBox<String> startTimeComboBox;
    @FXML private ComboBox<String> endTimeComboBox;
    @FXML private ComboBox<String> customerComboBox;
    @FXML private TextField IDText;
    @FXML private TextField appointmentTitleText;
    @FXML private TextField appointmentDescriptionText;
    @FXML private TextField appointmentLocationText;
    @FXML private ComboBox<String> contactComboBox;
    @FXML private TextField appointmentTypeText;
    @FXML private ObservableList<String> timeOptions;

    /** This method updates the selected appointment in the database using user inputted values, and returns the user to the main screen if input validation succeeds.
     *
     * @param event Save button is pressed by the user
     */
    public void onActionSaveAppointment(ActionEvent event) {
        try {
            String appointmentType = appointmentTypeText.getText();
            String selectedCustomer = customerComboBox.getValue();
            String appointmentTitle = appointmentTitleText.getText();
            String appointmentDescription = appointmentDescriptionText.getText();
            String appointmentLocation = appointmentLocationText.getText();
            String selectedContact = contactComboBox.getValue();
            int contactID;
            int customerID;
            int apptID = Integer.parseInt(IDText.getText());

            LocalDateTime appointmentStartDateTime = getAppointmentStartDateTime(startDatePicker, startTimeComboBox);
            LocalDateTime appointmentEndDateTime = getAppointmentEndDateTime(endDatePicker, endTimeComboBox);

            if (!validateAppointmentTitle(appointmentTitle)) {
                return;
            }

            if (!validateAppointmentDescription(appointmentDescription)) {
                return;
            }

            if (!validateAppointmentLocation(appointmentLocation)) {
                return;
            }

            if (selectedContact != null) {
                contactID = Integer.parseInt(selectedContact.split(" - ")[0]);
            } else {
                showInformationDialog("Error!", "Data Entry Error", "Select which contact the appointment is with.");
                return;
            }

            if (!validateAppointmentType(appointmentType)) {
                return;
            }

            if (!validateAppointmentDateTime(appointmentStartDateTime, appointmentEndDateTime)) {
                return;
            }

            if (!isAppointmentWithinBusinessHours(appointmentStartDateTime, appointmentEndDateTime)) {
                showInformationDialog("Error!", "Invalid Appointment Time", "The current time selected is outside of business hours. All appointments must be scheduled between 8AM and 10PM EST.");
                return;
            }

            if (appointmentStartDateTime.isBefore(LocalDateTime.now())) {
                showInformationDialog("Error!", "Invalid Appointment Time", "The appointment start time is in the past.");
                return;
            }

            if (Duration.between(appointmentStartDateTime, appointmentEndDateTime).toMinutes() < 15) {
                showInformationDialog("Error!", "Invalid Appointment Duration", "The appointment duration must be at least 15 minutes.");
                return;
            }

            if (selectedCustomer != null) {
                customerID = Integer.parseInt(selectedCustomer.split(" - ")[0]);
            } else {
                showInformationDialog("Error!", "Data Entry Error", "Select a customer for the appointment.");
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

            if (isAppointmentOverlappingContact(contactID, appointmentStartDateTime, appointmentEndDateTime, apptID)) {
                showInformationDialog("Error!", "Appointment Overlapping", "The appointment day/time is overlapping with an existing appointment for the assigned contact.");
                return;
            }

            if (isAppointmentOverlappingCustomer(customerID, appointmentStartDateTime, appointmentEndDateTime, apptID)) {
                showInformationDialog("Error!", "Appointment Overlapping", "The appointment is overlapping with an existing appointment for this customer.");
                return;
            }

            String updateStatement = "UPDATE APPOINTMENTS SET Title = ?, Description = ?, Location = ?, Type = ?, " +
                    "Start = ?, End = ?, Last_Update = ?, Last_Updated_By = ?, Contact_ID = ?, Customer_ID = ?, User_ID = ? " +
                    "WHERE Appointment_ID = ?";

            PreparedStatement statement = connection.prepareStatement(updateStatement);
            statement.setString(1, appointmentTitleText.getText());
            statement.setString(2, appointmentDescriptionText.getText());
            statement.setString(3, appointmentLocationText.getText());
            statement.setString(4, appointmentTypeText.getText());
            statement.setTimestamp(5, Timestamp.valueOf(appointmentStartDateTime));
            statement.setTimestamp(6, Timestamp.valueOf(appointmentEndDateTime));
            statement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(8, UserSession.getUserName());
            statement.setInt(9, contactID);
            statement.setInt(10, customerID);
            statement.setInt(11, selectedUserID);
            statement.setInt(12, apptID);

            statement.executeUpdate();

            loadScene("/view/Main.fxml", "Scheduling Application", event);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    /** This method returns the application to the Main.fxml screen. No information that was input is saved.*
     *
     * @param event user clicks the Cancel button
     */
    public void onActionCancel(ActionEvent event) throws IOException {
        loadScene("/view/Main.fxml", "Scheduling Application", event);
    }

    /** Lambda #2 - This method takes the selected appointment from the main screen and sets the corresponding text fields on the EditAppointmentController.fxml screen to display the appointment's details. The lambda allows for a concise way to take each contact object from the contactsObservableList and append the ID and name to it.
     *
     * @param appointment the appointment that was selected on main screen whenever Edit Appointment was clicked by the user
     */
    public void setAppointment(Appointments appointment) {
        try {
            if (appointment != null) {
                DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

                LocalDateTime apptStartDateTime = LocalDateTime.from(appointment.getAppointmentStart());
                startDatePicker.setValue(apptStartDateTime.toLocalDate());
                startTimeComboBox.setValue(apptStartDateTime.toLocalTime().format(timeFormatter));

                LocalDateTime apptEndDateTime = LocalDateTime.from(appointment.getAppointmentEnd());
                endDatePicker.setValue(apptEndDateTime.toLocalDate());
                endTimeComboBox.setValue(apptEndDateTime.toLocalTime().format(timeFormatter));

                ZoneId utcTimeZone = ZoneOffset.UTC;
                ZoneId userTimeZone = ZoneId.systemDefault();
                ZonedDateTime startDateTimeUserZone = apptStartDateTime.atZone(utcTimeZone).withZoneSameLocal(userTimeZone);
                ZonedDateTime endDateTimeUserZone = apptEndDateTime.atZone(utcTimeZone).withZoneSameLocal(userTimeZone);

                startDatePicker.setValue(startDateTimeUserZone.toLocalDate());
                endDatePicker.setValue(endDateTimeUserZone.toLocalDate());
                startTimeComboBox.setValue(startDateTimeUserZone.toLocalTime().format(timeFormatter));
                endTimeComboBox.setValue(endDateTimeUserZone.toLocalTime().format(timeFormatter));

                ObservableList<String> customerOptions = getCustomersFromDatabase();
                customerComboBox.setItems(customerOptions);

                for (String customerOption : customerOptions) {
                    if (customerOption.contains(String.valueOf(appointment.getCustomerID()))) {
                        customerComboBox.setValue(customerOption);
                        break;
                    }
                }

                String userIDAndName = appointment.getUserID() + " - " + getUserNameByID(appointment.getUserID());
                userIDComboBox.setValue(userIDAndName);

                ObservableList<Contacts> contactsObservableList = getAllContacts();
                ObservableList<String> allContactsNames = FXCollections.observableArrayList();
                String contactIDAndName = "";

                contactsObservableList.forEach(contact -> allContactsNames.add(contact.getId() + " - " + contact.getContactName()));
                contactComboBox.setItems(allContactsNames);

                for (Contacts contact : contactsObservableList) {
                    if (appointment.getContactID() == contact.getId()) {
                        contactIDAndName = contact.getId() + " - " + contact.getContactName();
                    }
                }

                IDText.setText(String.valueOf(appointment.getAppointmentID()));
                appointmentTitleText.setText(appointment.getAppointmentTitle());
                appointmentDescriptionText.setText(appointment.getAppointmentDescription());
                appointmentLocationText.setText(appointment.getAppointmentLocation());
                appointmentTypeText.setText(appointment.getAppointmentType());

                contactComboBox.setValue(contactIDAndName);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method initializes the EditAppointmentController class, and sets the options within the start date/time, end date/time, contacts, and customers combo boxes on the screen.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseConnect();
        timeOptions = populateTimeOptions();
        startTimeComboBox.setItems(timeOptions);
        endTimeComboBox.setItems(timeOptions);

        try {
            ObservableList<String> customerOptions = getCustomersFromDatabase();
            customerComboBox.setItems(customerOptions);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            ObservableList<String> contactOptions = getContactNameAndIDFromDatabase();
            contactComboBox.setItems(contactOptions);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            ObservableList<String> userOptions = getUsersFromDatabase();
            userIDComboBox.setItems(userOptions);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}