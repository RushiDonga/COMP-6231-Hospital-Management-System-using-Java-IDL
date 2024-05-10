package DataModel;

import java.util.ArrayList;
import java.util.List;

import Helper.Helper;

public class AptModel {
    private String aptServer;
    private String aptID;
    private String aptType;
    private int totalSlots;
    private String aptDate;
    private String aptTimeSlot;
    private List<String> registeredPatients;

    public AptModel(String aptType, String aptID, int totalSlots) {
        this.aptID = aptID;
        this.aptType = aptType;
        this.totalSlots = totalSlots;
        this.aptTimeSlot = Helper.identifyTimeSlotFromAptID(aptID);
        this.aptServer = Helper.identifyServer(aptID);
        this.aptDate = Helper.identifyAptDate(aptID);
        registeredPatients = new ArrayList<>();
    }

    public String getAptID() {
        return aptID;
    }

    public void setAptId(String aptID) {
        this.aptID = aptID;
    }

    public String getAptType() {
        return aptID;
    }

    public void setAptType(String aptID) {
        this.aptID = aptID;
    }

    public String getAptDate() {
        return aptDate;
    }

    public void setAptDate(String aptDate) {
        this.aptDate = aptDate;
    }

    public String getAptTimeSlot() {
        return aptTimeSlot;
    }

    public void setAptTimeSlot(String aptTimeSlot) {
        this.aptTimeSlot = aptTimeSlot;
    }

    public int getTotalSlots() {
        return totalSlots;
    }

    public void setTotalSlots(int slotsSize) {
        this.totalSlots = slotsSize;
    }

    public int getRemainingSlotSize() {
        return totalSlots - registeredPatients.size();
    }

    public int addPatientToRegisteredPatientsList(String registeredPatientID) {
        if (!isFull()) {
            if (registeredPatients.contains(registeredPatientID)) {
                return 0;
            } else {
                registeredPatients.add(registeredPatientID);
                return 1;
            }
        } else {
            return -1;
        }
    }

    public boolean isFull() {
        return getTotalSlots() == registeredPatients.size();
    }

    public List<String> getRegisteredPatientsList() {
        return registeredPatients;
    }

    public boolean removePatientFromRegisteredPatientsList(String patientID) {
        return registeredPatients.remove(patientID);
    }

    @Override
    public String toString() {
        return " (" + getAptID() + ") in the " + getAptTimeSlot() + " of " + getAptDate()
                + " Total[Remaining] Capacity: " + getTotalSlots() + "[" + getRemainingSlotSize() + "]";
    }
}
