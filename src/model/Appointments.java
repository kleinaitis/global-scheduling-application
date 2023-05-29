package model;

import java.time.LocalDateTime;

public class Appointments {
    private int appointmentID;
    private String appointmentTitle;
    private String appointmentDescription;
    private String appointmentLocation;
    private String appointmentType;
    private LocalDateTime appointmentStart;
    private LocalDateTime appointmentEnd;
    private int customerID;
    private int userID;
    private int contactID;

    public Appointments(int appointmentID, String appointmentTitle, String appointmentDescription, String appointmentLocation, String appointmentType, LocalDateTime appointmentStart, LocalDateTime appointmentEnd, int customerID, int userID, int contactID) {
        this.appointmentID = appointmentID;
        this.appointmentTitle = appointmentTitle;
        this.appointmentDescription = appointmentDescription;
        this.appointmentLocation = appointmentLocation;
        this.appointmentType = appointmentType;
        this.appointmentStart = appointmentStart;
        this.appointmentEnd = appointmentEnd;
        this.customerID = customerID;
        this.userID = userID;
        this.contactID = contactID;
    }

    public Appointments(int appointmentID, String appointmentTitle, String appointmentLocation, LocalDateTime time, String appointmentType) {
        this.appointmentID = appointmentID;
        this.appointmentTitle = appointmentTitle;
        this.appointmentLocation = appointmentLocation;
        this.appointmentStart = time;
        this.appointmentType = appointmentType;
    }

    public Appointments(int appointmentID, String appointmentTitle, String appointmentDescription, LocalDateTime appointmentStart, LocalDateTime appointmentEnd, int customerID, String appointmentType) {
        this.appointmentID = appointmentID;
        this.appointmentTitle = appointmentTitle;
        this.appointmentDescription = appointmentDescription;
        this.appointmentType = appointmentType;
        this.appointmentStart = appointmentStart;
        this.appointmentEnd = appointmentEnd;
        this.customerID = customerID;
    }

    public Appointments() {
    }

    /**
     * @return the appointment's location.
     */
    public String getAppointmentLocation() {
        return appointmentLocation;
    }

    /**
     * @return the appointment's ID.
     */
    public int getAppointmentID() {
        return appointmentID;
    }

    /**
     * @return the appointment's title.
     */
    public String getAppointmentTitle() {
        return appointmentTitle;
    }

    /**
     * @return the appointment's description.
     */
    public String getAppointmentDescription() {
        return appointmentDescription;
    }

    /**
     * @return the appointment's type.
     */
    public String getAppointmentType() {
        return appointmentType;
    }

    /**
     * @return the appointment's start date/time.
     */
    public LocalDateTime getAppointmentStart() {
        return appointmentStart;
    }

    /**
     * @return the appointment's end date/time.
     */
    public LocalDateTime getAppointmentEnd() {
        return appointmentEnd;
    }

    /**
     * @return the customer ID associated with the appointment.
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * @return the user ID associated with the appointment.
     */
    public int getUserID() {
        return userID;
    }

    /**
     * @return the contact ID associated with the appointment.
     */
    public int getContactID() {
        return contactID;
    }
}
