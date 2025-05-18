package com.example.mywebapp;

import java.io.*;
import java.util.*;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;

@WebServlet("/AdminManageAppointmentServlet")
public class AdminManageAppointmentServlet extends HttpServlet {
    private static final String FILE_NAME = "/WEB-INF/appointments.txt";

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String appointmentId = request.getParameter("appointmentId");
        String patientName = request.getParameter("patientName");
        String doctorName = request.getParameter("doctorName");
        String appointmentDate = request.getParameter("appointmentDate");
        String appointmentTime = request.getParameter("appointmentTime");
        String status = request.getParameter("status");

        if (isNotEmpty(appointmentId) && isNotEmpty(patientName) && isNotEmpty(doctorName)
                && isNotEmpty(appointmentDate) && isNotEmpty(appointmentTime) && isNotEmpty(status)) {

            saveAppointment(request, appointmentId, patientName, doctorName, appointmentDate, appointmentTime, status);
            response.sendRedirect("admin_manageAppointment.html");
        } else {
            response.sendRedirect("admin_manageAppointment.html?error=missing_fields");
        }
    }

    private void saveAppointment(HttpServletRequest request, String appointmentId, String patientName,
                                 String doctorName, String appointmentDate, String appointmentTime, String status) {
        String filePath = getServletContext().getRealPath(FILE_NAME);
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
            out.println(appointmentId + "," + patientName + "," + doctorName + "," + appointmentDate + "," + appointmentTime + "," + status);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        PrintWriter out = response.getWriter();
        List<String[]> appointments = new ArrayList<>();

        String filePath = getServletContext().getRealPath(FILE_NAME);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length == 6) {
                    appointments.add(data);
                }
            }
        } catch (FileNotFoundException e) {
            out.println("<p>No appointments found.</p>");
        } catch (IOException e) {
            e.printStackTrace();
            out.println("<p>Error loading appointment records.</p>");
        }

        // Bubble Sort to sort appointments by Date and Time
        for (int i = 0; i < appointments.size() - 1; i++) {
            for (int j = 0; j < appointments.size() - i - 1; j++) {
                String datetime1 = appointments.get(j)[3] + " " + appointments.get(j)[4];  // Date + Time
                String datetime2 = appointments.get(j + 1)[3] + " " + appointments.get(j + 1)[4];

                if (datetime1.compareTo(datetime2) > 0) {
                    String[] temp = appointments.get(j);
                    appointments.set(j, appointments.get(j + 1));
                    appointments.set(j + 1, temp);
                }
            }
        }

        // Display sorted appointment records
        out.println("<html><head><style>");
        out.println("table { width: 100%; border-collapse: collapse; background: rgba(30,30,50,0.95); border-radius: 10px; overflow: hidden; margin-top: 20px; }");
        out.println("th, td { padding: 14px 18px; text-align: center; border-bottom: 1px solid #444; color: #eee; }");
        out.println("th { background: #2c2c44; color: #61dafb; font-size: 16px; text-transform: uppercase; }");
        out.println("td { background: rgba(50,50,70,0.9); font-size: 15px; }");
        out.println("</style></head><body>");
        out.println("<table>");
        out.println("<tr><th>ID</th><th>Patient</th><th>Doctor</th><th>Date</th><th>Time</th><th>Status</th></tr>");

        if (appointments.isEmpty()) {
            out.println("<tr><td colspan='6'>No appointments available.</td></tr>");
        } else {
            for (String[] appointment : appointments) {
                out.println("<tr>");
                for (String item : appointment) {
                    out.println("<td>" + escapeHtml(item) + "</td>");
                }
                out.println("</tr>");
            }
        }

        out.println("</table></body></html>");
    }

    private boolean isNotEmpty(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#x27;");
    }
}
