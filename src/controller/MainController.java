package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import model.Appointments;
import model.Customers;
import java.io.IOException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static utility.AppointmentUtility.doesCustomerHaveAppointments;
import static utility.AppointmentUtility.getAllAppointments;
import static utility.CustomerUtility.*;
import static utility.ReportsUtility.*;
import static utility.GeneralUtilityFunctions.*;

/** This creates the MainController class which handles user interactions, manages the UI components, and performs various operations related to customers, appointments, and reports.
 */
public class MainController implements Initializable {
    @FXML private RadioButton canadaRadioButton;
    @FXML private RadioButton usRadioButton;
    @FXML private RadioButton ukRadioButton;
    @FXML private RadioButton allCustomersRadioButton;
    @FXML private TableView allReportsTableView;
    @FXML private TableColumn<Appointments, Integer> reportApptIDCol;
    @FXML private TableColumn<Appointments, String> reportTitleCol;
    @FXML private TableColumn<Appointments, String> reportTypeCol;
    @FXML private TableColumn<Appointments, String> reportDescriptionCol;
    @FXML private TableColumn<Appointments, LocalDateTime> reportStartDateCol;
    @FXML private TableColumn<Appointments, LocalDateTime> reportEndDateCol;
    @FXML private TableColumn<Appointments, Integer> reportApptCustomerIDCol;
    @FXML private RadioButton appointmentByMonthRadioButton;
    @FXML private RadioButton appointmentByTypeRadioButton;
    @FXML private Label reportResultLabel;
    @FXML private ComboBox<String> reportsComboBox;
    @FXML private RadioButton appointmentsScheduleRadioButton;
    @FXML private RadioButton appointmentsByCountryRadioButton;
    @FXML private TableView<Customers> allCustomersTableView;
    @FXML private TableColumn<?, ?> customerIDColumn;
    @FXML private TableColumn<?, ?> customerNameColumn;
    @FXML private TableColumn<?, ?> customerPhoneColumn;
    @FXML private TableColumn<?, ?> customerAddressColumn;
    @FXML private TableColumn<Customers, String> customerDivisionColumn;
    @FXML private TableColumn<?, ?> customerPostalColumn;
    @FXML private TableColumn<Customers, String> customerCountryColumn;
    @FXML private RadioButton currentWeekRBAppts;
    @FXML private RadioButton currentMonthRBAppts;
    @FXML private RadioButton allAppointmentsRB;
    @FXML private TableView<Appointments> allAppointmentsTableView;
    @FXML private TableColumn<Appointments, Integer> apptIDCol;
    @FXML private TableColumn<Appointments, String> titleCol;
    @FXML private TableColumn<Appointments, String> descriptionCol;
    @FXML private TableColumn<Appointments, String>locationCol;
    @FXML private TableColumn<Appointments, Integer> contactCol;
    @FXML private TableColumn<Appointments, String> typeCol;
    @FXML private TableColumn<Appointments, String> startDateCol;
    @FXML private TableColumn<Appointments, String> endDateCol;
    @FXML private TableColumn<Appointments, Integer> apptCustomerIDCol;
    @FXML private TableColumn<Appointments, Integer> userIDCol;

    private Parent scene;

    /** This method loads AddAppointment.fxml which allows the user to schedule a new appointment.
     *
     * @param event
     * @throws IOException
     */
    public void onActionAddAppointment(ActionEvent event) throws IOException {
        loadScene("/view/AddAppointment.fxml", "Add Appointment", event);
    }

    /** This method loads EditAppointment.fxml which allows the user to modify the selected appointment's details.
     *
     * @param event user clicks the Edit Appointment button within the Appointments tab.
     */
    public void onActionEditAppointment(ActionEvent event) {
        try {
            Appointments selectedAppointment = allAppointmentsTableView.getSelectionModel().getSelectedItem();
            if (selectedAppointment == null) {
                showInformationDialog("Warning!", "No Appointment Selected", "Select which appointment you'd like to edit and try again.");
                return;
            }
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditAppointment.fxml"));
            scene = loader.load();
            EditAppointmentController editAppointmentController = loader.getController();
            editAppointmentController.setAppointment(selectedAppointment);
            stage.setTitle("Edit Appointment");
            stage.setScene(new Scene(scene));
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to generate window", e);
        }
    }

