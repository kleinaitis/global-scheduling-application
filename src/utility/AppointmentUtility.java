package utility;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import model.Appointments;
import model.Contacts;

import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;


import static utility.GeneralUtilityFunctions.*;
import static utility.GeneralUtilityFunctions.showInformationDialog;

/** This creates the AppointmentUtility class which encapsulates common functionality and operations that are performed on appointments.
 */
public class AppointmentUtility {

    public static final ZoneId utcZone = ZoneId.of("Etc/UTC");
    public static final ZoneId localZone = ZoneId.systemDefault();


    /** This method validates the user's input on the appointment title text field.
     *
     * @param appointmentTitle the appointment title to be validated.
     * @return true if the appointment title is valid, false otherwise.
     */
    public static boolean validateAppointmentTitle(String appointmentTitle) {
        if (appointmentTitle.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Appointment title cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method validates the user's input on the appointment description text field.
     *
     * @param appointmentDescription the appointment description to be validated.
     * @return true if the appointment description is valid, false otherwise.
     */
    public static boolean validateAppointmentDescription(String appointmentDescription) {
        if (appointmentDescription.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Appointment description cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method validates the user's input on the appointment location text field.
     *
     * @param appointmentLocation the appointment location to be validated.
     * @return true if the appointment location is valid, false otherwise.
     */
    public static boolean validateAppointmentLocation(String appointmentLocation) {
        if (appointmentLocation.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Appointment location cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method validates the user's input on the appointment type text field.
     *
     * @param appointmentType the appointment type to be validated.
     * @return true if the appointment type is valid, false otherwise.
     */
    public static boolean validateAppointmentType(String appointmentType) {
        if (appointmentType.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Appointment type cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method validates the appointment start date/time and end date/time to ensure that both are present, and the start date/time is before the end date/time.
     *
     * @param startDateTime the appointment start date/time to be validated.
     * @param endDateTime the appointment end date/time to be validated.
     * @return true if the appointment date/time is valid, false otherwise.
     */
    public static boolean validateAppointmentDateTime(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            showInformationDialog("Error!", "Data Entry Error", "Please select both the start and end date/time for the appointment.");
            return false;
        }
        if (startDateTime.isAfter(endDateTime)) {
            showInformationDialog("Error!", "Data Entry Error", "Start date/time must be before end date/time.");
            return false;
        }
        return true;
    }

    /** This method checks the database to see if the customer has any existing appointments.
     *
     * @param customerID ID of the customer to check for appointments.
     * @return true if the customer has appointments, false otherwise.
     * @throws SQLException
     */
    public static boolean doesCustomerHaveAppointments(int customerID) throws SQLException {
        String sqlSelect = "SELECT COUNT(*) FROM APPOINTMENTS WHERE Customer_ID = ?";
        PreparedStatement statement = connection.prepareStatement(sqlSelect);
        statement.setInt(1, customerID);
        ResultSet rs = statement.executeQuery();

        if (rs.next()) {
            int count = rs.getInt(1);
            return count > 0;
        }

        return false;
    }

    /** This method checks to see if the appointment's start date/time and end date/time are within business hours.
     * Business hours are set from 8AM - 10PM EST and appointments cannot exist outside of those hours.
     * The hours are adjusted to reflect the user's local time as all appointments/times are shown in the user's local time.
     * If the user is in EST already, the hours are shown as 8AM-10PM EST without showing the additional time conversion.
     *
     * @param startDateTime the appointment start date/time to be validated.
     * @param endDateTime the appointment end date/time to be validated.
     * @return true if the appointment is within business hours, false otherwise.
     */
    public static boolean isAppointmentWithinBusinessHours(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        ZoneId estTimeZone = ZoneId.of("America/New_York");
        ZoneId userTimeZone = ZoneId.systemDefault();

        LocalTime businessStartTime = LocalTime.of(8, 0);
        LocalTime businessEndTime = LocalTime.of(22, 0);

        ZonedDateTime userStartDateTime = startDateTime.atZone(userTimeZone).withZoneSameInstant(estTimeZone);
        ZonedDateTime userEndDateTime = endDateTime.atZone(userTimeZone).withZoneSameInstant(estTimeZone);

        boolean withinBusinessHours =  !(userStartDateTime.toLocalTime().isBefore(businessStartTime) || userEndDateTime.toLocalTime().isAfter(businessEndTime));

        if (!withinBusinessHours) {
            DateTimeFormatter userTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(userTimeZone);
            DateTimeFormatter businessTimeFormatter = DateTimeFormatter.ofPattern("h:mm a").withZone(estTimeZone);
            String businessHoursEST = String.format("%s to %s EST", businessStartTime.format(businessTimeFormatter), businessEndTime.format(businessTimeFormatter));

            ZonedDateTime userStartBusinessHours = userStartDateTime.with(businessStartTime);
            ZonedDateTime userEndBusinessHours = userStartDateTime.with(businessEndTime);

            String businessHoursUserZone = String.format("%s to %s local time", userStartBusinessHours.format(userTimeFormatter), userEndBusinessHours.format(userTimeFormatter));

            String errorMessage = String.format("The current time selected is outside the business hours of %s. All appointments must be scheduled from %s.", businessHoursEST, businessHoursUserZone);
            if (userTimeZone.equals(estTimeZone)) {
                showInformationDialog("Error!", "Invalid Appointment Time", "The current time selected is outside the business hours of " + businessHoursEST + ".");
            } else {
                showInformationDialog("Error!", "Invalid Appointment Time", errorMessage);
            }
        }
        return withinBusinessHours;
    }

    /** This method is used to retrieve customer names from the database.
     *
     * @return the names of each customer in the database.
     * @throws SQLException
     */
    public static ObservableList<String> getCustomersFromDatabase() throws SQLException {
        ObservableList<String> customerOptions = FXCollections.observableArrayList();

        String sqlSelect = "SELECT Customer_ID, Customer_Name FROM CUSTOMERS";
        PreparedStatement statement = connection.prepareStatement(sqlSelect);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int customerID = resultSet.getInt("Customer_ID");
            String customerName = resultSet.getString("Customer_Name");

            String customerIDAndName = customerID + " - " + customerName;
            customerOptions.add(customerIDAndName);
        }

        resultSet.close();
        statement.close();

        return customerOptions;
    }

    /** This method is used to retrieve all appointments from the database.
     *
     * @return an observable list of all appointments.
     */
    public static ObservableList<Appointments> getAllAppointments() {
        ObservableList<Appointments> appointmentsObservableList = FXCollections.observableArrayList();

        try {
             PreparedStatement sqlSelect = connection.prepareStatement("SELECT * FROM appointments");
             ResultSet resultSet = sqlSelect.executeQuery(); {

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String appointmentTitle = resultSet.getString("Title");
                String appointmentDescription = resultSet.getString("Description");
                String appointmentLocation = resultSet.getString("Location");
                String appointmentType = resultSet.getString("Type");
                LocalDateTime appointmentStart = resultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime appointmentEnd = resultSet.getTimestamp("End").toLocalDateTime();
                int customerID = resultSet.getInt("Customer_ID");
                int userID = resultSet.getInt("User_ID");
                int contactID = resultSet.getInt("Contact_ID");

                ZonedDateTime utcTime = appointmentStart.atZone(utcZone).withZoneSameLocal(localZone);
                LocalDateTime localUserTimeStart = utcTime.toLocalDateTime();

                ZonedDateTime utcTimes = appointmentEnd.atZone(utcZone).withZoneSameLocal(localZone);
                LocalDateTime localUserTimeEnd = utcTimes.toLocalDateTime();

                Appointments appointment = new Appointments(appointmentID, appointmentTitle, appointmentDescription,
                        appointmentLocation, appointmentType, localUserTimeStart, localUserTimeEnd, customerID, userID, contactID);
                appointmentsObservableList.add(appointment);
            }
        }
        }catch (SQLException e) {
            e.printStackTrace();
        }
        return appointmentsObservableList;
    }

    /** This method is used to check the database to see if the selected customer has an appointment that would cause overlap before saving updates to the appointment.
     *
     * @param customerID The ID of the customer to retrieve.
     * @param startDateTime the appointment start date/time.
     * @param endDateTime the appointment end date/time.
     * @param currentAppointmentID the appointment ID of the current appointment
     * @return true if there is an overlapping appointment, false otherwise.
     * @throws SQLException
     */
    public static boolean isAppointmentOverlappingCustomer(int customerID, LocalDateTime startDateTime, LocalDateTime endDateTime, int currentAppointmentID) throws SQLException {
        String query = "SELECT * FROM APPOINTMENTS WHERE (End > ? AND Start < ?) AND Customer_ID = ? AND Appointment_ID != ?";
        PreparedStatement statement = connection.prepareStatement(query);

        ZoneId userTimeZone = ZoneId.systemDefault();
        ZoneId utcTimeZone = ZoneOffset.UTC;

        ZonedDateTime convertedToUtcStartDateTime = startDateTime.atZone(userTimeZone).withZoneSameLocal(utcTimeZone);
        ZonedDateTime convertedToUtcEndDateTime = endDateTime.atZone(userTimeZone).withZoneSameLocal(utcTimeZone);

        statement.setTimestamp(1, Timestamp.valueOf(convertedToUtcStartDateTime.toLocalDateTime()));
        statement.setTimestamp(2, Timestamp.valueOf(convertedToUtcEndDateTime.toLocalDateTime()));
        statement.setInt(3, customerID);
        statement.setInt(4, currentAppointmentID);

        ResultSet resultSet = statement.executeQuery();

        boolean overlapping = false;

        while (resultSet.next()) {
            LocalDateTime existingStartDateTime = resultSet.getTimestamp("Start").toLocalDateTime();
            LocalDateTime existingEndDateTime = resultSet.getTimestamp("End").toLocalDateTime();

            ZonedDateTime existingStartUtcDateTime = existingStartDateTime.atZone(utcTimeZone);
            ZonedDateTime existingEndUtcDateTime = existingEndDateTime.atZone(utcTimeZone);

            if (convertedToUtcStartDateTime.isBefore(existingEndUtcDateTime) && convertedToUtcEndDateTime.isAfter(existingStartUtcDateTime)) {
                overlapping = true;
                break;
            }
        }

        resultSet.close();
        statement.close();
        return overlapping;
    }

    /** This method is used to check the database to see if the selected customer has an existing appointment that would overlap with the new appointment.
     *
     * @param customerID The ID of the customer to retrieve.
     * @param startDateTime the appointment start date/time.
     * @param endDateTime the appointment end date/time.
     * @return true if there is an overlapping appointment, false otherwise.
     * @throws SQLException
     */
    public static boolean isAppointmentOverlappingCustomer(int customerID, LocalDateTime startDateTime, LocalDateTime endDateTime) throws SQLException {
        String query = "SELECT * FROM APPOINTMENTS WHERE Customer_ID = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, customerID);
        ResultSet resultSet = statement.executeQuery();

        boolean overlapping = false;

        while (resultSet.next()) {
            LocalDateTime existingStartDateTime = resultSet.getTimestamp("Start").toLocalDateTime();
            LocalDateTime existingEndDateTime = resultSet.getTimestamp("End").toLocalDateTime();

            ZonedDateTime convertedExistingStartDateTime = existingStartDateTime.atZone(ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault());
            ZonedDateTime convertedExistingEndDateTime = existingEndDateTime.atZone(ZoneOffset.UTC).withZoneSameLocal(ZoneId.systemDefault());

            if (startDateTime.isBefore(convertedExistingEndDateTime.toLocalDateTime()) && endDateTime.isAfter(convertedExistingStartDateTime.toLocalDateTime())) {
                overlapping = true;
                break;
            }
        }
        resultSet.close();
        statement.close();
        return overlapping;
    }

    /** This method is used to retrieve a list of contacts that are within the database.
     *
     * @return an observable list of contacts within the database that contains their contact ID, contact name, and email.
     * @throws SQLException
     */
    public static ObservableList<Contacts> getAllContacts() throws SQLException {
        ObservableList<Contacts> contactsObservableList = FXCollections.observableArrayList();
        String sql = "SELECT * from contacts";
        PreparedStatement statement = connection.prepareStatement(sql);
        ResultSet rs = statement.executeQuery();
        while (rs.next()) {
            int contactID = rs.getInt("Contact_ID");
            String contactName = rs.getString("Contact_Name");
            String contactEmail = rs.getString("Email");
            Contacts contact = new Contacts(contactID, contactName, contactEmail);
            contactsObservableList.add(contact);
        }
        return contactsObservableList;
    }

    /** This method is used to get a contact's ID and name formatted with a dash in between.
     *
     * @return a list of formatted contacts within the database (e.g. "3 - Jane Doe")
     * @throws SQLException
     */
    public static ObservableList<String> getContactNameAndIDFromDatabase() throws SQLException {
        ObservableList<String> contactOptions = FXCollections.observableArrayList();

        String query = "SELECT Contact_ID, Contact_Name FROM Contacts";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet rs = statement.executeQuery();

        while (rs.next()) {
            int contactID = rs.getInt("Contact_ID");
            String contactName = rs.getString("Contact_Name");
            String contactOption = contactID + " - " + contactName;
            contactOptions.add(contactOption);
        }

        return contactOptions;
    }

    /** This method is used to retrieve the start date and time of an appointment from the DatePicker and ComboBox inputs.
     *
     * @param startDatePicker the DatePicker that holds the selected appointment start date.
     * @param startTimeComboBox the ComboBox that holds the selected appointment start time.
     * @return the LocalDateTime object that holds the start date and time of the appointment.
     */
    public static LocalDateTime getAppointmentStartDateTime(DatePicker startDatePicker, ComboBox<String> startTimeComboBox) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        LocalDate appointmentDate = startDatePicker.getValue();
        if (appointmentDate == null) {
            return null;
        }

        String appointmentStartTime = startTimeComboBox.getValue();
        if (appointmentStartTime == null || appointmentStartTime.isEmpty()) {
            return null;
        }

        LocalTime appointmentLocalStartTime;
        try {
            appointmentLocalStartTime = LocalTime.parse(appointmentStartTime.toUpperCase(), timeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }

        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentLocalStartTime);

        return appointmentDateTime;
    }

    /** This method is used to retrieve the end date and time of an appointment from the DatePicker and ComboBox inputs.
     *
     * @param endDatePicker the DatePicker that holds the selected appointment end date.
     * @param endTimeComboBox the ComboBox that holds the selected appointment end time.
     * @return the LocalDateTime object that holds the end date and time of the appointment.
     */
    public static LocalDateTime getAppointmentEndDateTime(DatePicker endDatePicker, ComboBox<String> endTimeComboBox) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");

        LocalDate appointmentEndDate = endDatePicker.getValue();
        if (appointmentEndDate == null) {
            return null;
        }

        String appointmentEndTime = endTimeComboBox.getValue();
        if (appointmentEndTime == null || appointmentEndTime.isEmpty()) {
            return null;
        }

        LocalTime appointmentLocalEndTime;
        try {
            appointmentLocalEndTime = LocalTime.parse(appointmentEndTime.toUpperCase(), timeFormatter);
        } catch (DateTimeParseException e) {
            return null;
        }

        LocalDateTime appointmentEndDateTime = LocalDateTime.of(appointmentEndDate, appointmentLocalEndTime);

        return appointmentEndDateTime;
    }

    /** This method is used to check the database to see if the selected contact has an existing appointment that would overlap with the new appointment.
     *
     * @param contactID The ID of the contact to retrieve.
     * @param appointmentStartDateTime the appointment start date/time.
     * @param appointmentEndDateTime the appointment end date/time.
     * @return true if there is an overlapping appointment, false otherwise.
     * @throws SQLException
     */
    public static boolean isAppointmentOverlappingContact(int contactID, LocalDateTime appointmentStartDateTime, LocalDateTime appointmentEndDateTime) throws SQLException {
        ZoneId userTimeZone = ZoneId.systemDefault();
        ZoneId utcTimeZone = ZoneOffset.UTC;

        ZonedDateTime convertedToUtcStartDateTime = appointmentStartDateTime.atZone(userTimeZone).withZoneSameLocal(utcTimeZone);
        ZonedDateTime convertedToUtcEndDateTime = appointmentEndDateTime.atZone(userTimeZone).withZoneSameLocal(utcTimeZone);

        String sqlSelect = "SELECT * FROM APPOINTMENTS WHERE Contact_ID = ? AND NOT (End <= ? OR Start >= ?)";
        PreparedStatement statement = connection.prepareStatement(sqlSelect);
        statement.setInt(1, contactID);
        statement.setTimestamp(2, Timestamp.valueOf(convertedToUtcStartDateTime.toLocalDateTime()));
        statement.setTimestamp(3, Timestamp.valueOf(convertedToUtcEndDateTime.toLocalDateTime()));
        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    /** This method is used to check the database to see if the selected contact has an existing appointment that would overlap with the new appointment.
     *
     * @param contactID The ID of the contact to retrieve.
     * @param appointmentStartDateTime the appointment start date/time.
     * @param appointmentEndDateTime the appointment end date/time.
     * @param currentAppointmentID the appointment id of the current appointment
     * @return true if there is an overlapping appointment, false otherwise.
     * @throws SQLException
     */
    public static boolean isAppointmentOverlappingContact(int contactID, LocalDateTime appointmentStartDateTime, LocalDateTime appointmentEndDateTime, int currentAppointmentID) throws SQLException {
        ZoneId userTimeZone = ZoneId.systemDefault();
        ZoneId utcTimeZone = ZoneOffset.UTC;

        ZonedDateTime convertedToUtcStartDateTime = appointmentStartDateTime.atZone(userTimeZone).withZoneSameLocal(utcTimeZone);
        ZonedDateTime convertedToUtcEndDateTime = appointmentEndDateTime.atZone(userTimeZone).withZoneSameLocal(utcTimeZone);

        String sqlSelect = "SELECT * FROM APPOINTMENTS WHERE Contact_ID = ? AND (End > ? AND Start < ?) AND Appointment_ID != ?";
        PreparedStatement statement = connection.prepareStatement(sqlSelect);
        statement.setInt(1, contactID);
        statement.setTimestamp(2, Timestamp.valueOf(convertedToUtcStartDateTime.toLocalDateTime()));
        statement.setTimestamp(3, Timestamp.valueOf(convertedToUtcEndDateTime.toLocalDateTime()));
        statement.setInt(4, currentAppointmentID);
        ResultSet rs = statement.executeQuery();

        return rs.next();
    }

    /** This function is used to retrieve the users from the database.
     *
     * @return an observable list that contains each user's ID and name.
     * @throws SQLException
     */
    public static ObservableList<String> getUsersFromDatabase() throws SQLException {
        ObservableList<String> userOptions = FXCollections.observableArrayList();

        String sqlQuery = "SELECT user_ID, user_name FROM users";
        PreparedStatement statement = connection.prepareStatement(sqlQuery);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            int userID = resultSet.getInt("user_ID");
            String userName = resultSet.getString("user_name");
            String userOption = userID + " - " + userName;
            userOptions.add(userOption);
        }
        return userOptions;
    }

    /** This function is used to retrieve a user's name based on their user ID from the database.
     *
     * @return the name of the user.
     * @throws SQLException
     */
    public static String getUserNameByID(int userID) throws SQLException {
        String userName = null;

        String sqlSelect = "SELECT User_Name FROM USERS WHERE User_ID = ?";
        PreparedStatement statement = connection.prepareStatement(sqlSelect);
        statement.setInt(1, userID);
        ResultSet resultSet = statement.executeQuery();

        if (resultSet.next()) {
            userName = resultSet.getString("User_Name");
        }

        resultSet.close();
        statement.close();

        return userName;
    }

}
