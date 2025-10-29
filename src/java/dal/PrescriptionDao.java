/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import dto.PrescriptionDto;
import java.util.ArrayList;
import java.util.List;
import model.Prescriptions;
import java.sql.*;
import model.Doctor;
import model.MedicalRecords;
import model.Medications;
import model.PrescriptionDetails;
import model.Users;

/**
 *
 * @author TNO
 */
public class PrescriptionDao {

    public List<PrescriptionDetails> findByPrescriptionId(int prescriptionId) {
        List<PrescriptionDetails> list = new ArrayList<>();

        String sql = """
            SELECT pd.PrescriptionDetailID,
                   pd.PrescriptionID,
                   pd.MedicationID,
                   pd.Dosage,
                   pd.Quantity,
                   pd.Duration,
                   m.MedicationName,
                   m.Description,
                   m.Price,
                   m.StockQuantity,
                   m.ExpiryDate,
                   m.Manufacturer,
                   m.IsActive
            FROM PrescriptionDetails pd
            JOIN Medications m ON pd.MedicationID = m.MedicationID
            WHERE pd.PrescriptionID = ?
        """;

        try (Connection conn = new DBContext().connection; PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, prescriptionId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    // Ánh xạ từ ResultSet sang model PrescriptionDetails
    private PrescriptionDetails mapRow(ResultSet rs) throws SQLException {
        PrescriptionDetails pd = new PrescriptionDetails();
        pd.setPrescriptionDetailId(rs.getInt("PrescriptionDetailID"));

        // Prescription (chỉ set ID)
        Prescriptions p = new Prescriptions();
        p.setPrescriptionId(rs.getInt("PrescriptionID"));
        pd.setPrescriptionId(p);

        // Medication (gán đầy đủ thông tin)
        Medications m = new Medications();
        m.setMedicationId(rs.getInt("MedicationID"));
        m.setMedicationName(rs.getString("MedicationName"));
        m.setDescription(rs.getString("Description"));
        m.setPrice(rs.getBigDecimal("Price"));
        m.setStockQuantity(rs.getInt("StockQuantity"));
        m.setExpiryDate(rs.getDate("ExpiryDate"));
        m.setManufacturer(rs.getString("Manufacturer"));
        m.setIsActive(rs.getBoolean("IsActive"));
        pd.setMedications(m);

        pd.setDosage(rs.getString("Dosage"));
        pd.setQuantity(rs.getInt("Quantity"));
        pd.setDuration(rs.getString("Duration"));

        return pd;
    }

    public List<Prescriptions> filterPrescription(PrescriptionDto dto) {
        List<Prescriptions> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT 
                p.[PrescriptionID],
                p.[RecordID],
                p.[DoctorID],
                p.[IssueDate],
                p.[Instructions],
                
                -- Doctor info
                d.[Specialization],
                d.[LicenseNumber],
                d.[YearsOfExperience],
                d.[Education],
                d.[Biography],
                d.[ConsultationFee],
                u.[UserID],
                u.[FullName] AS DoctorName,
                u.[Email] AS DoctorEmail,
                
                -- MedicalRecord info
                mr.[AppointmentID],
                mr.[Diagnosis],
                mr.[Symptoms],
                mr.[TreatmentPlan],
                mr.[FollowUpDate],
                mr.[CreatedDate]
            FROM [dbo].[Prescriptions] p
            LEFT JOIN [dbo].[Doctors] d ON p.[DoctorID] = d.[DoctorID]
            LEFT JOIN [dbo].[Users] u ON d.[UserID] = u.[UserID]
            LEFT JOIN [dbo].[MedicalRecords] mr ON p.[RecordID] = mr.[RecordID]
            LEFT JOIN [dbo].[Appointments] a ON mr.[AppointmentID] = a.[AppointmentID]
            LEFT JOIN [dbo].[Patients] pat ON a.[PatientID] = pat.[PatientID]
            WHERE 1 = 1
        """);

        //Điều kiện lọc động
        if (dto.getRecordId() != null) {
            sql.append(" AND p.RecordID = ?\n");
        }
        if (dto.getDoctorId() != null) {
            sql.append(" AND p.DoctorID = ?\n");
        }
        if (dto.getPatientId() != null) {
            sql.append(" AND pat.PatientID = ?\n");
        }
        if (dto.getIssueDateFrom() != null) {
            sql.append(" AND p.IssueDate >= ?\n");
        }
        if (dto.getIssueDateTo() != null) {
            sql.append(" AND p.IssueDate <= ?\n");
        }
        if (dto.getInstructions() != null && !dto.getInstructions().isBlank()) {
            sql.append(" AND p.Instructions LIKE ?\n");
        }

        // 🔢 Sắp xếp & phân trang
        if (dto.isSortMode()) {
            sql.append(" ORDER BY p.IssueDate DESC\n");
        }
        if (dto.isPaginationMode()) {
            if (!dto.isSortMode()) {
                sql.append(" ORDER BY p.IssueDate DESC\n");
            }
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
        }

        try (Connection conn = new DBContext().connection; PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 1;
            if (dto.getRecordId() != null) {
                ps.setInt(i++, dto.getRecordId());
            }
            if (dto.getDoctorId() != null) {
                ps.setInt(i++, dto.getDoctorId());
            }
            if (dto.getPatientId() != null) {
                ps.setInt(i++, dto.getPatientId());
            }
            if (dto.getIssueDateFrom() != null) {
                ps.setTimestamp(i++, dto.getIssueDateFrom());
            }
            if (dto.getIssueDateTo() != null) {
                ps.setTimestamp(i++, dto.getIssueDateTo());
            }
            if (dto.getInstructions() != null && !dto.getInstructions().isBlank()) {
                ps.setString(i++, "%" + dto.getInstructions().trim() + "%");
            }

            if (dto.isPaginationMode()) {
                int page = Math.max(1, dto.getPage());
                int size = Math.max(1, dto.getSize());
                ps.setInt(i++, (page - 1) * size);
                ps.setInt(i++, size);
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapJoinedRow(rs));
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    private Prescriptions mapJoinedRow(ResultSet rs) throws SQLException {
        Prescriptions p = new Prescriptions();
        p.setPrescriptionId(rs.getInt("PrescriptionID"));
        p.setIssueDate(rs.getTimestamp("IssueDate"));
        p.setInstructions(rs.getString("Instructions"));

        // Medical Record
        MedicalRecords record = new MedicalRecords();
        record.setRecordId(rs.getInt("RecordID"));
        record.setDiagnosis(rs.getString("Diagnosis"));
        record.setSymptoms(rs.getString("Symptoms"));
        record.setTreatmentPlan(rs.getString("TreatmentPlan"));
        record.setFollowUpDate(rs.getDate("FollowUpDate"));
        record.setCreatedDate(rs.getTimestamp("CreatedDate"));
        p.setRecordId(record);

        // Doctor
        Doctor doctor = new Doctor();
        doctor.setDoctorID(rs.getInt("DoctorID"));
        doctor.setSpecialization(rs.getString("Specialization"));
        doctor.setLicenseNumber(rs.getString("LicenseNumber"));
        doctor.setYearsOfExperience(rs.getInt("YearsOfExperience"));
        doctor.setEducation(rs.getString("Education"));
        doctor.setBiography(rs.getString("Biography"));
        doctor.setConsultationFee(rs.getBigDecimal("ConsultationFee"));

        // User (tên bác sĩ)
        Users user = new Users();
        user.setUserId(rs.getInt("UserID"));
        user.setFullName(rs.getString("DoctorName"));
        user.setEmail(rs.getString("DoctorEmail"));
        doctor.setUserId(user);

        p.setDoctorId(doctor);
        return p;
    }
}
