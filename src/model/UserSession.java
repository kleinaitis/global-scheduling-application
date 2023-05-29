package model;

/** This creates the UserSession class which is used to store the user's ID and name for the current session.
 */
public class UserSession {
    private static int userID = -1;
    private static String userName = "";

    /**
     * @return the ID of the user for the current session.
     */
    public static int getUserID() {
        return userID;
    }

    /**
     * @param userID the user ID to set.
     */
    public static void setUserID(int userID) {
        UserSession.userID = userID;
    }

    /**
     * @return the name of the user for the current session.
     */
    public static String getUserName() {
        return userName;
    }

    /**
     * @param userName the name of the user to set.
     */
    public static void setUserName(String userName) {
        UserSession.userName = userName;
    }
}