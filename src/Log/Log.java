package Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    public static void userLog(String userID, String action, String requestParams, String response)
            throws IOException {
        FileWriter fileWriter = new FileWriter(getFileName(userID, "User"), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " Client Action: " + action + " | RequestParameters: "
                + requestParams + " | Server Response: " + response);

        printWriter.close();
    }

    public static void userLog(String userID, String msg) throws IOException {
        FileWriter fileWriter = new FileWriter(getFileName(userID, "User"), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void serverLog(String serverID, String userID, String requestType, String requestParams,
            String serverResponse) throws IOException {

        if (userID.equals("null")) {
            userID = "Admin";
        }
        FileWriter fileWriter = new FileWriter(getFileName(serverID, "Server"), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " userID: " + userID + " | RequestType: " + requestType
                + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

        printWriter.close();
    }

    public static void serverLog(String serverID, String msg) throws IOException {

        FileWriter fileWriter = new FileWriter(getFileName(serverID, "Server"), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    public static void deleteALogFile(String ID) throws IOException {

        String fileName = getFileName(ID, "User");
        File file = new File(fileName);
        file.delete();
    }

    private static String getFileName(String ID, String logType) {
        String fileName="";
        String path = "C:\\Users\\thumm\\Desktop\\Rushi\\DSD\\Assignment3\\Assignment3\\Logs";
        if (logType == "Server") {
            if (ID.equalsIgnoreCase("MTL")) {
                fileName = path + "\\Server\\MONTREAL.txt";
            } else if (ID.equalsIgnoreCase("QUE")) {
                fileName = path + "\\Server\\QUEBEC.txt";
            } else if (ID.equalsIgnoreCase("SHE")) {
                fileName = path + "\\Server\\SHERBROOKE.txt";
            }
        } else if (logType == "User") {
            fileName = path + "\\User\\" + ID + ".txt";
        }
        return fileName;
    }

    private static String getFormattedDate() {
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

}
