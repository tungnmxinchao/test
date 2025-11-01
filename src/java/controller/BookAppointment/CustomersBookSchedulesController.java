/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.BookAppointment;

import dal.AppointmentsDao;
import dal.NotificationsDao;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import model.Appointments;
import model.Doctor;
import model.Patients;
import model.Service;

/**
 *
 * @author TNO
 */
@WebServlet(name = "CustomersBookSchedulesController", urlPatterns = {"/CustomersBookSchedules"})
public class CustomersBookSchedulesController extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            request.setCharacterEncoding("UTF-8");
            response.setCharacterEncoding("UTF-8");

            // ==== Lấy patientId: ưu tiên session; fallback tạm thời = 3 ====
            Integer patientId = null;
            try {
                // ví dụ: bạn lưu Users hoặc Patients trong session
                Object patientObj = request.getSession().getAttribute("patient");
                if (patientObj instanceof Patients) {
                    patientId = ((Patients) patientObj).getPatientID();
                } else {
                    Object userObj = request.getSession().getAttribute("account");
                    // nếu account là Users, bạn có thể tra patientId theo userId qua PatientDao (nếu có)
                    // patientId = new PatientDao().getPatientIdByUserId(((Users)userObj).getUserId());
                }
            } catch (Exception ignore) {}
            if (patientId == null) patientId = 1; // fallback hiện tại

            // ==== Lấy tham số từ form ====
            int doctorId  = Integer.parseInt(request.getParameter("doctorId"));
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));

            String dateStr = safeTrim(request.getParameter("appointmentDate"));
            String slot    = safeTrim(request.getParameter("slot"));
            String notes   = safeTrim(request.getParameter("notes"));

            if (dateStr == null || dateStr.isEmpty()) {
                request.setAttribute("error", "Thiếu ngày hẹn (appointmentDate).");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            if (slot == null || !slot.contains("-")) {
                request.setAttribute("error", "Dữ liệu khung giờ không hợp lệ.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            // ==== Tách start - end ====
            String[] timeParts = slot.split("-");
            if (timeParts.length < 2) {
                request.setAttribute("error", "Dữ liệu khung giờ không hợp lệ.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }
            String startStr = timeParts[0].trim();
            String endStr   = timeParts[1].trim();

            // ==== Parse sang SQL types ====
            java.sql.Date appointmentDate = java.sql.Date.valueOf(dateStr);
            java.sql.Time startTime       = java.sql.Time.valueOf(startStr);
            java.sql.Time endTime         = java.sql.Time.valueOf(endStr);

            // ==== Tạo entity Appointments ====
            Appointments a = new Appointments();
            a.setPatientId(new Patients(patientId));
            a.setDoctorId(new Doctor(doctorId));
            a.setServiceId(new Service(serviceId));
            a.setAppointmentDate(appointmentDate);
            a.setStartTime(startTime);
            a.setEndTime(endTime);
            a.setNotes(notes);
            a.setStatus("Scheduled");

            // ==== Insert ====
            AppointmentsDao appointmentDAO = new AppointmentsDao();
            Integer newId = appointmentDAO.insertAppointment(a);

            if (newId == null) {
                request.setAttribute("error", "Không thể đặt lịch. Có thể bác sĩ đã bận trong khung giờ này.");
                request.getRequestDispatcher("/error.jsp").forward(request, response);
                return;
            }

            // ==== Notifications (tuỳ chọn: lấy tên thật từ DB) ====
            NotificationsDao notiDAO = new NotificationsDao();
            String doctorName  = "Bác sĩ #" + doctorId;
            String patientName = "Bệnh nhân #" + patientId;

            String messageForPatient = "Bạn đã đặt lịch khám với " + doctorName
                    + " vào ngày " + dateStr + " lúc " + startStr + ".";
            String messageForDoctor  = patientName + " đã đặt lịch khám vào ngày "
                    + dateStr + " lúc " + startStr + ".";

            notiDAO.insert(patientId, "Xác nhận đặt lịch", messageForPatient, "Appointment");
            notiDAO.insert(doctorId,  "Lịch hẹn mới",      messageForDoctor,  "Appointment");

            // ==== Thành công ====
            request.setAttribute("appointmentId", newId);
            request.getRequestDispatcher("/appointment-success.jsp").forward(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Đã xảy ra lỗi trong quá trình đặt lịch.");
            request.getRequestDispatcher("/error.jsp").forward(request, response);
        }

    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
    
    private String safeTrim(String s) {
        return s == null ? null : s.trim();
    }

}
