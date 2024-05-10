package Helper;

import java.util.Scanner;

public class Helper {

    public static final String SERVER_MTL = "MONTREAL";
    public static final String SERVER_QUE = "QUEBEC";
    public static final String SERVER_SHE = "SHERBROOKE";

    public static final int MTL_UDP_PORT = 6666;
    public static final int SHE_UDP_PORT = 7777;
    public static final int QUE_UDP_PORT = 8888;

    public static final String USER_PATIENT = "Patient";
    public static final String USER_ADMIN = "Admin";

    public static final String APT_TYPE_SURGEON = "Surgeon";
    public static final String APT_TYPE_PHYSICIAN = "Physician";
    public static final String APT_TYPE_DENTAL = "Dental";

    public static final String ADMIN_ADD_APT = "1";
    public static final String ADMIN_REMOVE_APT = "2";
    public static final String ADMIN_LIST_APT_AVAILABILITY = "3";
    public static final String ADMIN_BOOK_APT = "4";
    public static final String ADMIN_GET_APT_SCHEDULE = "5";
    public static final String ADMIN_CANCEL_APT = "6";
    public static final String ADMIN_SWAP_APT = "7";
    public static final String ADMIN_LOGOUT = "8";
    public static final String PATIENT_BOOK_APT = "1";
    public static final String PATIENT_GET_APT_SCHEDULE = "2";
    public static final String PATIENT_CANCEL_APT = "3";
    public static final String PATIENT_SWAP_APT = "4";
    public static final String PATIENT_LOGOUT = "5";

    public static String getUserType(String usrID) {
        if (usrID.length() == 8) {
            if (usrID.substring(0, 3).equals("MTL") ||
                    usrID.substring(0, 3).equals("QUE") ||
                    usrID.substring(0, 3).equals("SHE")) {
                if (usrID.substring(3, 4).equals("P")) {
                    return USER_PATIENT;
                } else if (usrID.substring(3, 4).equalsIgnoreCase("A")) {
                    return USER_ADMIN;
                }
            }
        }
        return "";
    }

    public static void getToDoOperation(String userType) {
        System.out.println("**********************************************");
        System.out.println("Please choose an option below:");
        if (userType == USER_PATIENT) {
            System.out.println("1) Book Appointment");
            System.out.println("2) Get Appointment Schedule");
            System.out.println("3) Cancel Appointment");
            System.out.println("4) Swap Appointment");
            System.out.println("5) Logout");
        } else if (userType == USER_ADMIN) {
            System.out.println("1) Add Appointment Slot");
            System.out.println("2) Remove Appointment Slot");
            System.out.println("3) List Appointments Availability");
            System.out.println("4) Book Appointment");
            System.out.println("5) Get Appointment Schedule");
            System.out.println("6) Cancel Appointment");
            System.out.println("7) Swap Appointment");
            System.out.println("8) Logout");
        }
    }

    public static int getAppointmentCapacity(Scanner sc) {
        System.out.println("**********************************************");
        System.out.println("Please enter the booking capacity:");
        return sc.nextInt();
    }

    public static String getAppointmentType(Scanner sc) {
        System.out.println("**********************************************");
        System.out.println("Please choose an Appointment type below:");
        System.out.println("1) Physician");
        System.out.println("2) Surgeon");
        System.out.println("3) Dental");
        switch (sc.next()) {
            case "1":
                return APT_TYPE_PHYSICIAN;
            case "2":
                return APT_TYPE_SURGEON;
            case "3":
                return APT_TYPE_DENTAL;
            default:
                System.out.println("Invalid Choice");
        }
        return getAppointmentType(sc);
    }

    public static String getAptID(Scanner sc) {
        System.out.println("**********************************************");
        System.out.println("Please enter the appointmentID:");
        String eventID = sc.next().trim().toUpperCase();
        if (eventID.length() == 10) {
            if (eventID.substring(0, 3).equalsIgnoreCase("MTL") ||
                    eventID.substring(0, 3).equalsIgnoreCase("QUE") ||
                    eventID.substring(0, 3).equalsIgnoreCase("SHE")) {
                if (eventID.substring(3, 4).equalsIgnoreCase("M") ||
                        eventID.substring(3, 4).equalsIgnoreCase("A") ||
                        eventID.substring(3, 4).equalsIgnoreCase("E")) {
                    return eventID;
                }
            }
        }
        return getAptID(sc);
    }

    public static String identifyTimeSlotFromAptID(String appointmentID) {
        if (appointmentID.substring(3, 4).toUpperCase().equals("M")) {
            return "Morning";
        } else if (appointmentID.substring(3, 4).toUpperCase().equals("A")) {
            return "Afternoon";
        } else {
            return "Evening";
        }
    }

    public static String identifyServer(String appointmentID) {
        if (appointmentID.substring(0, 3).toUpperCase().equals("MTL")) {
            return SERVER_MTL;
        } else if (appointmentID.substring(0, 3).toUpperCase().equals("QUE")) {
            return SERVER_QUE;
        } else {
            return SERVER_SHE;
        }
    }

    public static String identifyAptDate(String appointmentID) {
        return appointmentID.substring(4, 6) + "/" + appointmentID.substring(6, 8) + "/20" + appointmentID.substring(8);
    }

    public static int getUDPServerPort(String serverId) {
        if (serverId.equalsIgnoreCase("MTL")) {
            return MTL_UDP_PORT;
        } else if (serverId.equalsIgnoreCase("QUE")) {
            return QUE_UDP_PORT;
        } else if (serverId.equalsIgnoreCase("SHE")) {
            return SHE_UDP_PORT;
        }
        return 1;
    }

}
