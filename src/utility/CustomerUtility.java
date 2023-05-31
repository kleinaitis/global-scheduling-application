package utility;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableView;
import model.Customers;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static utility.GeneralUtilityFunctions.*;

/** This creates the CustomerUtility class which encapsulates common functionality and operations that are performed on customers.
 */
public class CustomerUtility {

    /** This method retrieves country and division data from the database.
     *
     * @return a map containing country names as keys and a list of division names as values.
     * @throws SQLException
     */
    public static Map<String, List<String>> getCountryAndDivisionData() throws SQLException {
        Map<String, List<String>> dataMap = new HashMap<>();

        String sqlSelectCountry = "SELECT Country FROM countries ORDER BY Country ASC";
        PreparedStatement countryPS = connection.prepareStatement(sqlSelectCountry);
        ResultSet countryResultSet = countryPS.executeQuery();

        while (countryResultSet.next()) {
            String country = countryResultSet.getString("Country");
            dataMap.put(country, new ArrayList<>());
        }

        String sqlSelectDivision = "SELECT c.Country, d.Division FROM First_Level_Divisions d " +
                "JOIN countries c ON d.Country_ID = c.Country_ID ORDER BY c.Country ASC, d.Division ASC";
        PreparedStatement divisionPS = connection.prepareStatement(sqlSelectDivision);
        ResultSet divisionResultSet = divisionPS.executeQuery();

        while (divisionResultSet.next()) {
            String country = divisionResultSet.getString("Country");
            String division = divisionResultSet.getString("Division");

            List<String> divisions = dataMap.get(country);
            if (divisions != null) {
                divisions.add(division);
            }
        }
        return dataMap;
    }

