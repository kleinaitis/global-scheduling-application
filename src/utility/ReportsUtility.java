package utility;

import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import model.Appointments;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static utility.GeneralUtilityFunctions.*;

/** This creates the ReportsUtility class which encapsulates functionality and operations that are performed on the, "Reports," tab of the MainController.
 */
public class ReportsUtility {

    /** This method sets up the table view for displaying reports.
     *
     * @param tableView the table view to be set up.
     * @param apptIDCol the column for displaying appointment IDs.
     * @param titleCol the column for displaying appointment titles.
     * @param typeCol the column for displaying appointment types.
     * @param descriptionCol the column for displaying appointment descriptions.
     * @param startDateCol the column for displaying appointment start date/times.
     * @param endDateCol the column for displaying appointment end date/times.
     * @param apptCustomerIDCol the column for displaying customer IDs.
     */
    public static void setupReportsTableView(TableView<Appointments> tableView, TableColumn<Appointments, Integer> apptIDCol, TableColumn<Appointments, String> titleCol,
                                       TableColumn<Appointments, String> typeCol, TableColumn<Appointments, String> descriptionCol,
                                       TableColumn<Appointments, LocalDateTime> startDateCol, TableColumn<Appointments, LocalDateTime> endDateCol,
                                       TableColumn<Appointments, Integer> apptCustomerIDCol) {
        apptIDCol.setCellValueFactory(new PropertyValueFactory<>("appointmentID"));
        titleCol.setCellValueFactory(new PropertyValueFactory<>("appointmentTitle"));
        typeCol.setCellValueFactory(new PropertyValueFactory<>("appointmentType"));
        descriptionCol.setCellValueFactory(new PropertyValueFactory<>("appointmentDescription"));
        startDateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentStart"));
        endDateCol.setCellValueFactory(new PropertyValueFactory<>("appointmentEnd"));
        apptCustomerIDCol.setCellValueFactory(new PropertyValueFactory<>("customerID"));
    }

    /** This method is used to retrieve a list of appointments for a specific month from the database.
     *
     * @param month the name of the month to retrieve appointments from.
     * @return a list of appointments scheduled in the selected month.
     */
    public static List<Appointments> getAppointmentsByMonth(String month) {
        List<Appointments> appointments = new ArrayList<>();

        try {
            databaseConnect();
            String sqlSelect = "SELECT * FROM appointments WHERE MONTHNAME(Start) = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setString(1, month);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                LocalDateTime startDateTime = resultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp("End").toLocalDateTime();
                int customerID = resultSet.getInt("Customer_ID");
                String appointmentType = resultSet.getString("Type");

                Appointments appointment = new Appointments(appointmentID, title, description, startDateTime, endDateTime, customerID, appointmentType);
                appointments.add(appointment);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }
    /** This method is used to retrieve a list of appointments based on the appointment type selected from the database.
     *
     * @param type the type of appointment to retrieve appointments for.
     * @return a list of appointments scheduled with the specified type.
     */
    public static List<Appointments> getAppointmentsByType(String type) {
        List<Appointments> appointments = new ArrayList<>();

        try {
            databaseConnect();
            String sqlSelect = "SELECT * FROM appointments WHERE Type = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setString(1, type);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                LocalDateTime startDateTime = resultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp("End").toLocalDateTime();
                int customerID = resultSet.getInt("Customer_ID");
                String appointmentType = resultSet.getString("Type");

                Appointments appointment = new Appointments(appointmentID, title, description, startDateTime, endDateTime, customerID, appointmentType);
                appointments.add(appointment);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            databaseDisconnect();
        }
        return appointments;
    }

    /** This method is used to retrieve a list of appointments for a specific country from the database.
     *
     * @param country the name of the country to retrieve appointments from.
     * @return a list of appointments scheduled in the selected country.
     */
    public static List<Appointments> getAppointmentsByCountry(String country) {
        List<Appointments> appointments = new ArrayList<>();

        try {
            databaseConnect();
            String sqlSelect = "SELECT * FROM appointments a " +
                    "INNER JOIN customers c ON a.Customer_ID = c.Customer_ID " +
                    "INNER JOIN first_level_divisions d ON c.Division_ID = d.Division_ID " +
                    "INNER JOIN countries cn ON d.COUNTRY_ID = cn.COUNTRY_ID " +
                    "WHERE cn.country = ?";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setString(1, country);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                LocalDateTime startDateTime = resultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp("End").toLocalDateTime();
                int customerID = resultSet.getInt("Customer_ID");
                String type = resultSet.getString("Type");

                Appointments appointment = new Appointments(appointmentID, title, description, startDateTime, endDateTime, customerID, type);
                appointments.add(appointment);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally { databaseDisconnect();
        }
        return appointments;
    }

    /** This method is used to retrieve a list of appointments for a specific contact from the database.
     *
     * @param contactName the name of the contact for which to retrieve appointments.
     * @return a list of appointments scheduled for the specified contact.
     */
    public static List<Appointments> getAppointmentsContactSchedule(String contactName) {
        List<Appointments> appointments = new ArrayList<>();
        try {
            databaseConnect();
            String sqlSelect = "SELECT * FROM appointments a " +
                    "INNER JOIN contacts c ON a.Contact_ID = c.Contact_ID " +
                    "WHERE c.contact_name = ? " +
                    "ORDER BY a.Start";
            PreparedStatement statement = connection.prepareStatement(sqlSelect);
            statement.setString(1, contactName);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                int appointmentID = resultSet.getInt("Appointment_ID");
                String title = resultSet.getString("Title");
                String description = resultSet.getString("Description");
                LocalDateTime startDateTime = resultSet.getTimestamp("Start").toLocalDateTime();
                LocalDateTime endDateTime = resultSet.getTimestamp("End").toLocalDateTime();
                int customerID = resultSet.getInt("Customer_ID");
                String type = resultSet.getString("Type");

                Appointments appointment = new Appointments(appointmentID, title, description, startDateTime, endDateTime, customerID, type);
                appointments.add(appointment);
            }
            statement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return appointments;
    }
}
