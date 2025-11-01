/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import dto.ScheduleDto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Doctor;
import model.Schedules;
import model.Users;

/**
 *
 * @author Nguyen Dinh Giap
 */
public class ScheduleDao extends DBContext {

    //ham lay lich trinh theo id
    public Schedules getScheduleById(int scheduleId) {
        //cau lenh sql lay du lieu tu bang schedule
        String sql = """
            SELECT ScheduleID, DoctorID, DayOfWeek, StartTime, EndTime,
                   IsAvailable, MaxAppointments, ValidFrom, ValidTo,
                   RequiresApproval, IsApproved, ApprovedBy, ApprovedDate, CreatedDate
            FROM dbo.Schedules
            WHERE ScheduleID = ?
        """;
        //mo ket noi
        try (Connection connection = new DBContext().connection; PreparedStatement ps = connection.prepareStatement(sql)) {
            //set gia tri scheduleId
            ps.setInt(1, scheduleId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    //map du lieu tu resultset sang object schedules
                    Schedules s = new Schedules();
                    s.setScheduleId(rs.getInt("ScheduleID"));
                    Object doctorIdObj = rs.getObject("DoctorID");
                    if (doctorIdObj != null) {
                        Doctor d = new Doctor();
                        //ep object kieu ve int
                        d.setDoctorID((Integer) doctorIdObj);
                        s.setDoctorId(d);
                    } else {
                        s.setDoctorId(null);
                    }

                    s.setDayOfWeek(rs.getInt("DayOfWeek"));
                    s.setStartTime(rs.getTime("StartTime"));
                    s.setEndTime(rs.getTime("EndTime"));
                    s.setIsAvailable(rs.getBoolean("IsAvailable"));
                    s.setMaxAppointments(rs.getInt("MaxAppointments"));
                    s.setValidFrom(rs.getDate("ValidFrom"));
                    s.setValidTo(rs.getDate("ValidTo"));
                    s.setRequiresApproval(rs.getBoolean("RequiresApproval"));
                    s.setIsApproved(rs.getBoolean("IsApproved"));
                    Object apByObj = rs.getObject("ApprovedBy");
                    if (apByObj != null) {
                        Users u = new Users();
                        //ep object kieu ve int
                        u.setUserId((Integer) apByObj);
                        s.setApprovedBy(u);
                    } else {
                        s.setApprovedBy(null);
                    }
                    s.setApprovedDate(rs.getTimestamp("ApprovedDate"));
                    s.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    return s;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        //ko tim thay tra ve null
        return null;
    }

    public List<Schedules> filterSchedules(ScheduleDto f) {
        List<Schedules> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT [ScheduleID], [DoctorID], [DayOfWeek], [StartTime], [EndTime],
                   [IsAvailable], [MaxAppointments], [ValidFrom], [ValidTo],
                   [RequiresApproval], [IsApproved], [ApprovedBy],
                   [ApprovedDate], [CreatedDate]
            FROM [dbo].[Schedules]
            WHERE 1=1
        """);

        List<Object> params = new ArrayList<>();

        // ====== Điều kiện lọc linh hoạt ======
        if (f.getDoctorId() != null) {
            sql.append(" AND DoctorID = ?");
            params.add(f.getDoctorId());
        }
        if (f.getDayOfWeek() != null) {
            sql.append(" AND DayOfWeek = ?");
            params.add(f.getDayOfWeek());
        }
        if (f.getStartTime() != null) {
            sql.append(" AND StartTime >= CAST(? AS time(0))");
            params.add(f.getStartTime());
        }
        if (f.getEndTime() != null) {
            sql.append(" AND EndTime <= CAST(? AS time(0))");
            params.add(f.getEndTime());
        }
        if (f.getIsAvailable() != null) {
            sql.append(" AND IsAvailable = ?");
            params.add(f.getIsAvailable());
        }

        // ====== Sắp xếp & phân trang ======
        sql.append(" ORDER BY ScheduleID ");
        sql.append(f.isSortMode() ? "ASC" : "DESC");

        if (f.isPaginationMode()) {
            int offset = (f.getPage() - 1) * f.getSize();
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY");
            params.add(offset);
            params.add(f.getSize());
        }

        // ====== Thực thi ======
        try (Connection c = new DBContext().connection; PreparedStatement ps = c.prepareStatement(sql.toString())) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Schedules s = new Schedules();
                    s.setScheduleId(rs.getInt("ScheduleID"));

                    Doctor d = new Doctor();
                    d.setDoctorID(rs.getInt("DoctorID"));
                    s.setDoctorId(d);

                    s.setDayOfWeek(rs.getInt("DayOfWeek"));
                    s.setStartTime(rs.getTime("StartTime"));
                    s.setEndTime(rs.getTime("EndTime"));
                    s.setIsAvailable(rs.getBoolean("IsAvailable"));
                    s.setMaxAppointments(rs.getInt("MaxAppointments"));
                    s.setValidFrom(rs.getDate("ValidFrom"));
                    s.setValidTo(rs.getDate("ValidTo"));
                    s.setRequiresApproval(rs.getBoolean("RequiresApproval"));
                    s.setIsApproved(rs.getBoolean("IsApproved"));

                    Users u = new Users();
                    u.setUserId(rs.getInt("ApprovedBy"));
                    s.setApprovedBy(u);

                    s.setApprovedDate(rs.getTimestamp("ApprovedDate"));
                    s.setCreatedDate(rs.getTimestamp("CreatedDate"));
                    list.add(s);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

}
