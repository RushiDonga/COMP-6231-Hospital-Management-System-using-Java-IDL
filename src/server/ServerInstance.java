package server;

import Helper.Helper;
import Log.Log;
import com.web.service.implementation.AptManagement;

import javax.xml.ws.Endpoint;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class ServerInstance {

    private String serverID;
    private String serverName;
    private String serverEndpoint;
    private int serverUdpPort;

    public ServerInstance(String serverID, String[] args) throws Exception {
        this.serverID = serverID;

        switch (serverID) {
            case "MTL":
                serverName = Helper.SERVER_MTL;
                serverUdpPort = Helper.MTL_UDP_PORT;
                serverEndpoint = "http://localhost:8080/montreal";
                break;
            case "QUE":
                serverName = Helper.SERVER_QUE;
                serverUdpPort = Helper.QUE_UDP_PORT;
                serverEndpoint = "http://localhost:8080/quebec";
                break;
            case "SHE":
                serverName = Helper.SERVER_SHE;
                serverUdpPort = Helper.SHE_UDP_PORT;
                serverEndpoint = "http://localhost:8080/sherbrooke";
                break;
        }
        try {
            System.out.println(serverName + " Server started....");
            Log.serverLog(serverID, " Server started....");
            AptManagement service = new AptManagement(serverID, serverName);

            Endpoint endpoint = Endpoint.publish(serverEndpoint, service);

            System.out.println(serverName + " Server is Up & Running");
            Log.serverLog(serverID, " Server is Up & Running");

            Runnable task = () -> {
                listenForRequest(service, serverUdpPort, serverName, serverID);
            };
            Thread thread = new Thread(task);
            thread.start();

        } catch (Exception e) {
            e.printStackTrace(System.out);
            Log.serverLog(serverID, "Exception: " + e);
        }
    }

    private static void listenForRequest(AptManagement obj, int serverUdpPort, String serverName,
                                         String serverID) {
        DatagramSocket aSocket = null;
        String sendingResult = "";
        try {
            aSocket = new DatagramSocket(serverUdpPort);
            byte[] buffer = new byte[1000];
            System.out.println(serverName + " UDP Server Started at port " + aSocket.getLocalPort() + " ............");
            Log.serverLog(serverID, " UDP Server Started at port " + aSocket.getLocalPort());
            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                aSocket.receive(request);
                String sentence = new String(request.getData(), 0,
                        request.getLength());
                String[] parts = sentence.split(";");
                String method = parts[0];
                String userID = parts[1];
                String aptType = parts[2];
                String aptID = parts[3];
                if (method.equalsIgnoreCase("removeAppointment")) {
                    Log.serverLog(serverID, userID, " UDP request received " + method + " ",
                            " appointmentID: " + aptID + " appointmentType: " + aptType + " ", " ...");
                    String result = obj.removeAptUDP(aptID, aptType, userID);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("listAppointmentAvailability")) {
                    Log.serverLog(serverID, userID, " UDP request received " + method + " ",
                            " appointmentType: " + aptType + " ", " ...");
                    String result = obj.listAptAvailabilityUDP(aptType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("bookAppointment")) {
                    Log.serverLog(serverID, userID, " UDP request received " + method + " ",
                            " appointmentID: " + aptID + " appointmentType: " + aptType + " ", " ...");
                    String result = obj.bookApt(userID, aptID, aptType);
                    sendingResult = result + ";";
                } else if (method.equalsIgnoreCase("cancelAppointment")) {
                    Log.serverLog(serverID, userID, " UDP request received " + method + " ",
                            " appointmentID: " + aptID + " appointmentType: " + aptType + " ", " ...");
                    String result = obj.cancelApt(userID, aptID, aptType);
                    sendingResult = result + ";";
                }
                byte[] sendData = sendingResult.getBytes();
                DatagramPacket reply = new DatagramPacket(sendData, sendingResult.length(), request.getAddress(),
                        request.getPort());
                aSocket.send(reply);
                Log.serverLog(serverID, userID, " UDP reply sent " + method + " ",
                        " appointmentID: " + aptID + " appointmentType: " + aptType + " ",
                        sendingResult);
            }
        } catch (SocketException e) {
            System.out.println("SocketException: " + e.getMessage());
            e.printStackTrace(System.out);
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
            e.printStackTrace(System.out);
        } finally {
            if (aSocket != null)
                aSocket.close();
        }
    }
}
