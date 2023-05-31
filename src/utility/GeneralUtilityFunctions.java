package utility;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import model.Appointments;
import model.Customers;
import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

/** This creates the GeneralUtilityFunctions class which encapsulates common functionality and operations that are performed throughout the application.
 */
public class GeneralUtilityFunctions {

    //Database connection
    private static final String JDBC_URL = "jdbc:mysql://localhost/client_schedule?connectionTimeZone=SERVER";
    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final String USERNAME = "sqlUser";
    private static final String PASSWORD = "Passw0rd!";
    public static Connection connection;

    /** This function is used to connect to the database.
     */
    public static void databaseConnect() {
        try {
            Class.forName(DRIVER);
            connection = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** This function is used to disconnect from the database.
     */
    public static void databaseDisconnect() {
        try {
            connection.close();
        }
        catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This function displays a confirmation dialog box with the specified title and message.
     *
     * @param title the title of the dialog box.
     * @param message the message that is displayed in the dialog box.
     * @return true if the user clicks the "OK" button, false otherwise.
     */
    public static boolean showConfirmationDialog(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText("Confirmation Required");
        alert.setContentText(message);
        Optional<ButtonType> result = alert.showAndWait();
        return result.get() == ButtonType.OK;
    }

    /** This function displays a confirmation dialog box with a title and message based on the selected language of the application.
     *
     * @param title the title of the dialog box.
     * @param message the message that is displayed in the dialog box.
     * @param isFrenchSelected true if French is selected, false if English is selected.
     * @return true if the user clicks the "OK" button, false otherwise.
     */
    public static boolean showLocalizedConfirmationDialog(String title, String message, boolean isFrenchSelected) {
        ResourceBundle bundle;
        if (isFrenchSelected) {
            bundle = ResourceBundle.getBundle("resources.language_fr", Locale.FRENCH);
        } else {
            bundle = ResourceBundle.getBundle("resources.language_en", Locale.ENGLISH);
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(bundle.getString(title));
        alert.setHeaderText(bundle.getString("confirmation.dialog.header"));
        alert.setContentText(bundle.getString(message));

        ButtonType okButtonType = new ButtonType(bundle.getString("ok.button"), ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButtonType = new ButtonType(bundle.getString("cancel.button"), ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(okButtonType, cancelButtonType);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == okButtonType;
    }

    /** This function displays an information dialog box with the specified title, header, and message.
     *
     * @param title the title of the dialog box
     * @param header the header text displayed in the dialog box
     * @param message the message displayed in the dialog box
     */
    public static void showInformationDialog(String title, String header, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /** This function displays an information dialog box with a specified title, header, and message, based on the selected language of the application.
     *
     * @param title the title of the dialog box
     * @param header the header text displayed in the dialog box
     * @param message the message displayed in the dialog box
     * @param isFrenchSelected true if French is selected, false if English is selected.
     */
    public static void showLocalizedInformationDialog(String title, String header, String message, boolean isFrenchSelected) {
        Locale userLocale = Locale.getDefault();
        ResourceBundle bundle;

        if (isFrenchSelected || userLocale.getLanguage().equals("fr")) {
            bundle = ResourceBundle.getBundle("resources.language_fr", Locale.FRENCH);
        } else {
            bundle = ResourceBundle.getBundle("resources.language_en", Locale.ENGLISH);
        }
        String errorTitle = bundle.getString(title);
        String signInErrorHeader = bundle.getString(header);
        String invalidCredentialsMessage = bundle.getString(message);

        showInformationDialog(errorTitle, signInErrorHeader, invalidCredentialsMessage);
    }

    /** This function loads a new scene and sets it as the current scene in the window.
     *
     * @param location location of the FXML file to load
     * @param title title of the new window
     * @param actionEvent event that triggered the method
     * @throws IOException
     */
    public static void loadScene(String location, String title, ActionEvent actionEvent) throws IOException {
        Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        Parent root = FXMLLoader.load(GeneralUtilityFunctions.class.getResource(location));
        stage.setScene(new Scene(root));
        stage.setTitle(title);
        stage.show();
    }

    /** This function is used to create a list of time options based on the user's time zone.
     *
     * @return an observable list of time options.
     */
    public static ObservableList<String> populateTimeOptions() {
        ObservableList<String> timeOptions = FXCollections.observableArrayList();
        ZoneId estTimeZone = ZoneId.of("America/New_York");
        ZoneId userTimeZone = ZoneId.systemDefault();

        LocalTime startTime = LocalTime.of(7, 45);
        LocalTime endTimeET = LocalTime.of(22, 15);

        int intervalMinutes = 15;
        LocalDate currentDate = LocalDate.now(userTimeZone);
        ZonedDateTime startDateTimeET = ZonedDateTime.of(currentDate, startTime, estTimeZone);
        ZonedDateTime endDateTimeET = ZonedDateTime.of(currentDate, endTimeET, estTimeZone);

        ZonedDateTime startDateTimeUser = startDateTimeET.withZoneSameInstant(userTimeZone);
        ZonedDateTime endDateTimeUser = endDateTimeET.withZoneSameInstant(userTimeZone);

        while (!startDateTimeUser.toLocalTime().isAfter(endDateTimeUser.toLocalTime())) {
            timeOptions.add(startDateTimeUser.format(DateTimeFormatter.ofPattern("hh:mm a")));
            startDateTimeUser = startDateTimeUser.plusMinutes(intervalMinutes);
        }

        return timeOptions;
    }

    /** Lambdas #8/9 - This method sets up the table view for displaying appointments. These lambdas provide a way to dynamically generate cell values without the need for separate implementations.
     * @param tableView the table view to be set up.
     * @param appointmentIDColumn the column for displaying appointment IDs.
     * @param titleColumn the column for displaying appointment titles.
     * @param descriptionColumn the column for displaying appointment descriptions.
     * @param typeColumn the column for displaying appointment types.
     * @param startDateColumn the column for displaying appointment start date/times.
     * @param endDateColumn the column for displaying appointment end date/times.
     * @param apptCustomerIDColumn the column for displaying customer IDs.
     * @param locationColumn the column for displaying customer locations.
     * @param contactColumn the column for displaying contacts.
     * @param userIDColumn the column for displaying user IDs.
     * @param data the data in which the appointments come from.
     */
    public static void setupAppointmentsTableView(TableView<Appointments> tableView, TableColumn<Appointments, Integer> appointmentIDColumn, TableColumn<Appointments, String> titleColumn, TableColumn<Appointments, String> descriptionColumn, TableColumn<Appointments, String> typeColumn, TableColumn<Appointments, String> startDateColumn, TableColumn<Appointments, String> endDateColumn, TableColumn<Appointments, Integer> apptCustomerIDColumn, TableColumn<Appointments, String> locationColumn, TableColumn<Appointments, Integer> contactColumn, TableColumn<Appointments, Integer> userIDColumn, ObservableList<Appointments> data) {
        tableView.setItems(data);
        appointmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentDescription"));
        locationColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentLocation"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        apptCustomerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        contactColumn.setCellValueFactory(new PropertyValueFactory<>("contactID"));
        userIDColumn.setCellValueFactory(new PropertyValueFactory<>("userID"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        startDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime appointmentStart = cellData.getValue().getAppointmentStart();
            LocalDateTime localStart = appointmentStart.atZone(ZoneId.of("UTC")).withZoneSameLocal(ZoneId.systemDefault()).toLocalDateTime();
            return new SimpleStringProperty(localStart.format(formatter));
        });

        endDateColumn.setCellValueFactory(cellData -> {
            LocalDateTime appointmentEnd = cellData.getValue().getAppointmentEnd();
            LocalDateTime localEnd = appointmentEnd.atZone(ZoneId.of("UTC")).withZoneSameLocal(ZoneId.systemDefault()).toLocalDateTime();
            return new SimpleStringProperty(localEnd.format(formatter));
        });
    }

    /** Lambdas #10/11 - This method sets up the table view for displaying customers. These lambdas provide a way to dynamically generate cell values without the need for separate implementations.
     *
     * @param tableView  the table view to be set up.
     * @param customerIDColumn the column for displaying customers IDs.
     * @param customerNameColumn the column for displaying customers names.
     * @param customerPhoneColumn the column for displaying customers phone numbers.
     * @param customerAddressColumn the column for displaying customers addresses.
     * @param customerDivisionColumn the column for displaying the customers divisions.
     * @param customerPostalColumn the column for displaying customers postal codes.
     * @param customerCountryColumn the column for displaying customers countries.
     * @param data the data in which the customers come from.
     */
    public static void setupCustomersTableView(TableView tableView, TableColumn customerIDColumn, TableColumn customerNameColumn, TableColumn customerPhoneColumn, TableColumn customerAddressColumn, TableColumn<Customers, String> customerDivisionColumn, TableColumn customerPostalColumn, TableColumn<Customers, String> customerCountryColumn, ObservableList<Customers> data) {

        tableView.setItems(data);
        customerIDColumn.setCellValueFactory(new PropertyValueFactory<>("customerID"));
        customerNameColumn.setCellValueFactory(new PropertyValueFactory<>("customerName"));
        customerPhoneColumn.setCellValueFactory(new PropertyValueFactory<>("customerPhoneNumber"));
        customerAddressColumn.setCellValueFactory(new PropertyValueFactory<>("customerAddress"));

        customerDivisionColumn.setCellValueFactory(param -> {
            Customers customer = param.getValue();
            int divisionId = customer.getDivisionID();
            String divisionName = "";

            try {
                databaseConnect();
                String query = "SELECT Division FROM first_level_divisions WHERE Division_ID = ?";
                PreparedStatement divisionPS = connection.prepareStatement(query);
                divisionPS.setInt(1, divisionId);
                ResultSet resultSet = divisionPS.executeQuery();

                if (resultSet.next()) {
                    divisionName = resultSet.getString("Division");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new SimpleStringProperty(divisionName);
        });
        customerPostalColumn.setCellValueFactory(new PropertyValueFactory<>("customerPostalCode"));
        customerCountryColumn.setCellValueFactory(param -> {
            Customers customer = param.getValue();
            String country = "";

            try {
                int divisionId = customer.getDivisionID();
                String query = "SELECT countries.Country FROM first_level_divisions " +
                        "JOIN countries ON first_level_divisions.Country_ID = countries.Country_ID " +
                        "WHERE first_level_divisions.Division_ID = ?";
                PreparedStatement countryPS = connection.prepareStatement(query);
                countryPS.setInt(1, divisionId);
                ResultSet resultSet = countryPS.executeQuery();
                if (resultSet.next()) {
                    country = resultSet.getString("COUNTRY");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            return new SimpleStringProperty(country);
        });
    }

    /** This function is used to check the database to see if there is an upcoming appointment within 15 minutes of the user logging into the application.
     *
     * @param loginDateTime the date/time being checked against appointments in the database.
     */
    public static void checkUpcomingAppointments(LocalDateTime loginDateTime) {
        LocalDateTime fifteenMinutesAfterLogin = loginDateTime.plusMinutes(15);

        try {
            databaseConnect();
            String sqlSelect = "SELECT appointment_id, start FROM appointments " +
                    "WHERE start > ? AND start < ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);

            ZonedDateTime loginDateTimeUTC = loginDateTime.atZone(ZoneId.systemDefault()).withZoneSameLocal(ZoneOffset.UTC);
            ZonedDateTime fifteenMinutesAfterLoginUTC = fifteenMinutesAfterLogin.atZone(ZoneId.systemDefault()).withZoneSameLocal(ZoneOffset.UTC);

            statement.setTimestamp(1, Timestamp.valueOf(loginDateTimeUTC.toLocalDateTime()));
            statement.setTimestamp(2, Timestamp.valueOf(fifteenMinutesAfterLoginUTC.toLocalDateTime()));

            ResultSet resultSet = statement.executeQuery();
            boolean hasUpcomingAppointments = false;
            while (resultSet.next()) {
                hasUpcomingAppointments = true;
                String appointmentID = resultSet.getString("appointment_id");
                LocalDateTime appointmentDateTimeUTC = resultSet.getTimestamp("start").toLocalDateTime();

                ZonedDateTime appointmentDateTimeLocal = appointmentDateTimeUTC.atZone(ZoneOffset.UTC)
                        .withZoneSameLocal(ZoneId.systemDefault());

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formattedDateTime = appointmentDateTimeLocal.format(formatter);

                showInformationDialog("Scheduling Application", "Appointments Within 15 Minutes",
                        "There are appointments within 15 minutes.\nAppointment ID: " + appointmentID +
                                "\nAppointment Date/Time: " + formattedDateTime);
            }
            if (!hasUpcomingAppointments) {
                showInformationDialog("Scheduling Application", "No Upcoming Appointments",
                        "There are no upcoming appointments within 15 minutes.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

