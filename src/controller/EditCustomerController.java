package controller;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Appointments;
import model.Customers;
import model.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static utility.CustomerUtility.*;
import static utility.GeneralUtilityFunctions.*;

/** This creates the EditCustomerController class which is used to update existing customer's information.
 */
public class EditCustomerController implements Initializable {
    @FXML private TextField createCustomerIDText;
    @FXML private TextField customerNameText;
    @FXML private TextField customerPhoneText;
    @FXML private TextField customerAddressText;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private ComboBox<String> divisionComboBox;
    @FXML private Label divisionLabel;
    @FXML private TextField customerPostalText;
    @FXML private TableView<Appointments> appointmentsTableView;
    @FXML private TableColumn<Appointments, Integer> appointmentIDColumn;
    @FXML private TableColumn<Appointments, String> appointmentTitleColumn;
    @FXML private TableColumn<Appointments, LocalDateTime> appointmentTimeColumn;
    @FXML private TableColumn<Appointments, String> appointmentLocationColumn;
    private Customers selectedCustomer;

    /** This method updates the selected customer in the database using user inputted values, and returns the user to the main screen if input validation succeeds.
     *
     * @param event Save button is pressed by the user
     */
    public void onActionSaveCustomer(ActionEvent event) {

        String customerName = customerNameText.getText();
        String customerPhone = customerPhoneText.getText();
        String customerAddress = customerAddressText.getText();
        String customerPostal = customerPostalText.getText();
        String customerCountry = countryComboBox.getSelectionModel().getSelectedItem();
        String customerDivision = divisionComboBox.getSelectionModel().getSelectedItem();

        try {

            if (!validateCustomerName(customerName)) {
                return;
            }

            if (!validateCustomerPhoneNumber(customerPhone)) {
                return;
            }

            if (!validateCustomerAddress(customerAddress)) {
                return;
            }

            if (!validateCustomerPostalCode(customerPostal)) {
                return;
            }

            if (customerCountry == null) {
                showInformationDialog("Error!", "Data Entry Error", "Select which country the customer's address is within.");
                return;
            }

            if (customerDivision == null) {
                showInformationDialog("Error!", "Data Entry Error", "Select which state/province/division the customer's address is within.");
                return;
            }

            int divisionID = getDivisionIDByName(customerDivision);
            int customerID = Integer.parseInt(createCustomerIDText.getText());

            String updateStatement = "UPDATE CUSTOMERS SET Customer_Name = ?,  Address = ?, Postal_Code = ? , Phone = ?, Last_Update = ?, Last_Updated_By = ?, Division_ID = ? " +
                    "WHERE Customer_ID = ?";

            PreparedStatement statement = connection.prepareStatement(updateStatement);
            statement.setString(1, customerName);
            statement.setString(2, customerAddress);
            statement.setString(3, customerPostal);
            statement.setString(4, customerPhone);
            statement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
            statement.setString(6, UserSession.getUserName());
            statement.setInt(7, divisionID);
            statement.setInt(8, customerID);

            statement.executeUpdate();

            loadScene("/view/Main.fxml", "Scheduling Application", event);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    /** This method takes the selected customer from the main screen and sets the corresponding text fields on the EditCustomerController.fxml screen to display the customer's details.
     *
     * @param customer the customer that was selected on main screen whenever Edit Customer was clicked by the user
     */
    public void setCustomer(Customers customer) {
        try {
            databaseConnect();
            selectedCustomer = customer;

            if (selectedCustomer != null) {
                createCustomerIDText.setText(String.valueOf(selectedCustomer.getCustomerID()));
                customerNameText.setText(selectedCustomer.getCustomerName());
                customerPhoneText.setText(selectedCustomer.getCustomerPhoneNumber());
                customerAddressText.setText(selectedCustomer.getCustomerAddress());
                customerPostalText.setText(selectedCustomer.getCustomerPostalCode());

                int divisionID = selectedCustomer.getDivisionID();
                String division = getDivisionNameByID(divisionID);
                divisionComboBox.getSelectionModel().select(division);

                int countryID = getCountryIDByDivisionID(divisionID);
                countryComboBox.getSelectionModel().select(getCountryNameByID(countryID));
                loadAppointments();
                countryComboBox.fireEvent(new ActionEvent());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** Retrieves appointments from the database based on the customer's ID and populates them in the table view on the screen. The appointment's time is converted to the user's local time zone before being displayed.
     */
    private void loadAppointments() {
        try {
            String sqlSelect = "SELECT * FROM appointments WHERE Customer_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setInt(1, selectedCustomer.getCustomerID());

            ResultSet resultSet = statement.executeQuery();

            appointmentsTableView.getItems().clear();

            appointmentIDColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
            appointmentTitleColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
            appointmentLocationColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentLocation"));
            appointmentTimeColumn.setCellValueFactory(new PropertyValueFactory<>("appointmentStart"));

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            appointmentTimeColumn.setCellFactory(column ->
                    new TableCell<>() {
                        @Override
                        protected void updateItem(LocalDateTime item, boolean empty) {
                            super.updateItem(item, empty);
                            if (empty || item == null) {
                                setText(null);
                            } else {
                                ZoneId zoneId = ZoneId.systemDefault();
                                ZonedDateTime localTime = item.atZone(ZoneOffset.UTC).withZoneSameLocal(zoneId);
                                setText(localTime.format(formatter));
                            }
                        }
                    });

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String location = resultSet.getString("Location");
                LocalDateTime time = resultSet.getTimestamp("Start").toLocalDateTime();
                String appointmentType = resultSet.getString("Type");

                Appointments appointment = new Appointments(appointmentID, title, location, time, appointmentType);
                appointmentsTableView.getItems().add(appointment);
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /** This method returns the application to the Main.fxml screen. No information that was input is saved.
     *
     * @param event user clicks the Cancel button
     */
    public void onActionCancel(ActionEvent event) throws IOException {
        loadScene("/view/Main.fxml", "Scheduling Application", event);

    }

    /** This method is used to delete the selected appointment from the database. The user is asked to confirm their decision before the delete statement executes.
     */
    public void onActionDeleteAppointment() {
        if (appointmentsTableView.getSelectionModel().isEmpty()) {
            showInformationDialog("Warning!", "No Appointment Selected", "Select which appointment you'd like to delete and try again.");
            return;
        }

        if (showConfirmationDialog("Warning!", "Are you sure you want to delete this appointment?")) {
            Appointments selectedAppointment = appointmentsTableView.getSelectionModel().getSelectedItem();
            int appointmentID = selectedAppointment.getAppointmentID();
            String appointmentType = selectedAppointment.getAppointmentType();
            try {
                String sqlDelete = "DELETE FROM APPOINTMENTS WHERE Appointment_ID = ?";
                PreparedStatement statement = connection.prepareStatement(sqlDelete);
                statement.setInt(1, appointmentID);

                statement.executeUpdate();
                statement.close();

                appointmentsTableView.getItems().remove(selectedAppointment);
                showInformationDialog("Appointment Scheduler Application", "Appointment Successfully Deleted", "Deleted Appointment Details \nAppointment ID: " + appointmentID + "\nAppointment Type: " + appointmentType);

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /** Lambda #3 - This method initializes the EditCustomerController class, sets the options in the country and division combo boxes, and updates the division label based on the selected country. This lambda allows for easier readability, understandability, and organization of the logic for updating the divisionComboBox and divisionLabel.
     *
     * @param url
     * @param resourceBundle
     */
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        databaseConnect();
        try {
            Map<String, List<String>> dataMap = getCountryAndDivisionData();

            countryComboBox.getItems().addAll(dataMap.keySet());
            countryComboBox.setOnAction(event -> {
                String selectedCountry = countryComboBox.getValue();
                List<String> divisions = dataMap.get(selectedCountry);
                divisionComboBox.getItems().clear();
                divisionComboBox.getItems().addAll(divisions);

                if (selectedCountry.equals("U.S.")) {
                    divisionLabel.setText("State");
                } else if (selectedCountry.equals("Canada")) {
                    divisionLabel.setText("Province");
                } else {
                    divisionLabel.setText("Division");
                }
            });
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}