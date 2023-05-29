package model;

public class Contacts {
    public int contactID;
    public String contactName;
    public String contactEmail;

    public Contacts(int contactID, String contactName, String contactEmail) {
        this.contactID = contactID;
        this.contactName = contactName;
        this.contactEmail = contactEmail;
    }

    /**
     * @return contactID the ID of the contact.
     */
    public int getId() {

        return contactID;
    }

    /**
     * @return contactName the name of the contact.
     */
    public String getContactName() {

        return contactName;
    }

}