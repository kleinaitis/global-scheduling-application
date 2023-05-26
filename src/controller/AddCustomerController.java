package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import model.UserSession;

import java.io.IOException;
import java.net.URL;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

import static utility.CustomerUtility.*;
import static utility.GeneralUtilityFunctions.*;

/** This creates the AddCustomerController class which is used to create new customers.
 */
public class AddCustomerController implements Initializable {
    @FXML private TextField customerNameText;
    @FXML private TextField customerPhoneText;
    @FXML private TextField customerAddressText;
    @FXML private ComboBox<String> countryComboBox;
    @FXML private ComboBox<String> divisionComboBox;
    @FXML private Label divisionLabel;
    @FXML private TextField customerPostalText;

    String userName = UserSession.getUserName();

    /** This method saves a new customer to the database using user inputted values, and returns the user to the main screen if input validation succeeds.
     *
     * @param event Save button is pressed by the user
     * @throws IOException
     * @throws SQLException
     */
    public void onActionSaveCustomer(ActionEvent event) throws IOException, SQLException {
        String customerName = customerNameText.getText();
        String customerPhone = customerPhoneText.getText();
        String customerAddress = customerAddressText.getText();
        String customerPostal = customerPostalText.getText();
        String customerCountry = countryComboBox.getSelectionModel().getSelectedItem();
        String customerDivision = divisionComboBox.getSelectionModel().getSelectedItem();

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

        String sqlInsert = "INSERT INTO customers (Customer_Name, Address, Postal_Code, Phone, Create_Date, Created_By, Last_Update, Last_Updated_By, Division_ID) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement insertStatement = connection.prepareStatement(sqlInsert);
        insertStatement.setString(1, customerName);
        insertStatement.setString(2, customerAddress);
        insertStatement.setString(3, customerPostal);
        insertStatement.setString(4, customerPhone);
        insertStatement.setTimestamp(5, Timestamp.valueOf(LocalDateTime.now()));
        insertStatement.setString(6, userName);
        insertStatement.setTimestamp(7, Timestamp.valueOf(LocalDateTime.now()));
        insertStatement.setString(8, userName);
        insertStatement.setInt(9, divisionID);

        insertStatement.executeUpdate();
        loadScene("/view/Main.fxml", "Scheduling Application", event);
    }

    /** This method returns the application to the Main.fxml screen. No information that was input is saved.
     *
     * @param event user clicks the Cancel button
     */
    public void onActionCancel(ActionEvent event) throws IOException {
        loadScene("/view/Main.fxml", "Scheduling Application", event);
    }

    /** Lambda #1 - This method initializes the AddCustomerController class, sets the options in the country and division combo boxes, and updates the division label based on the selected country. The lambda allows for easier readability, understandability, and organization of the logic for updating the divisionComboBox and divisionLabel.
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