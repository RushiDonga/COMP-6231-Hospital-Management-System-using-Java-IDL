package com.web.service;

import javax.xml.namespace.QName;
import javax.xml.ws.Service;

import java.net.URL;

public class TestData {
    public static Service mtlService;
	public static Service sheService;
	public static Service queService;

    public static final String PHYSICIAN = "Physician";
    public static final String SURGEON = "Surgeon";
    public static final String DENTAL = "Dental";

    public static void main(String[] args) throws Exception {
        URL montrealURL=new URL("http://localhost:8080/montreal?wsdl");
        QName montrealQName=new QName("http://implementation.service.web.com/","AppointmentManagementService");
        mtlService = Service.create(montrealURL,montrealQName);

        URL quebecURL=new URL("http://localhost:8080/quebec?wsdl");
        QName quebecQName=new QName("http://implementation.service.web.com/","AppointmentManagementService");
        mtlService = Service.create(quebecURL,quebecQName);

        URL sherbroookeURL=new URL("http://localhost:8080/sherbrooke?wsdl");
        QName sherbrookeQName=new QName("http://implementation.service.web.com/","AppointmentManagementService");
        mtlService = Service.create(sherbroookeURL,sherbrookeQName);

        addTestData();
	}

    private synchronized static void addTestData() {
		WebInterface MTLobj = mtlService.getPort(WebInterface.class);
		WebInterface QUEobj = queService.getPort(WebInterface.class);
		WebInterface SHEobj = sheService.getPort(WebInterface.class);

		System.out.println("PreDemo TestCases");
		System.out.println("*********************************************************");

		System.out.println("Logged in as MTLA3456 ADMIN:");
		System.out.println(MTLobj.addApt("MTLA080820", PHYSICIAN, 2));
		System.out.println(MTLobj.addApt("MTLM110820", SURGEON, 1));
		System.out.println(MTLobj.addApt("MTLM120820", SURGEON, 1));

		System.out.println("Logged in as SHEA9000 ADMIN:");
		System.out.println(SHEobj.addApt("SHEE080820", DENTAL, 1));

		System.out.println("Logged in as QUEA9000 ADMIN:");
		System.out.println(QUEobj.addApt("QUEA250820", SURGEON, 1));
		System.out.println(QUEobj.addApt("QUEE150820", DENTAL, 1));

		System.out.println("Logged in as SHEP1234 PATIENT:");
		System.out.println(SHEobj.bookApt("SHEC1234", "MTLA080820", PHYSICIAN));
		System.out.println(SHEobj.bookApt("SHEC1234", "MTLM110820", SURGEON));
		System.out.println(SHEobj.bookApt("SHEC1234", "QUEA250820", SURGEON));
		System.out.println(SHEobj.bookApt("SHEC1234", "SHEE080820", DENTAL));

	}
}
