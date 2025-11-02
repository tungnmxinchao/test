/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dal;

import dto.MedicationDto;
import java.util.ArrayList;
import java.util.List;
import model.Medications;
import java.sql.*;

/**
 *
 * @author TNO
 */
public class MedicationsDao {

    public List<Medications> filterMedications(MedicationDto dto) {
        List<Medications> list = new ArrayList<>();

        StringBuilder sql = new StringBuilder("""
            SELECT
                m.[MedicationID],
                m.[MedicationName],
                m.[Description],
                m.[Price],
                m.[StockQuantity],
                m.[ExpiryDate],
                m.[Manufacturer],
                m.[IsActive]
            FROM [dbo].[Medications] m
            WHERE 1 = 1
        """);

        // Điều kiện lọc
        if (dto.getMedicationId() != null) {
            sql.append(" AND m.MedicationID = ? \n");
        }
        if (dto.getName() != null && !dto.getName().isBlank()) {
            sql.append(" AND m.MedicationName LIKE ? \n");
        }
        if (dto.getPriceFrom() != null) {
            sql.append(" AND m.Price >= ? \n");
        }
        if (dto.getPriceTo() != null) {
            sql.append(" AND m.Price <= ? \n");
        }
        if (dto.getExpiryDateFrom() != null) {
            sql.append(" AND m.ExpiryDate >= ? \n");
        }
        if (dto.getExpiryDateTo() != null) {
            sql.append(" AND m.ExpiryDate <= ? \n");
        }
        if (dto.getManufacturer() != null && !dto.getManufacturer().isBlank()) {
            sql.append(" AND m.Manufacturer LIKE ? \n");
        }
        if (dto.getIsActive() != null) {
            sql.append(" AND m.IsActive = ? \n");
        }

        // Sắp xếp
        if (dto.isSortMode()) {
            sql.append(" ORDER BY m.ExpiryDate ASC, m.MedicationName ASC \n");
        } else {
            sql.append(" ORDER BY m.MedicationID DESC \n");
        }

        // Phân trang
        if (dto.isPaginationMode()) {
            sql.append(" OFFSET ? ROWS FETCH NEXT ? ROWS ONLY ");
        }

        try (Connection conn = new DBContext().connection; PreparedStatement ps = conn.prepareStatement(sql.toString())) {

            int i = 1;
            if (dto.getMedicationId() != null) {
                ps.setInt(i++, dto.getMedicationId());
            }
            if (dto.getName() != null && !dto.getName().isBlank()) {
                ps.setString(i++, "%" + dto.getName().trim() + "%");
            }
            if (dto.getPriceFrom() != null) {
                ps.setBigDecimal(i++, dto.getPriceFrom());
            }
            if (dto.getPriceTo() != null) {
                ps.setBigDecimal(i++, dto.getPriceTo());
            }
            if (dto.getExpiryDateFrom() != null) {
                ps.setDate(i++, dto.getExpiryDateFrom());
            }
            if (dto.getExpiryDateTo() != null) {
                ps.setDate(i++, dto.getExpiryDateTo());
            }
            if (dto.getManufacturer() != null && !dto.getManufacturer().isBlank()) {
                ps.setString(i++, "%" + dto.getManufacturer().trim() + "%");
            }
            if (dto.getIsActive() != null) {
                ps.setBoolean(i++, dto.getIsActive());
            }

            if (dto.isPaginationMode()) {
                int page = Math.max(1, dto.getPage());
                int size = Math.max(1, dto.getSize());
                ps.setInt(i++, (page - 1) * size);
                ps.setInt(i++, size);
            }

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

    private Medications mapRow(ResultSet rs) throws SQLException {
        Medications m = new Medications();
        m.setMedicationId(rs.getInt("MedicationID"));
        m.setMedicationName(rs.getString("MedicationName"));
        m.setDescription(rs.getString("Description"));
        m.setPrice(rs.getBigDecimal("Price"));            // BigDecimal
        m.setStockQuantity(rs.getInt("StockQuantity"));
        m.setExpiryDate(rs.getDate("ExpiryDate"));        // java.sql.Date
        m.setManufacturer(rs.getString("Manufacturer"));
        m.setIsActive(rs.getBoolean("IsActive"));         // boolean
        return m;
    }

    public Medications getMedicationById(Integer medicationId) {
        if (medicationId == null) {
            return null;
        }

        String sql = """
        SELECT
            m.[MedicationID],
            m.[MedicationName],
            m.[Description],
            m.[Price],
            m.[StockQuantity],
            m.[ExpiryDate],
            m.[Manufacturer],
            m.[IsActive]
        FROM [dbo].[Medications] m
        WHERE m.MedicationID = ?
    """;

        try (Connection conn = new DBContext().connection; PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, medicationId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapRow(rs); // dùng lại phương thức mapRow
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null; // không tìm thấy
    }

}