    /** This method is used to delete the selected appointment from the database. After clicking Delete Appointment on the Appointments tab, the user is asked to confirm that they want to delete the selected appointment before the delete statement is executed.
     *
     * @throws SQLException
     */
    public void onActionDeleteAppointment() throws SQLException {
        if (allAppointmentsTableView.getSelectionModel().isEmpty()) {
            showInformationDialog("Warning!", "No Appointment Selected", "Select which appointment you'd like to delete and try again.");
            return;
        }
        if (showConfirmationDialog("Warning!", "Are you sure you want to delete this appointment?")) {
            Appointments selectedAppointment = allAppointmentsTableView.getSelectionModel().getSelectedItem();
            int appointmentID = selectedAppointment.getAppointmentID();
            String appointmentType = selectedAppointment.getAppointmentType();
            databaseConnect();
            String sqlDelete = "DELETE FROM APPOINTMENTS WHERE Appointment_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sqlDelete);
            statement.setInt(1, appointmentID);

            statement.executeUpdate();
            statement.close();

            allAppointmentsTableView.getItems().remove(selectedAppointment);
            showInformationDialog("Appointment Scheduler Application", "Appointment Successfully Deleted", "Deleted Appointment Details \nAppointment ID: " + appointmentID + "\nAppointment Type: " + appointmentType);
        }
    }

    /**  This method is used to exit the application. The user is asked to confirm their decision before the application is terminated.
     */
    public void ExitApplicationButtonSelected() {
        if (showConfirmationDialog("Exit", "Are you sure you want to exit this program?")) {
            System.exit(0);
        }
    }

    /**  This method handles the event when the All Appointments radio button is selected. It updates the table view on the Appointments tab to show all appointments within the database.
     */
    public void AllAppointmentsRadioButtonSelected() {
        if (allAppointmentsRB.isSelected()) {
            allAppointmentsTableView.setItems(getAllAppointments());
        }
    }

    /** Lambda #7 - This method handles the event when the Current Month radio button is selected. It updates the table view on the Appointments tab to show only appointments occurring between the start of the current week and the end of the current week. The lambda here allows for a more streamlined way of defining the logic for filtering the appointments without the need for loops.
     */
    public void CurrentWeekAppointmentsRadioButtonSelected() {
        if (currentWeekRBAppts.isSelected()) {
            LocalDate now = LocalDate.now();
            LocalDate startOfWeek = now.minusDays(now.getDayOfWeek().getValue() - 1);
            LocalDate endOfWeek = now.plusDays(7 - now.getDayOfWeek().getValue());

            List<Appointments> appointmentsForCurrentWeek = getAllAppointments().stream()
                    .filter(appointment -> {
                        LocalDateTime startDateTime = appointment.getAppointmentStart();
                        return startDateTime.toLocalDate().isEqual(startOfWeek) || startDateTime.toLocalDate().isAfter(startOfWeek) && startDateTime.toLocalDate().isBefore(endOfWeek.plusDays(1));
                    })
                    .collect(Collectors.toList());

            allAppointmentsTableView.setItems(FXCollections.observableArrayList(appointmentsForCurrentWeek));
        }
    }

    /**  This method handles the event when the Current Month radio button is selected. It updates the table view on the Appointments tab to show only appointments occurring between the start of the current month and the end of the current month.
     * Lambda #8 - allows for a more streamlined way of defining the logic for filtering the appointments without the need for loops.
     */
    public void CurrentMonthAppointmentsRadioButtonSelected() {
        if (currentMonthRBAppts.isSelected()) {
            LocalDate now = LocalDate.now();
            LocalDate startOfMonth = now.withDayOfMonth(1);
            LocalDate endOfMonth = now.withDayOfMonth(now.lengthOfMonth());

            List<Appointments> appointmentsForCurrentMonth = getAllAppointments().stream()
                    .filter(appointment -> {
                        LocalDateTime startDateTime = appointment.getAppointmentStart();
                        LocalDate appointmentDate = startDateTime.toLocalDate();
                        return appointmentDate.isEqual(startOfMonth) || (appointmentDate.isAfter(startOfMonth) && appointmentDate.isBefore(endOfMonth.plusDays(1)));
                    })
                    .collect(Collectors.toList());

            allAppointmentsTableView.setItems(FXCollections.observableArrayList(appointmentsForCurrentMonth));
        }
    }

    /** This method loads AddCustomer.fxml which allows the user to add a new customer to the database.
     *
     * @param event
     * @throws IOException
     */
    public void onActionAddCustomer(ActionEvent event) throws IOException {
        loadScene("/view/AddCustomer.fxml", "Add Customers", event);
    }

