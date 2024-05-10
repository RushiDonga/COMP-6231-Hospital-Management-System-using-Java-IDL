package com.web.client;

import Log.Log;
import com.web.service.WebInterface;
import Helper.Helper;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.net.URL;
import java.util.Scanner;

public class Main {
    public static Service mtlService;
    public static Service shrService;
    public static Service queService;
    private static WebInterface obj;

    static Scanner sc;

    public static void main(String[] args) throws Exception {
        URL montrealURL = new URL("http://localhost:8080/montreal?wsdl");
        QName montrealQName = new QName("http://implementation.service.web.com/", "AptManagementService");
        mtlService = Service.create(montrealURL, montrealQName);

        URL quebecURL = new URL("http://localhost:8080/quebec?wsdl");
        QName quebecQName = new QName("http://implementation.service.web.com/", "AptManagementService");
        queService = Service.create(quebecURL, quebecQName);

        URL sherbroookeURL = new URL("http://localhost:8080/sherbrooke?wsdl");
        QName sherbrookeQName = new QName("http://implementation.service.web.com/", "AptManagementService");
        shrService = Service.create(sherbroookeURL, sherbrookeQName);

        init();
    }

    public static void init() throws Exception {
        System.out.println("**********************************************************");
        System.out.println("Welcome to the 'Distributed Health Care Management System'\n");
        System.out.println("Student Id: 40269583                                    *'\n");
        System.out.println("**********************************************************\n");
        start();
    }

