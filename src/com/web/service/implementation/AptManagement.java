package com.web.service.implementation;

import DataModel.AptModel;
import DataModel.UserModel;
import Log.Log;
import com.web.service.WebInterface;
import Helper.Helper;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@WebService(endpointInterface = "com.web.service.WebInterface")

@SOAPBinding(style = SOAPBinding.Style.RPC)
public class AptManagement implements WebInterface {

    private String srvID;
    private String srvName;
    private Map<String, Map<String, AptModel>> allApts;
    private Map<String, Map<String, List<String>>> userApts;
    private Map<String, UserModel> srvUsers;

    public AptManagement(String serverID, String serverName) {
        super();
        this.srvID = serverID;
        this.srvName = serverName;
        this.allApts = new ConcurrentHashMap<>();
        this.allApts.put(Helper.APT_TYPE_SURGEON, new ConcurrentHashMap<>());
        this.allApts.put(Helper.APT_TYPE_PHYSICIAN, new ConcurrentHashMap<>());
        this.allApts.put(Helper.APT_TYPE_DENTAL, new ConcurrentHashMap<>());
        this.userApts = new ConcurrentHashMap<>();
        this.srvUsers = new ConcurrentHashMap<>();
    }

    @Override
    public String addApt(String aptID, String aptType, int capacity) {
        String response;

        if (isAptofThisServer(aptID)) {
            if (aptExists(aptType, aptID)) {
                if (allApts.get(aptType).get(aptID).getTotalSlots() <= capacity) {
                    allApts.get(aptType).get(aptID).setTotalSlots(capacity);
                    response = "Success: Appointment " + aptID + " Capacity increased to " + capacity;
                    try {
                        Log.serverLog(srvID, "null", " addAppointment ",
                                " appointmentID: " + aptID + " appointmentType: "
                                        + aptType + " capacity " + capacity + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: Appointment Already Exists, Cannot Decrease Booking Capacity";
                    try {
                        Log.serverLog(srvID, "null", " addAppointment ",
                                " appointmentID: " + aptID + " appointmentType: "
                                        + aptType + " capacity " + capacity + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            } else {
                AptModel apt = new AptModel(aptType, aptID, capacity);
                Map<String, AptModel> eventHashMap = allApts.get(aptType);
                eventHashMap.put(aptID, apt);
                allApts.put(aptType, eventHashMap);
                response = "Success: Appointment " + aptID + " added successfully";
                try {
                    Log.serverLog(srvID, "null", " addAppointment ",
                            " appointmentID: " + aptID + " appointmentType: " + aptType
                                    + " capacity " + capacity + " ",
                            response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Add Appointments to servers other than " + srvName;
            try {
                Log.serverLog(srvID, "null", " addAppointment ",
                        " appointmentID: " + aptID + " appointmentType: " + aptType
                                + " capacity " + capacity + " ",
                        response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
    }

    @Override
    public String removeApt(String aptID, String aptType) {
        String response;
        if (isAptofThisServer(aptID)) {
            if (aptExists(aptType, aptID)) {
                List<String> registeredPatients = allApts.get(aptType).get(aptID)
                        .getRegisteredPatientsList();
                allApts.get(aptType).remove(aptID);

                System.out.println(" List of Users:" + registeredPatients);
                addPatientsToNextSameApt(aptID, aptType, registeredPatients);

                response = "Success: Appointment removed successfully";
                try {
                    Log.serverLog(srvID, "null", " removeAppointment ",
                            " appointmentID: " + aptID + " appointmentName: " + aptType + " ",
                            response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: Could Not Find Appointment with ID " + aptID;
                try {
                    Log.serverLog(srvID, "null", " removeAppointment ",
                            " appointmentID: " + aptID + " appointmentName: " + aptType + " ",
                            response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        } else {
            response = "Failed: Cannot Remove Appointment from Servers other than " + srvName;
            try {
                Log.serverLog(srvID, "null", " removeAppointment ",
                        " appointmentID: " + aptID + " appointmentName: " + aptType + " ", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response;
    }

    @Override
    public String listAptAvailability(String aptType) {
        String response;
        Map<String, AptModel> aptSlots = allApts.get(aptType);
        StringBuilder builder = new StringBuilder();
        builder.append(srvName + " Server " + aptType + ":\n");
        if (aptSlots.size() == 0) {
            builder.append("No slot for appointment " + aptType);
        } else {
            for (AptModel apt : aptSlots.values()) {
                builder.append(apt.toString() + " || ");
            }
        }
        builder.append("\n**********************************************\n");

        String otherServer1, otherServer2;
        if (srvID.equals("MTL")) {
            otherServer1 = sendUDPMessage(Helper.QUE_UDP_PORT, "listAppointmentAvailability", "null", aptType,
                    "null");
            otherServer2 = sendUDPMessage(Helper.SHE_UDP_PORT, "listAppointmentAvailability", "null", aptType,
                    "null");
        } else if (srvID.equals("QUE")) {
            otherServer1 = sendUDPMessage(Helper.MTL_UDP_PORT, "listAppointmentAvailability", "null", aptType,
                    "null");
            otherServer2 = sendUDPMessage(Helper.SHE_UDP_PORT, "listAppointmentAvailability", "null", aptType,
                    "null");
        } else {
            otherServer1 = sendUDPMessage(Helper.MTL_UDP_PORT, "listAppointmentAvailability", "null", aptType,
                    "null");
            otherServer2 = sendUDPMessage(Helper.QUE_UDP_PORT, "listAppointmentAvailability", "null", aptType,
                    "null");
        }
        builder.append(otherServer1).append(otherServer2);
        response = builder.toString();
        try {
            Log.serverLog(srvID, "null", " listAppointmentAvailability ",
                    "  appointmenType: " + aptType + " ", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String bookApt(String patientID, String aptID, String aptType) {

        String response;

        checkUserExists(patientID);

        if (isAptofThisServer(aptID)) {
            if (allApts.get(aptType).containsKey(aptID)) {
                AptModel apt = allApts.get(aptType).get(aptID);

                if (apt == null) {
                    response = "Failed: Appointment " + aptID + " Does not exists";
                    try {
                        Log.serverLog(srvID, patientID, " bookAppointment ",
                                " appointmentID: " + aptID + "appointmentType: " + aptType
                                        + " ",
                                response);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return response;
                }

                if (!apt.isFull()) {
                    if (userApts.containsKey(patientID)) {
                        if (userApts.get(patientID).containsKey(aptType)) {
                            if (!patientHasEvent(patientID, aptType, aptID)) {
                                if (isUserofThisServer(patientID))
                                    userApts.get(patientID).get(aptType).add(aptID);
                            } else {
                                response = "Failed: Appointment " + aptID + " Already Booked";

                                try {
                                    Log.serverLog(srvID, patientID, " bookAppointment ",
                                            " appointmentID: " + aptID + "appointmentType: " + aptType
                                                    + " ",
                                            response);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return response;
                            }
                        } else {
                            if (isUserofThisServer(patientID))
                                addAptTypeAndApt(patientID, aptType, aptID);
                        }
                    } else {
                        if (isUserofThisServer(patientID))
                            addPatientAndApt(patientID, aptType, aptID);
                    }
                    if (apt.addPatientToRegisteredPatientsList(patientID) == 1) {
                        response = "Success: Appointment Slot " + aptID + " Booked Successfully";
                    } else if (apt.addPatientToRegisteredPatientsList(patientID) == -1) {
                        response = "Failed: Appointment Slot " + aptID + " is Full";
                    } else {
                        response = "Failed: Cannot Add You To Appointment Slot " + aptID;
                    }

                    try {
                        Log.serverLog(srvID, patientID, " bookAppointment ",
                                " appointmentID: " + aptID + " appointmnentType: " + aptType + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;

                } else {
                    response = "Failed: Appointment Slot " + aptID + " is Full";

                    try {
                        Log.serverLog(srvID, patientID, " bookAppointment ",
                                " appointmentID: " + aptID + " appointmnentType: " + aptType + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            } else {
                response = "Appointment slot is not open yet by Administrator";
                return response;
            }

        } else {

            if (patientHasEvent(patientID, aptType, aptID)) {
                String serverResponse = "Failed: Appointment " + aptID + " Already Booked";
                try {
                    Log.serverLog(srvID, patientID, " bookAppointment ",
                            " appointmentID: " + aptID + " appointmnentType: " + aptType + " ",
                            serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return serverResponse;
            }

            if (!(exceedWeeklyLimit(patientID, aptID.substring(4)))) {

                String serverResponse = sendUDPMessage(Helper.getUDPServerPort(aptID.substring(0, 3)),
                        "bookAppointment", patientID, aptType, aptID);
                if (serverResponse.startsWith("Success:")) {
                    if (userApts.get(patientID).containsKey(aptType)) {
                        userApts.get(patientID).get(aptType).add(aptID);
                    } else {
                        List<String> temp = new ArrayList<>();
                        temp.add(aptID);
                        userApts.get(patientID).put(aptType, temp);
                    }
                }
                try {
                    Log.serverLog(srvID, patientID, " bookEvent ",
                            " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                            serverResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return serverResponse;
            } else {
                response = "Failed: You Cannot Book Appointment in Other Servers For This Week(Max Weekly Limit = 3)";
                try {
                    Log.serverLog(srvID, patientID, " bookEvent ",
                            "appointmentID: " + aptID + "appointmentType: " + aptType + " ", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    @Override
    public String getAptSchedule(String patientID) {
        String response;
        if (!checkUserExists(patientID)) {
            response = "Booking Schedule Empty For " + patientID;
            try {
                Log.serverLog(srvID, patientID, " getAppointmentSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        Map<String, List<String>> apts = userApts.get(patientID);
        if (apts.size() == 0) {
            response = "Booking Schedule Empty For " + patientID;
            try {
                Log.serverLog(srvID, patientID, " getAppointmentSchedule ", "null", response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        }
        StringBuilder builder = new StringBuilder();
        for (String aptType : apts.keySet()) {
            builder.append(aptType + ":\n");
            for (String aptId : apts.get(aptType)) {
                builder.append(aptId + " ||");
            }
            builder.append("\n**********************************************\n");
        }
        response = builder.toString();
        try {
            Log.serverLog(srvID, patientID, " getAppointmentSchedule ", "null", response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String cancelApt(String patientID, String aptID, String aptType) {
        String response;
        if (isAptofThisServer(aptID)) {
            if (isUserofThisServer(patientID)) {
                if (!checkUserExists(patientID)) {
                    response = "Failed: You " + patientID + " Are Not Registered in " + aptID;
                    try {
                        Log.serverLog(srvID, patientID, " cancelEvent ",
                                " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    if (removeAptIfExists(patientID, aptType, aptID)) {

                        allApts.get(aptType).get(aptID)
                                .removePatientFromRegisteredPatientsList(patientID);
                        response = "Success: Appointment " + aptID + " Canceled for " + patientID;
                        try {
                            Log.serverLog(srvID, patientID, " cancelEvent ",
                                    " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                                    response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    } else {
                        response = "Failed: You " + patientID + " Are Not Registered in " + aptID;
                        try {
                            Log.serverLog(srvID, patientID, " RMI cancelEvent ",
                                    " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                                    response);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        return response;
                    }
                }
            } else {
                if (allApts.get(aptType).get(aptID)
                        .removePatientFromRegisteredPatientsList(patientID)) {
                    response = "Success: Event " + aptID + " Canceled for " + patientID;
                    try {
                        Log.serverLog(srvID, patientID, " cancelEvent ",
                                " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                } else {
                    response = "Failed: You " + patientID + " Are Not Registered in " + aptID;
                    try {
                        Log.serverLog(srvID, patientID, " cancelEvent ",
                                " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return response;
                }
            }
        } else {

            if (!srvUsers.containsKey(patientID)) {
                addNewPatientToUsers(patientID);
            } else {
                if (userApts.get(patientID).get(aptType).remove(aptID)) {
                    return sendUDPMessage(Helper.getUDPServerPort(aptID.substring(0, 3)),
                            "cancelAppointment",
                            patientID,
                            aptType, aptID);
                }
            }
        }

        response = "Failed: You " + patientID + " Are Not Registered in " + aptID;
        try {
            Log.serverLog(srvID, patientID, " cancelEvent ",
                    " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                    response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    @Override
    public String Apt(String patientID, String oldAptID, String oldAptType,
                      String newAptID, String newAptType) {
        String response;
        if (!checkUserExists(patientID)) {
            response = "Failed: You " + patientID + " Are Not Registered in " + oldAptID;
            try {
                Log.serverLog(srvID, patientID, " swapAppointment ",
                        " oldAppointmentID: " + oldAptID + " oldAppointmentType: " + oldAptType
                                + " newAppointmentID: " + newAptID + " newAppointmentType: "
                                + newAptType + " ",
                        response);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return response;
        } else {
            if (patientHasEvent(patientID, oldAptType, oldAptID)) {
                String bookResp = "Failed: did not send book request for your newAppointment " + newAptID;
                String cancelResp = "Failed: did not send cancle request for your oldEvent " + oldAptID;
                synchronized (this) {
                    if (onTheSameWeek(newAptID.substring(4), oldAptID)
                            && !exceedWeeklyLimit(patientID, newAptID.substring(4))) {
                        cancelResp = cancelApt(patientID, oldAptID, oldAptType);
                        if (cancelResp.startsWith("Success:")) {
                            bookResp = bookApt(patientID, newAptID, newAptType);
                        }
                    } else {
                        bookResp = bookApt(patientID, newAptID, newAptType);
                        if (bookResp.startsWith("Success:")) {
                            cancelResp = cancelApt(patientID, oldAptID, oldAptType);
                        }
                    }
                }
                if (bookResp.startsWith("Success:") && cancelResp.startsWith("Success:")) {
                    response = "Success: Event " + oldAptID + " swapped with " + newAptID;
                } else if (bookResp.startsWith("Success:") && cancelResp.startsWith("Failed:")) {
                    cancelApt(patientID, newAptID, newAptType);
                    response = "Failed: Your oldAppointment " + oldAptID + " could not be cancled. Reason "
                            + cancelResp;
                } else if (bookResp.startsWith("Failed:") && cancelResp.startsWith("Success:")) {
                    String resp1 = bookApt(patientID, oldAptID, oldAptType);
                    response = "Failed: Your new Appointment " + newAptID + " could not be booked. Reason: "
                            + bookResp + " and your old  Appointment rolling back: " + resp1;
                } else {
                    response = "Failed: on both new Appointment" + newAptID + " Booking reason: " + bookResp
                            + " and old Appointment " + oldAptID + " canceling resong: " + cancelResp;
                }
                try {
                    Log.serverLog(srvID, patientID, " swapAppointment ",
                            " oldAppointmentID: " + oldAptID + " oldAppointmentType: " + oldAptType
                                    + " newAppointmentID: " + newAptID + " newAppointmentType: "
                                    + newAptType + " ",
                            response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            } else {
                response = "Failed: You " + patientID + " are not registered in " + oldAptID;
                try {
                    Log.serverLog(srvID, patientID, " swapAppointment ",
                            " oldAppointmentID: " + oldAptID + " oldAppointmentType: " + oldAptType
                                    + " newAppointmentID: " + newAptID + " newAppointmentType: "
                                    + newAptType + " ",
                            response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return response;
            }
        }
    }

    // FOR UDP CALLS
    public String removeAptUDP(String aptID, String aptType, String userID) {
        if (!srvUsers.containsKey(userID)) {
            addNewPatientToUsers(userID);
            return "Failed: You " + userID + " Are Not Registered with " + aptID;
        } else {
            if (userApts.get(userID).get(aptType).remove(aptID)) {
                return "Success: Appointment " + aptID + " Was Removed from " + userID + " Schedule";
            } else {
                return "Failed: You " + userID + " Are Not Registered with " + aptID;
            }
        }
    }

    // FOR UDP CALLS
    public String listAptAvailabilityUDP(String aptType) {
        Map<String, AptModel> apts = allApts.get(aptType);
        StringBuilder builder = new StringBuilder();
        builder.append(srvName + " Server, Appointment Slots for" + aptType + ":\n");
        if (apts.size() == 0) {
            builder.append("No Events of Type " + aptType);
        } else {
            for (AptModel event : apts.values()) {
                builder.append(event.toString() + " ||\n ");
            }
        }

        builder.append("\n**********************************************\n");
        return builder.toString();
    }

    private String sendUDPMessage(int ServerPort, String method, String patientID, String aptType,
                                  String aptID) {
        DatagramSocket aSocket = null;
        String result = "";
        String dataFromClient = method + ";" + patientID + ";" + aptType + ";" + aptID;

        try {
            Log.serverLog(srvID, patientID, " UDP request sent " + method + " ",
                    " appointmentID: " + aptID + " appointmentType: " + aptType + " ", " ... ");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            aSocket = new DatagramSocket();
            byte[] message = dataFromClient.getBytes();
            InetAddress aHost = InetAddress.getByName("localhost");
            DatagramPacket request = new DatagramPacket(message, dataFromClient.length(), aHost, ServerPort);

            aSocket.send(request);

            byte[] buffer = new byte[1000];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            aSocket.receive(reply);

            result = new String(reply.getData());
            String[] parts = result.split(";");
            result = parts[0];

        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("IO: " + e.getMessage());
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
        try {
            Log.serverLog(srvID, patientID, " UDP reply received" + method + " ",
                    " eventID: " + aptID + " appointmentType: " + aptType + " ", result);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    private void addPatientsToNextSameApt(String aptID, String aptType,
                                          List<String> registeredPatients) {
        String response;
        for (String patientID : registeredPatients) {
            if (patientID.substring(0, 3).equals(srvID)) {
                removeAptIfExists(patientID, aptType, aptID);
                String nextSameAptResult = getNextSameApt(allApts.get(aptType).keySet(),
                        aptType, aptID);
                if (nextSameAptResult.equals("Failed")) {
                    response = "Acquiring nextSameAppointment: " + nextSameAptResult;

                    try {
                        Log.serverLog(srvID, patientID, " addPatientsToNextSameEvent ",
                                " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                                response);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    System.out.println("Couldn't find another slot for same appointment");
                    return;
                } else {
                    System.out.println("trying to book Appointment for patient " + patientID);
                    System.out.println(nextSameAptResult + " nextEventResult");
                    bookApt(patientID, nextSameAptResult, aptType);
                }
            } else {
                System.out.println("Send UDP called for remove slot and add new Slot");

                sendUDPMessage(Helper.getUDPServerPort(patientID.substring(0, 3)),
                        "removeAppointment", patientID, aptType, aptID);
            }
        }
    }

    private String getNextSameApt(Set<String> keySet, String aptType, String apt) {
        List<String> sortedIDs = new ArrayList<String>(keySet);
        sortedIDs.add(apt);
        Collections.sort(sortedIDs, new Comparator<String>() {
            @Override
            public int compare(String ID1, String ID2) {
                Integer timeSlot1 = 0;
                switch (ID1.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot1 = 1;
                        break;
                    case "A":
                        timeSlot1 = 2;
                        break;
                    case "E":
                        timeSlot1 = 3;
                        break;
                }
                Integer timeSlot2 = 0;
                switch (ID2.substring(3, 4).toUpperCase()) {
                    case "M":
                        timeSlot2 = 1;
                        break;
                    case "A":
                        timeSlot2 = 2;
                        break;
                    case "E":
                        timeSlot2 = 3;
                        break;
                }
                Integer date1 = Integer.parseInt(ID1.substring(8, 10) + ID1.substring(6, 8) + ID1.substring(4, 6));
                Integer date2 = Integer.parseInt(ID2.substring(8, 10) + ID2.substring(6, 8) + ID2.substring(4, 6));
                int dateCompare = date1.compareTo(date2);
                int timeSlotCompare = timeSlot1.compareTo(timeSlot2);
                if (dateCompare == 0) {
                    return ((timeSlotCompare == 0) ? dateCompare : timeSlotCompare);
                } else {
                    return dateCompare;
                }
            }
        });
        int index = sortedIDs.indexOf(apt) + 1;
        for (int i = index; i < sortedIDs.size(); i++) {
            if (!allApts.get(aptType).get(sortedIDs.get(i)).isFull()) {
                System.out.println(sortedIDs.get(i));
                return sortedIDs.get(i);
            }
        }
        return "Failed";
    }

    private boolean exceedWeeklyLimit(String patientID, String aptDate) {
        int limit = 0;

        System.out.println("userAppointments:" + userApts);

        for (int i = 0; i < 3; i++) {
            List<String> registeredIDs = new ArrayList<>();
            switch (i) {
                case 0:
                    if (userApts.get(patientID).containsKey(Helper.APT_TYPE_PHYSICIAN)) {
                        registeredIDs = userApts.get(patientID).get(Helper.APT_TYPE_PHYSICIAN);
                    }
                    break;
                case 1:
                    if (userApts.get(patientID).containsKey(Helper.APT_TYPE_SURGEON)) {
                        registeredIDs = userApts.get(patientID).get(Helper.APT_TYPE_SURGEON);
                    }
                    break;
                case 2:
                    if (userApts.get(patientID).containsKey(Helper.APT_TYPE_DENTAL)) {
                        registeredIDs = userApts.get(patientID).get(Helper.APT_TYPE_DENTAL);
                    }
                    break;
            }

            for (String aptID : registeredIDs) {
                if (aptID.substring(6, 8).equals(aptDate.substring(2, 4))
                        && aptID.substring(8, 10).equals(aptDate.substring(4, 6))) {
                    int week1 = Integer.parseInt(aptID.substring(4, 6)) / 7;
                    int week2 = Integer.parseInt(aptDate.substring(0, 2)) / 7;
                    // System.out.println("Week1:" + week1 + " Week2:" + week2);
                    if (week1 == week2) {
                        limit++;
                    }
                }

                if (limit == 3) {
                    // System.out.println("limit hit");
                    return true;
                }
            }

        }
        return false;
    }

    public void addNewApt(String aptID, String aptType, int capacity) {
        AptModel sampleConf = new AptModel(aptType, aptID, capacity);
        allApts.get(aptType).put(aptID, sampleConf);
    }

    public void addNewPatientToUsers(String patientID) {
        UserModel newCustomer = new UserModel(patientID);
        srvUsers.put(newCustomer.getUserID(), newCustomer);
        userApts.put(newCustomer.getUserID(), new ConcurrentHashMap<>());
    }

    private synchronized boolean isAptofThisServer(String aptID) {
        return Helper.identifyServer(aptID).equals(srvName);
    }

    private synchronized boolean aptExists(String aptType, String aptID) {
        return allApts.get(aptType).containsKey(aptID);
    }

    private synchronized boolean checkUserExists(String patientID) {
        if (!srvUsers.containsKey(patientID)) {
            addNewPatientToUsers(patientID);
            return false;
        } else {
            return true;
        }
    }

    private synchronized boolean patientHasEvent(String patientID, String aptType, String aptID) {
        if (userApts.get(patientID).containsKey(aptType)) {
            return userApts.get(patientID).get(aptType).contains(aptID);
        } else {
            return false;
        }
    }

    private boolean isUserofThisServer(String userID) {
        return userID.substring(0, 3).equals(srvID);
    }

    private synchronized void addAptTypeAndApt(String patientID, String aptType,
                                               String aptID) {
        List<String> temp = new ArrayList<>();
        temp.add(aptID);
        userApts.get(patientID).put(aptType, temp);
    }

    private synchronized void addPatientAndApt(String patientID, String aptType,
                                               String aptID) {
        Map<String, List<String>> temp = new ConcurrentHashMap<>();
        List<String> temp2 = new ArrayList<>();
        temp2.add(aptID);
        temp.put(aptType, temp2);
        userApts.put(patientID, temp);
    }

    private boolean removeAptIfExists(String patientID, String aptType, String aptID) {

        if (userApts.get(patientID).containsKey(aptType)) {
            return userApts.get(patientID).get(aptType).remove(aptID);
        } else {
            return false;
        }
    }

    private boolean onTheSameWeek(String newAptDate, String apt) {
        if (apt.substring(6, 8).equals(newAptDate.substring(2, 4))
                && apt.substring(8, 10).equals(newAptDate.substring(4, 6))) {
            int week1 = Integer.parseInt(apt.substring(4, 6)) / 7;
            int week2 = Integer.parseInt(newAptDate.substring(0, 2)) / 7;
            return week1 == week2;
        } else {
            return false;
        }
    }
}