    /** This method loads EditCustomer.fxml which allows the user to modify the selected customer's details.
     *
     * @param event user clicks the Edit Customer button within the Customers tab.
     */
    public void onActionEditCustomer(ActionEvent event) {
        try {
            Customers selectedCustomer = allCustomersTableView.getSelectionModel().getSelectedItem();
            if (allCustomersTableView.getSelectionModel().isEmpty()) {
                showInformationDialog("Warning!", "No Customer Selected", "Select which customer you'd like to edit and try again.");
                return;
            }
            Stage stage = (Stage) ((Button) event.getSource()).getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/EditCustomer.fxml"));
            scene = loader.load();
            EditCustomerController editCustomerController = loader.getController();
            editCustomerController.setCustomer(selectedCustomer);
            stage.setTitle("Edit Customer");
            stage.setScene(new Scene(scene));
            stage.show();
        } catch (IOException e) {
            Logger logger = Logger.getLogger(getClass().getName());
            logger.log(Level.SEVERE, "Failed to generate window", e);
        }
    }

    /** This method is used to delete the selected customer from the database.
     * It first checks to make sure that the customer has no active appointments since all appointments need to be removed before a customer can be deleted.
     * If there are no appointments, the user is asked to confirm that they want to delete the selected customer before the delete statement is executed.
     *
     * @throws SQLException
     */
    public void onActionDeleteCustomer() throws SQLException {

        if (allCustomersTableView.getSelectionModel().isEmpty()) {
            showInformationDialog("Warning!", "No Customer Selected", "Select which customer you'd like to delete and try again.");
            return;
        }

        Customers selectedCustomer = allCustomersTableView.getSelectionModel().getSelectedItem();
        int customerID = selectedCustomer.getCustomerID();
        String name = selectedCustomer.getCustomerName();

        if (doesCustomerHaveAppointments(customerID)) {
            showInformationDialog("Warning!", "Cannot Delete Customer", "The selected customer has existing appointments. Customers cannot be deleted until all of their existing appointments are deleted.");
            return;
        }

        if (showConfirmationDialog("Warning!", "Are you sure you want to delete this customer?")) {
            databaseConnect();

            String sqlDelete = "DELETE FROM CUSTOMERS WHERE Customer_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sqlDelete);
            statement.setInt(1, customerID);

            statement.executeUpdate();
            statement.close();

            allCustomersTableView.getItems().remove(selectedCustomer);
            showInformationDialog("Appointment Scheduler Application", "Customer Successfully Deleted", "Deleted Customer Details \nCustomer ID: " + customerID + "\nCustomer Name: " + name);
        }
    }

    /**  This method handles the event when the All Customers radio button is selected. It updates the table view on the Customers tab to show all customers within the database.
     */
    public void onActionAllCustomersRadioButton() {
        if (allCustomersRadioButton.isSelected()) {
            allCustomersTableView.setItems(getAllCustomers());
        }
    }

    /** This method updates the table view on the Customers tab to show only customers located within the United States.
     *
     * @param event the United States radio button is selected.
     */
    public void onActionUSRadioButton(ActionEvent event) {
        if (usRadioButton.isSelected()) {
            onActionCountryRadioButton(event, 1, usRadioButton, allCustomersTableView);
        }
    }

    /** This method updates the table view on the Customers tab to show only customers located within the United Kingdom.
     *
     * @param event the United Kingdom radio button is selected.
     */
    public void onActionUKRadioButton(ActionEvent event) {
        if (ukRadioButton.isSelected()) {
            onActionCountryRadioButton(event, 2, ukRadioButton, allCustomersTableView);
        }
    }

    /** This method updates the table view on the Customers tab to show only customers located within Canada.
     *
     * @param event the Canada radio button is selected.
     */
    public void onActionCanadaRadioButton(ActionEvent event) {
        if (canadaRadioButton.isSelected()) {
            onActionCountryRadioButton(event, 3, canadaRadioButton, allCustomersTableView);
        }
    }

