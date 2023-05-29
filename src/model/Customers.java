package model;

public class Customers {

 private int customerID;
 private String customerName;
 private String customerAddress;
 private String customerPostalCode;
 private String customerPhoneNumber;
 private int divisionID;

    public Customers(int customerID, String customerName, String customerAddress, String customerPostalCode, String customerPhoneNumber, int divisionID) {
        this.customerID = customerID;
        this.customerName = customerName;
        this.customerAddress = customerAddress;
        this.customerPostalCode = customerPostalCode;
        this.customerPhoneNumber = customerPhoneNumber;
        this.divisionID = divisionID;
    }

    public Customers(int customerID, String customerName) {
        this.customerID = customerID;
        this.customerName = customerName;
    }

    /**
     * @return the ID of the customer.
     */
    public int getCustomerID() {
        return customerID;
    }

    /**
     * @return the name of the customer.
     */
    public String getCustomerName() {
        return customerName;
    }

    /**
     * @return the address of the customer.
     */
    public String getCustomerAddress() {
        return customerAddress;
    }

    /**
     * @return the postal code of the customer.
     */
    public String getCustomerPostalCode() {
        return customerPostalCode;
    }

    /**
     * @return the phone number of the customer.
     */
    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    /**
     * @return the division ID of the customer's address.
     */
    public int getDivisionID() {
        return divisionID;
    }

}
