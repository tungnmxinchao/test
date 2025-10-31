/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import dto.OrderDto;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import model.Orders;
import model.Patients;
import model.Prescriptions;
import model.Users;
import java.sql.*;
/**
 *
 * @author TNO
 */
public class OrderDao {

    public List<Orders> filterOrders(OrderDto o) {
        List<Orders> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                o.OrderID,
                o.PatientID,
                o.PrescriptionID,
                o.OrderDate,
                o.TotalAmount,
                o.Status,
                o.PaymentMethod,
                o.PaymentStatus,
                u.FullName AS PatientName     -- Lấy tên Bệnh nhân
            FROM dbo.Orders o
            JOIN dbo.Patients p ON o.PatientID = p.PatientID
            JOIN dbo.Users u ON p.UserID = u.UserID
            WHERE 1=1
        """);

        // 1. Xây dựng điều kiện WHERE động
        List<Object> params = new ArrayList<>();

        // Lọc theo Patient ID
        if (o.getPatientId() != null) {
            sql.append(" AND o.PatientID = ? ");
            params.add(o.getPatientId());
        }

        // Lọc theo Patient Name
        if (o.getPatientName() != null && !o.getPatientName().isBlank()) {
            sql.append(" AND u.FullName LIKE ? ");
            params.add("%" + o.getPatientName().trim() + "%");
        }

        // Lọc theo Prescription ID
        if (o.getPrescriptionId() != null) {
            sql.append(" AND o.PrescriptionID = ? ");
            params.add(o.getPrescriptionId());
        }

        // Lọc theo OrderDate From/To
        if (o.getOrderDateFrom() != null) {
            sql.append(" AND CAST(o.OrderDate AS DATE) >= ? ");
            params.add(o.getOrderDateFrom());
        }
        if (o.getOrderDateTo() != null) {
            sql.append(" AND CAST(o.OrderDate AS DATE) <= ? ");
            params.add(o.getOrderDateTo());
        }

        // Lọc theo Amount From/To
        if (o.getTotalAmountFrom() != null) {
            sql.append(" AND o.TotalAmount >= ? ");
            params.add(o.getTotalAmountFrom());
        }
        if (o.getTotalAmountTo() != null) {
            sql.append(" AND o.TotalAmount <= ? ");
            params.add(o.getTotalAmountTo());
        }

        // Lọc theo Status
        if (o.getStatus() != null && !o.getStatus().isBlank()) {
            sql.append(" AND o.Status = ? ");
            params.add(o.getStatus().trim());
        }

        // Lọc theo Payment Status
        if (o.getPaymentStatus() != null && !o.getPaymentStatus().isBlank()) {
            sql.append(" AND o.PaymentStatus = ? ");
            params.add(o.getPaymentStatus().trim());
        }

        // 2. Xây dựng ORDER BY
        sql.append(" ORDER BY o.OrderDate ");
        sql.append(o.isSortMode() ? "ASC" : "DESC"); // Giả sử sortMode=true là ASC, false là DESC

        // 3. Xây dựng Phân trang (Pagination)
        if (o.isPaginationMode()) {
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        }

        // 4. Thực thi truy vấn
        try (Connection connection = new DBContext().connection; PreparedStatement ps = connection.prepareStatement(sql.toString())) {

            int i = 1;
            // Gán các tham số cho WHERE
            for (Object param : params) {
                if (param instanceof Integer) {
                    ps.setInt(i++, (Integer) param);
                } else if (param instanceof String) {
                    ps.setString(i++, (String) param);
                } else if (param instanceof Date) {
                    ps.setDate(i++, (Date) param);
                } else if (param instanceof BigDecimal) {
                    ps.setBigDecimal(i++, (BigDecimal) param);
                }
            }

            // Gán tham số cho Phân trang
            if (o.isPaginationMode()) {
                int offset = (o.getPage() - 1) * o.getSize();
                int limit = o.getSize();
                ps.setInt(i++, offset);
                ps.setInt(i++, limit);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs)); // Dùng hàm mapRow để ánh xạ
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private Orders mapRow(ResultSet rs) throws SQLException {
        // --- 1. Ánh xạ Users (Thông tin bệnh nhân) ---
        Users user = new Users();
        user.setUserId(rs.getInt("UserID"));
        user.setUserName(rs.getString("Username"));
        user.setPassWord(rs.getString("PasswordHash")); // Lưu ý: Tên cột DB là PasswordHash
        user.setEmail(rs.getString("Email"));
        user.setFullName(rs.getString("FullName"));
        user.setPhoneNumber(rs.getString("PhoneNumber"));
        user.setDateOfBirth(rs.getDate("DateOfBirth"));
        user.setGender(rs.getString("Gender"));
        user.setAddress(rs.getString("Address"));
        user.setRole(rs.getString("Role"));
        user.setCreateDate(rs.getDate("CreatedDate"));

        // --- 2. Ánh xạ Patients (Thông tin bệnh án cơ bản) ---
        Patients patient = new Patients();
        patient.setPatientID(rs.getInt("PatientID"));
        patient.setUserID(user); // Gán đối tượng Users đã ánh xạ
        patient.setBloodType(rs.getString("BloodType"));
        patient.setAllergies(rs.getString("Allergies"));
        patient.setMedicalHistory(rs.getString("MedicalHistory"));
        patient.setInsuranceInfo(rs.getString("InsuranceInfo"));
        patient.setEmergencyContactName(rs.getString("EmergencyContactName"));
        patient.setEmergencyContactPhone(rs.getString("EmergencyContactPhone"));

        // --- 3. Ánh xạ Prescriptions (Thông tin đơn thuốc) ---
        // Lưu ý: Cần JOIN vào bảng Prescriptions để lấy đủ các cột này
        Prescriptions prescription = new Prescriptions();
        prescription.setPrescriptionId(rs.getInt("PrescriptionID"));
        // Giả sử các cột sau được SELECT từ bảng Prescriptions:
        prescription.setIssueDate(rs.getTimestamp("IssueDate"));
        prescription.setInstructions(rs.getString("Instructions"));

        // --- 4. Ánh xạ Orders (Thông tin hóa đơn) ---
        Orders order = new Orders();
        order.setOrderId(rs.getInt("OrderID"));
        order.setOrderDate(rs.getTimestamp("OrderDate"));
        order.setTotalAmount(rs.getBigDecimal("TotalAmount"));
        order.setStatus(rs.getString("Status"));
        order.setPaymentMethod(rs.getString("PaymentMethod"));
        order.setPaymentStatus(rs.getString("PaymentStatus"));

        // Gán các đối tượng lồng nhau vào Orders
        order.setPatients(patient); // Gán đối tượng Patients đã ánh xạ
        order.setPrescriptions(prescription); // Gán đối tượng Prescriptions đã ánh xạ

        return order;
    }

}