    /** This method checks the selected radio button on the Reports tab and fetches the corresponding appointments based on the selected criteria from the reports combo box.
     * The table view is then updated with the fetched appointments, and the total number of appointments is displayed in the report result label.
     *
     */
    public void UpdateReportsComboBox() {
        if (appointmentsByCountryRadioButton.isSelected()) {
            allReportsTableView.getItems().clear();
            reportResultLabel.setText("");
            String selectedCountry = reportsComboBox.getValue() != null ? reportsComboBox.getValue() : "";
            List<Appointments> appointmentsByCountry = getAppointmentsByCountry(selectedCountry);
            allReportsTableView.setItems(FXCollections.observableArrayList(appointmentsByCountry));
            int appointmentCount = appointmentsByCountry.size();
            reportResultLabel.setText("Total Appointments: " + appointmentCount);
        } else if (appointmentByTypeRadioButton.isSelected()) {
            allReportsTableView.getItems().clear();
            reportResultLabel.setText("");
            String selectedType = reportsComboBox.getValue() != null ? reportsComboBox.getValue() : "";
            List<Appointments> appointmentsByType = getAppointmentsByType(selectedType);
            allReportsTableView.setItems(FXCollections.observableArrayList(appointmentsByType));
            int appointmentCount = appointmentsByType.size();
            reportResultLabel.setText("Total Appointments: " + appointmentCount);
        } else if (appointmentByMonthRadioButton.isSelected()) {
            allReportsTableView.getItems().clear();
            reportResultLabel.setText("");
            String selectedMonth = reportsComboBox.getValue() != null ? reportsComboBox.getValue() : "";
            List<Appointments> appointmentsByMonth = getAppointmentsByMonth(selectedMonth);
            allReportsTableView.setItems(FXCollections.observableArrayList(appointmentsByMonth));
            int appointmentCount = appointmentsByMonth.size();
            reportResultLabel.setText("Total Appointments: " + appointmentCount);
        } else if (appointmentsScheduleRadioButton.isSelected()) {
            allReportsTableView.getItems().clear();
            reportResultLabel.setText("");
            String selectedContact = reportsComboBox.getValue() != null ? reportsComboBox.getValue() : "";
            List<Appointments> appointmentsByContact = getAppointmentsContactSchedule(selectedContact);
            allReportsTableView.setItems(FXCollections.observableArrayList(appointmentsByContact));
            int appointmentCount = appointmentsByContact.size();
            reportResultLabel.setText("Total Appointments: " + appointmentCount);
        }
    }

    /** This method populates the reports combo box with the months of the year as options.
     */
    public void AppointmentsByMonthRadioButtonSelected() {
        String[] monthOptions = {
                "January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"
        };
        reportsComboBox.setItems(FXCollections.observableArrayList(monthOptions));
    }

    /** This method retrieves distinct appointment types from the database and populates the reports combo box with the retrieved types.
     */
    public void AppointmentsByTypeRadioButtonSelected() {
        try {
            databaseConnect();
            String sqlSelect = "SELECT DISTINCT Type FROM appointments";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            ResultSet resultSet = statement.executeQuery();

            ObservableList<String> typeOptions = FXCollections.observableArrayList();
            while (resultSet.next()) {
                String type = resultSet.getString("Type");
                typeOptions.add(type);
            }
            reportsComboBox.setItems(typeOptions);
            statement.close();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            databaseDisconnect();
        }
    }

    /** This method retrieves distinct countries from the database and populates the reports combo box with the retrieved countries.
     */
    public void AppointmentsByCountryRadioButtonSelected() {
        try {
            databaseConnect();
            String sqlSelect = "SELECT Country FROM countries";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            ResultSet resultSet = statement.executeQuery();

            ObservableList<String> countryOptions = FXCollections.observableArrayList();
            while (resultSet.next()) {
                String country = resultSet.getString("Country");
                countryOptions.add(country);
            }
            reportsComboBox.setItems(countryOptions);
            List<Customers> customers = getAllCustomers();
            List<Customers> filteredCustomers = new ArrayList<>();

            for (Customers customer : customers) {
                int divisionID = customer.getDivisionID();
                String countryName = getCountryByDivisionID(divisionID);

                if (countryName.equals(reportsComboBox.getValue())) {
                    filteredCustomers.add(customer);
                }
            }
            allReportsTableView.setItems(FXCollections.observableArrayList(filteredCustomers));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } finally {
            databaseDisconnect();
        }
    }

    /** This method retrieves contacts from the database and populates the reports combo box with the retrieved contact names.
     */
    public void AppointmentScheduleRadioButtonSelected() {
        try {
            databaseConnect();
            String sqlSelect = "SELECT contact_name FROM contacts";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            ResultSet resultSet = statement.executeQuery();

            ObservableList<String> contactOptions = FXCollections.observableArrayList();
            while (resultSet.next()) {
                String contactName = resultSet.getString("contact_name");
                contactOptions.add(contactName);
            }
            reportsComboBox.setItems(contactOptions);
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** This method initializes the MainController class, sets up the appointments table view with columns and data, sets up the customers table view with columns and data, and sets up the reports table view with columns.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseConnect();
        setupAppointmentsTableView(allAppointmentsTableView, apptIDCol, titleCol, descriptionCol, typeCol, startDateCol, endDateCol, apptCustomerIDCol, locationCol, contactCol, userIDCol, getAllAppointments());
        setupCustomersTableView(allCustomersTableView, customerIDColumn, customerNameColumn, customerPhoneColumn, customerAddressColumn, customerDivisionColumn, customerPostalColumn, customerCountryColumn, getAllCustomers());
        setupReportsTableView(allReportsTableView, reportApptIDCol, reportTitleCol, reportTypeCol, reportDescriptionCol, reportStartDateCol, reportEndDateCol, reportApptCustomerIDCol);
    }
}