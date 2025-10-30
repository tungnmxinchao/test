/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.LookUpAppointments;

import dal.AppointmentsDao;
import dto.AppointmentDto;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.Appointments;
import java.sql.*;

/**
 *
 * @author TNO
 */
@WebServlet(name = "LookUpAppointmentsController", urlPatterns = {"/lookUpAppointments"})
public class LookUpAppointmentsController extends HttpServlet {

    private final AppointmentsDao appointmentsDAO = new AppointmentsDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        //Hiển thị trang tra cứu (ban đầu rỗng hoặc có sẵn danh sách)
        try {
            AppointmentDto filter = new AppointmentDto();
            filter.setPaginationMode(true);
            filter.setPage(1);
            filter.setSize(10);

            List<Appointments> appointments = appointmentsDAO.filterAppointment(filter);
            request.setAttribute("appointments", appointments);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Không thể tải danh sách lịch hẹn.");
        }

        request.getRequestDispatcher("lookup_appointments.jsp").forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");

        try {
            // Lấy tham số từ form
            String patientIdStr = request.getParameter("patientId");
            String doctorIdStr = request.getParameter("doctorId");
            String status = request.getParameter("status");
            String fromDateStr = request.getParameter("fromDate");
            String toDateStr = request.getParameter("toDate");
            String patientName = request.getParameter("patientName");
            String phoneNumber = request.getParameter("phoneNumber");
            String doctorName = request.getParameter("doctorName");

            AppointmentDto filter = new AppointmentDto();

            if (patientIdStr != null && !patientIdStr.isEmpty()) {
                filter.setPatientId(Integer.parseInt(patientIdStr));
            }
            if (doctorIdStr != null && !doctorIdStr.isEmpty()) {
                filter.setDoctorId(Integer.parseInt(doctorIdStr));
            }
            if (status != null && !status.isEmpty()) {
                filter.setStatus(status);
            }
            if (patientName != null && !patientName.isEmpty()) {
                filter.setPatientName(patientName.trim());
            }
            if (phoneNumber != null && !phoneNumber.isEmpty()) {
                filter.setPhoneNumber(phoneNumber.trim());
            }
            if (doctorName != null && !doctorName.isEmpty()) {
                filter.setDoctorName(doctorName.trim());
            }

            // Chuyển đổi ngày
            if (fromDateStr != null && !fromDateStr.isEmpty()) {
                filter.setAppointmentDateFrom(Date.valueOf(fromDateStr));
            }
            if (toDateStr != null && !toDateStr.isEmpty()) {
                filter.setAppointmentDateTo(Date.valueOf(toDateStr));
            }

            filter.setPaginationMode(true);
            filter.setPage(1);
            filter.setSize(10);

            // Gọi DAO để lấy dữ liệu
            List<Appointments> appointments = appointmentsDAO.filterAppointment(filter);

            // Gửi dữ liệu về JSP
            request.setAttribute("appointments", appointments);
            request.setAttribute("filter", filter);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("error", "Không thể tra cứu lịch hẹn.");
        }

        request.getRequestDispatcher("lookup_appointments.jsp").forward(request, response);

    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