    public static void start() throws Exception {
        sc = new Scanner(System.in);

        System.out.println("Please enter your UserId:(For Concurrency test enter 'ConTest')");
        String userID = sc.next().trim().toUpperCase();

        String userType = Helper.getUserType(userID);
        if (userID.equalsIgnoreCase("ConTest")) {
            startConcurrencyTest();
        } else {
            Log.userLog(userID, " login attempt");
            switch (userType) {
                case Helper.USER_PATIENT: {
                    try {
                        System.out.println("Successfully logged in as " + userID);
                        Log.userLog(userID, "Patient Login Successful.");
                        patient(userID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                case Helper.USER_ADMIN: {
                    try {
                        System.out.println("Successfully logged in as " + userID);
                        Log.userLog(userID, "Admin Login Successful.");
                        admin(userID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }

                default: {
                    System.out.println("Invalid UserId!!");
                    Log.deleteALogFile(userID);
                    start();

                }
            }
        }
    }

    private static void startConcurrencyTest() throws Exception {
        System.out.println("Concurrency Test Starting for BookAppointment");
        System.out.println("Connecting Montreal Server...");
        String appointmentType = Helper.APT_TYPE_SURGEON;
        String appointmentID = "MTLE100324";
        WebInterface servant = mtlService.getPort(WebInterface.class);
        System.out
                .println("adding " + appointmentID + " " + appointmentType + " with capacity 2 to Montreal Server...");
        String response = servant.addApt(appointmentID, appointmentType, 2);
        System.out.println(response);
        Runnable task1 = () -> {
            String patientID = "MTLP2345";
            // System.out.println("Connecting Montreal Server for " + patientID);
            String res = servant.bookApt(patientID, appointmentID, appointmentType);
            System.out.println("Booking response for " + patientID + " " + res);
        };
        Runnable task2 = () -> {
            String patientID = "MTLP3456";
            String res = servant.bookApt(patientID, appointmentID, appointmentType);
            System.out.println("Booking response for " + patientID + " " + res);
        };
        Runnable task3 = () -> {
            String patientID = "MTLP4567";
            String res = servant.bookApt(patientID, appointmentID, appointmentType);
            System.out.println("Booking response for " + patientID + " " + res);
        };
        Runnable task4 = () -> {
            String patientID = "MTLP6789";
            String res = servant.bookApt(patientID, appointmentID, appointmentType);
            System.out.println("Booking response for " + patientID + " " + res);

            res = servant.cancelApt("MTLP3456", appointmentID, appointmentType);
            System.out.println("Canceling response for " + patientID + " " + res);

            res = servant.cancelApt("MTLP4567", appointmentID, appointmentType);
            System.out.println("Canceling response for " + patientID + " " + res);
        };

        Runnable task5 = () -> {
            String res = servant.removeApt(appointmentID, appointmentType);
            System.out.println("remove appointment response for " + appointmentID + " " + res);
        };

        Thread thread1 = new Thread(task1);
        Thread thread2 = new Thread(task2);
        Thread thread3 = new Thread(task3);
        Thread thread4 = new Thread(task4);
        Thread thread5 = new Thread(task5);
        // synchronized (thread1) {
        thread1.start();
        thread2.start();
        thread3.start();
        // }
        thread1.join();
        thread2.join();
        thread3.join();

        thread4.start();
        thread4.join();

        System.out.println("Concurrency Test Finished for BookAppointment");
        thread5.start();
        thread5.join();
        init();
    }


    public static void patient(String patientID) throws Exception {

        String serverID = getServerID(patientID);
        if (serverID.equals("1")) {
            init();
        }

        boolean repeat = true;

        String appointmentType;
        String appointmentID;

        Helper.getToDoOperation("Patient");
        String userSelection = sc.next();

        String serverResponse;
        switch (userSelection) {
            case Helper.PATIENT_BOOK_APT: {
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                Log.userLog(patientID, "attempting to book appointment for " + appointmentType + " with appointmentID "
                        + appointmentID);
                serverResponse = obj.bookApt(patientID, appointmentID, appointmentType);
                Log.userLog(patientID, " bookAppointment",
                        " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ",
                        serverResponse);
                System.out.println(serverResponse);
                break;
            }

            case Helper.PATIENT_GET_APT_SCHEDULE: {
                Log.userLog(patientID, "attempting to get Appointment Schedule");
                serverResponse = obj.getAptSchedule(patientID);
                System.out.println(serverResponse);
                Log.userLog(patientID, " getAppointmentSchedule", " null ", serverResponse);
                break;
            }

            case Helper.PATIENT_CANCEL_APT: {
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                Log.userLog(patientID,
                        "attempting to cancel appointment for " + appointmentType + " with appointmentID "
                                + appointmentID);
                serverResponse = obj.cancelApt(patientID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Log.userLog(
                        patientID, " cancelAppointment", " appointmentID: " + appointmentID + " appointmentType: "
                                + appointmentType + " ",
                        serverResponse);
                break;
            }

            case Helper.PATIENT_SWAP_APT: {
                System.out.println("Please Enter the OLD appointment to be replaced");
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                System.out.println("Please Enter the NEW appointment to be replaced");
                String newAppointmentType = Helper.getAppointmentType(sc);
                String newAppointmentID = Helper.getAptID(sc);
                Log.userLog(patientID, " attempting to swapAppointment");
                serverResponse = obj.Apt(patientID, appointmentID, appointmentType, newAppointmentID,
                        newAppointmentType);
                System.out.println(serverResponse);
                Log.userLog(patientID, " swapAppointment",
                        " oldAppointmentID: " + appointmentID + " oldAppointmentType: " + appointmentType
                                + " newAppointmentID: " + newAppointmentID + " newAppointmentType: "
                                + newAppointmentType + " ",
                        serverResponse);
                break;
            }
            
            case Helper.PATIENT_LOGOUT: {
                repeat = false;
                System.out.println(patientID + " Logout Successfully!!");
                Log.userLog(patientID, " Logout Successfully");
                start();
                break;
            }
            default:
                System.out.println("Please Enter Valid Choice.");
        }

        if (repeat) {
            patient(patientID);
        }
    }

    public static void admin(String adminID) throws Exception {

        String serverID = getServerID(adminID);
        if (serverID.equals("1")) {
            init();
        }

        boolean repeat = true;
        Helper.getToDoOperation("Admin");
        String userSelection = sc.next();

        String appointmentType;
        String appointmentID;
        String serverResponse;
        int capacity;

        switch (userSelection) {
            case Helper.ADMIN_ADD_APT: {
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                capacity = Helper.getAppointmentCapacity(sc);
                Log.userLog(adminID,
                        "attempting to add appointment for " + appointmentType + " with slot size " + capacity);
                serverResponse = obj.addApt(appointmentID, appointmentType, capacity);
                System.out.println(serverResponse);
                Log.userLog(adminID, " bookAppointment",
                        " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " capacity: "
                                + capacity,
                        serverResponse);
                break;
            }

            case Helper.ADMIN_REMOVE_APT: {
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                Log.userLog(adminID,
                        "attempting to remove appointments for " + appointmentType + " with appointmentID "
                                + appointmentID);
                serverResponse = obj.removeApt(appointmentID, appointmentType);
                Log.userLog(adminID, " removeAppointment",
                        " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ",
                        serverResponse);
                System.out.println(serverResponse);
                break;
            }

            case Helper.ADMIN_LIST_APT_AVAILABILITY: {
                appointmentType = Helper.getAppointmentType(sc);
                Log.userLog(adminID, "attempting to check appointment availability");
                serverResponse = obj.listAptAvailability(appointmentType);
                System.out.println(serverResponse);
                Log.userLog(adminID, " listAppointmentAvailability",
                        " appointmentType: " + appointmentType + " ",
                        serverResponse);
                break;
            }

            case Helper.ADMIN_BOOK_APT: {
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                Log.userLog(adminID, "attempting to book appointment for " + appointmentType + " with appointmentID "
                        + appointmentID);
                serverResponse = obj.bookApt(adminID, appointmentID, appointmentType);
                Log.userLog(adminID, " bookAppointment",
                        " appointmentID: " + appointmentID + " appointmentType: " + appointmentType + " ",
                        serverResponse);
                System.out.println(serverResponse);
                break;
            }

            case Helper.ADMIN_GET_APT_SCHEDULE: {
                Log.userLog(adminID, "attempting to get Appointment Schedule");
                serverResponse = obj.getAptSchedule(adminID);
                System.out.println(serverResponse);
                Log.userLog(adminID, " getAppointmentSchedule", " null ", serverResponse);
                break;
            }

            case Helper.ADMIN_CANCEL_APT: {
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                Log.userLog(adminID,
                        "attempting to cancel appointment for " + appointmentType + " with appointmentID "
                                + appointmentID);
                serverResponse = obj.cancelApt(adminID, appointmentID, appointmentType);
                System.out.println(serverResponse);
                Log.userLog(
                        adminID, " cancelAppointment", " appointmentID: " + appointmentID + " appointmentType: "
                                + appointmentType + " ",
                        serverResponse);
                break;
            }

            case Helper.ADMIN_SWAP_APT: {
                // patientID = askForCustomerIDFromManager(adminID.substring(0, 3));
                System.out.println("Please Enter the OLD appointment to be swapped");
                appointmentType = Helper.getAppointmentType(sc);
                appointmentID = Helper.getAptID(sc);
                System.out.println("Please Enter the NEW appointment to be swapped");
                String newAppointmentType = Helper.getAppointmentType(sc);
                String newAppointmentID = Helper.getAptID(sc);
                Log.userLog(adminID, " attempting to swapAppointment");
                serverResponse = obj.Apt(adminID, appointmentID, appointmentType, newAppointmentID,
                        newAppointmentType);
                System.out.println(serverResponse);
                Log.userLog(adminID, " swapAppointment",
                        " patientID: " + adminID + " oldAppointmentID: " + appointmentID + " oldAppointmentType: "
                                + appointmentType + " newAppointmentID: " + newAppointmentID + " newAppointmentType: "
                                + newAppointmentType + " ",
                        serverResponse);
                break;
            }

            case Helper.ADMIN_LOGOUT: {
                repeat = false;
                System.out.println(adminID + " Logout Successfully!!");
                Log.userLog(adminID, " Logout Successfully");
                start();
                break;
            }
            default:
                System.out.println("Please Enter Valid Choice.");
        }

        if (repeat) {
            admin(adminID);
        }
    }

    
    public static String getServerID(String userID) {
        String branchAcronym = userID.substring(0, 3);
        if (branchAcronym.equalsIgnoreCase("MTL")) {
            obj = mtlService.getPort(WebInterface.class);
            return branchAcronym;
        } else if (branchAcronym.equalsIgnoreCase("SHE")) {
            obj = shrService.getPort(WebInterface.class);
            return branchAcronym;
        } else if (branchAcronym.equalsIgnoreCase("QUE")) {
            obj = queService.getPort(WebInterface.class);
            return branchAcronym;
        }
        return "1";
    }

}
