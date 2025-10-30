/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller.BookAppointmentsDirectly;

import dal.AppointmentsDao;
import dal.DoctorDao;
import dal.PatientDao;
import dal.ScheduleDao;
import dal.ServiceDao;
import dto.AppointmentDto;
import dto.ScheduleDto;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.Doctor;
import model.Patients;
import model.Service;
import java.sql.*;
import model.Appointments;
import model.Schedules;

/**
 *
 * @author TNO
 */
@WebServlet(name = "BookAppointmentsDirectlyController", urlPatterns = {"/bookAppointmentsDirectly"})
public class BookAppointmentsDirectlyController extends HttpServlet {

    private final AppointmentsDao appointmentsDao = new AppointmentsDao();
    private final DoctorDao doctorsDao = new DoctorDao();
    private final PatientDao patientsDao = new PatientDao();
    private final ServiceDao servicesDao = new ServiceDao();
    private final ScheduleDao scheduleDao = new ScheduleDao();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // Lấy danh sách dropdown
            List<Service> services = servicesDao.getAllServices();
            List<Patients> patients = patientsDao.getAllPatients();
            List<Doctor> doctors = doctorsDao.getAllDoctors();

            request.setAttribute("services", services);
            request.setAttribute("patients", patients);
            request.setAttribute("doctors", doctors);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Không thể tải dữ liệu form đặt lịch.");
        }

        request.getRequestDispatcher("/views/receptionist/create_appointment.jsp").forward(request, response);

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            int patientId = Integer.parseInt(request.getParameter("patientId"));
            int doctorId = Integer.parseInt(request.getParameter("doctorId"));
            int serviceId = Integer.parseInt(request.getParameter("serviceId"));
            Date appointmentDate = Date.valueOf(request.getParameter("appointmentDate"));
            Time startTime = Time.valueOf(request.getParameter("startTime") + ":00");
            Time endTime = Time.valueOf(request.getParameter("endTime") + ":00");
            String notes = request.getParameter("notes");

            //Kiểm tra bác sĩ có bị trùng lịch không
            AppointmentDto checkDoctor = new AppointmentDto();
            checkDoctor.setDoctorId(doctorId);
            checkDoctor.setAppointmentDate(appointmentDate);
            checkDoctor.setStartTime(startTime);
            checkDoctor.setEndTime(endTime);
            checkDoctor.setPaginationMode(false);

            List<Appointments> doctorBusyList = appointmentsDao.filterAppointment(checkDoctor);
            if (!doctorBusyList.isEmpty()) {
                request.setAttribute("errorMessage", "Bác sĩ đã có lịch trong khoảng thời gian này!");
                doGet(request, response);
                return;
            }

            //Kiểm tra bệnh nhân có bị trùng lịch không
            AppointmentDto checkPatient = new AppointmentDto();
            checkPatient.setPatientId(patientId);
            checkPatient.setAppointmentDate(appointmentDate);
            checkPatient.setStartTime(startTime);
            checkPatient.setEndTime(endTime);
            checkPatient.setPaginationMode(false);

            List<Appointments> patientBusyList = appointmentsDao.filterAppointment(checkPatient);
            if (!patientBusyList.isEmpty()) {
                request.setAttribute("errorMessage", "Bệnh nhân đã có lịch hẹn trong thời gian này!");
                doGet(request, response);
                return;
            }

            // =====Kiểm tra lịch làm việc hợp lệ của bác sĩ =====
            ScheduleDto checkSchedule = new ScheduleDto();
            checkSchedule.setDoctorId(doctorId);
            checkSchedule.setValidFrom(appointmentDate);
            checkSchedule.setValidTo(appointmentDate);
            checkSchedule.setStartTime(startTime);
            checkSchedule.setEndTime(endTime);
            checkSchedule.setIsAvailable(true);
            checkSchedule.setPaginationMode(false);

            List<Schedules> validSchedule = scheduleDao.filterSchedules(checkSchedule);
            if (validSchedule.isEmpty()) {
                request.setAttribute("errorMessage", "Lịch hẹn nằm ngoài thời gian làm việc của bác sĩ!");
                doGet(request, response);
                return;
            }

            Appointments appointment = new Appointments();

            // Tạo object Patient, Doctor, Service và set ID
            Patients patient = new Patients();
            patient.setPatientID(patientId);

            Doctor doctor = new Doctor();
            doctor.setDoctorID(doctorId);

            Service service = new Service();
            service.setServiceId(serviceId);

            // Gán vào appointment
            appointment.setPatientId(patient);
            appointment.setDoctorId(doctor);
            appointment.setServiceId(service);

            appointment.setAppointmentDate(appointmentDate);
            appointment.setStartTime(startTime);
            appointment.setEndTime(endTime);
            appointment.setStatus("Scheduled");
            appointment.setNotes(notes);

            // Thực hiện insert
            Integer newId = appointmentsDao.insertAppointment(appointment);

            if (newId != null) {
                request.setAttribute("successMessage", "Đặt lịch thành công! Mã lịch hẹn: " + newId);
            } else {
                request.setAttribute("errorMessage", "Không thể tạo lịch hẹn. Vui lòng kiểm tra lại.");
            }

            // Load lại dữ liệu dropdown
            doGet(request, response);

        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("errorMessage", "Lỗi khi tạo lịch hẹn. Vui lòng thử lại!");
            doGet(request, response);
        }

    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
