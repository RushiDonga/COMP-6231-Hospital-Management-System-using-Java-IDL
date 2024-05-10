package com.web.service;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WebInterface {
    // Admin Operations
    public String addApt(String appointmentID, String appointmentType, int capacity);

    public String removeApt(String appointmentID, String appointmentType);

    public String listAptAvailability(String appointmentType);

    // client + Admin Operations
    public String bookApt(String patientID, String appointmentID, String appointmentType);

    public String getAptSchedule(String patientID);

    public String cancelApt(String patientID, String appointmentID, String appointmenType);

    public String Apt(String patientID, String oldAppointmentID, String oldAppointmentType, String newAppointmentID,
                      String newAppointmentType);

}