    /** This method validates the user's input on the customer name text field.
     *
     * @param customerName the customer name to be validated.
     * @return true if the customer's name is valid, false otherwise.
     */
    public static boolean validateCustomerName(String customerName) {
        if (customerName.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Customer's name cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method validates the user's input on the customer phone number text field.
     *
     * @param customerPhoneNumber the customer phone number to be validated.
     * @return true if the customer's phone number is valid, false otherwise.
     */
    public static boolean validateCustomerPhoneNumber(String customerPhoneNumber) {
        String cleanedPhoneNumber = customerPhoneNumber.replaceAll("[^\\d-]", "");

        if (!cleanedPhoneNumber.matches("^[\\d-]+$")) {
            showInformationDialog("Error!", "Data Entry Error", "Customer's phone number can only contain digits and hyphens.");
            return false;
        }
        return true;
    }

    /** This method validates the user's input on the customer address text field.
     *
     * @param customerAddress the customer address to be validated.
     * @return true if the customer's address is valid, false otherwise.
     */
    public static boolean validateCustomerAddress(String customerAddress) {
        if (customerAddress.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Customer's address cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method validates the user's input on the customer postal code text field.
     *
     * @param customerPostalCode the customer postal code to be validated.
     * @return true if the customer's postal code is valid, false otherwise.
     */
    public static boolean validateCustomerPostalCode(String customerPostalCode) {
        if (customerPostalCode.isEmpty()) {
            showInformationDialog("Error!", "Data Entry Error", "Customer's postal code cannot be empty.");
            return false;
        }
        return true;
    }

    /** This method is used to retrieve the country ID associated with a division ID from the database.
     *
     * @param divisionID the ID of the division that we're checking the country ID of.
     * @return the countryID associated with the division ID.
     */
    public static int getCountryIDByDivisionID(int divisionID) {
        int countryID = -1;

        try {
            String sqlSelect = "SELECT country_ID FROM first_level_divisions WHERE division_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setInt(1, divisionID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                countryID = resultSet.getInt("Country_ID");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countryID;
    }

    /** This method is used to retrieve the country name associated with a country ID from the database.
     *
     * @param countryID the ID of the country that we're checking the country name of.
     * @return the name of the country associated with the specific country ID.
     */
    public static String getCountryNameByID(int countryID) {
        String countryName = "";

        try {
            String sqlSelect = "SELECT country FROM countries WHERE country_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setInt(1, countryID);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                countryName = resultSet.getString("Country");
            }

            resultSet.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return countryName;
    }

    /** This method is used to retrieve an observable list of all customers within the database that contains their ID, name, phone number, postal code, address, and division ID.
     *
     * @return an observable list of all customers.
     */
    public static ObservableList<Customers> getAllCustomers() {
        ObservableList<Customers> customersObservableList = FXCollections.observableArrayList();
        PreparedStatement statement;
        ResultSet resultSet;

        try {
            databaseConnect();
            String sqlSelect = "SELECT * FROM customers";
            statement = connection.prepareStatement(sqlSelect);
            resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int customerID = resultSet.getInt("Customer_ID");
                String customerName = resultSet.getString("Customer_Name");
                String customerPhone = resultSet.getString("Phone");
                String customerPostal = resultSet.getString("Postal_Code");
                String customerAddress = resultSet.getString("Address");
                int divisionID = resultSet.getInt("Division_ID");
                Customers customers = new Customers(customerID, customerName, customerAddress, customerPostal, customerPhone, divisionID);
                customersObservableList.add(customers);

            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return customersObservableList;
    }

    /** This method is used to filter the customer's table view on the Customer's tab of Main.fxml based on the specific country radio button selected.
     *
     * @param event the event created by selecting a specific country radio button
     * @param countryId the ID of the country selected.
     * @param selectedRadioButton the radio button selected.
     * @param tableView the table view to display the filtered customers.
     */
    public static void onActionCountryRadioButton(ActionEvent event, int countryId, RadioButton selectedRadioButton, TableView<Customers> tableView) {
        if (selectedRadioButton.isSelected()) {
            List<Customers> customers = getAllCustomers();

            List<Customers> filteredCustomers = new ArrayList<>();

            for (Customers customer : customers) {
                int divisionID = customer.getDivisionID();
                int customerCountryID = getCountryIDByDivisionID(divisionID);

                if (customerCountryID == countryId) {
                    filteredCustomers.add(customer);
                }
            }

            tableView.setItems(FXCollections.observableArrayList(filteredCustomers));
        }
    }

    /** This method is used to retrieve a division name based on its division ID.
     *
     * @param divisionID the ID of the division.
     * @return the name of the division associated with the specific division ID.
     */
    public static String getDivisionNameByID(int divisionID) {
        String divisionName = "";
        try {
            String sqlSelect = "SELECT Division FROM first_level_divisions WHERE Division_ID = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setInt(1, divisionID);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                divisionName = rs.getString("Division");
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return divisionName;
    }

    /** This method is used to retrieve the country name associated with a divisionID from the database.
     *
     * @param divisionID the ID of the division.
     * @return the name of the country associated with the specific division ID.
     * @throws SQLException
     */
    public static String getCountryByDivisionID(int divisionID) throws SQLException {
        String country = null;

        String sqlSelect = "SELECT c.Country FROM countries c " +
                "JOIN First_Level_Divisions d ON c.Country_ID = d.Country_ID " +
                "WHERE d.Division_ID = ?";

        PreparedStatement statement = connection.prepareStatement(sqlSelect);
        statement.setInt(1, divisionID);

        ResultSet resultSet = statement.executeQuery();
        if (resultSet.next()) {
            country = resultSet.getString("Country");
        }

        return country;
    }

    /** This method is used to retrieve the division ID associated with a division name from the database.
     *
     * @param divisionName the name of the division.
     * @return the division ID associated with the division name.
     */
    public static int getDivisionIDByName(String divisionName) {
        int divisionID = 0;
        try {
            String query = "SELECT Division_ID FROM first_level_divisions WHERE Division = ?";
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setString(1, divisionName);
            ResultSet rs = statement.executeQuery();
            if (rs.next()) {
                divisionID = rs.getInt("Division_ID");
            }
            rs.close();
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return divisionID;
    }
}
