package DataModel;

public class UserModel {
    public static final String USER_TYPE_ADMIN= "Admin";
    public static final String USER_TYPE_PATIENT = "Patient";
    public static final String USER_SERVER_SHERBROOKE = "SHERBROOKE";
    public static final String USER_SERVER_QUEBEC = "QUEBEC";
    public static final String USER_SERVER_MONTREAL = "MONTREAL";
    private String userType;
    private String userID;
    private String userServer;

    public UserModel(String userID) {
        this.userID = userID;
        this.userType = detectUserType();
        this.userServer = detectUserServer();
    }

    private String detectUserServer() {
        if (userID.substring(0, 3).equalsIgnoreCase("MTL")) {
            return USER_SERVER_MONTREAL;
        } else if (userID.substring(0, 3).equalsIgnoreCase("QUE")) {
            return USER_SERVER_QUEBEC;
        } else {
            return USER_SERVER_SHERBROOKE;
        }
    }

    private String detectUserType() {
        if (userID.substring(3, 4).equalsIgnoreCase("A")) {
            return USER_TYPE_ADMIN;
        } else {
            return USER_TYPE_PATIENT;
        }
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserServer() {
        return userServer;
    }

    public void setUserServer(String userServer) {
        this.userServer = userServer;
    }

    @Override
    public String toString() {
        return getUserType() + "(" + getUserID() + ") on " + getUserServer() + " Server.";
    }
}
