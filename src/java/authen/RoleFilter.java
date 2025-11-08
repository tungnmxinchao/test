/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package authen;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Users;

/**
 *
 * @author TNO
 */
@WebFilter(urlPatterns = {
    "/manageMedications",
    "/manageOrder",
    "/report",
    "/manageSchedule",
    "/manageService",
    "/manageUser",
    "/patientsToday",
    "/lookUpAppointments",
    "/bookAppointmentsDirectly"
})
public class RoleFilter implements Filter {

    private Map<String, List<String>> roleAccessMap;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        roleAccessMap = new HashMap<>();

        // Doctor có thể vào
        roleAccessMap.put("Doctor", Arrays.asList(
                "/patientsToday",
                "/manageMedications",
                "/manageService"
        ));

        // Receptionist có thể vào
        roleAccessMap.put("Receptionist", Arrays.asList(
                "/lookUpAppointments",
                "/bookAppointmentsDirectly",
                "/manageService",
                "/manageOrder"
        ));

        //Admin có toàn quyền (cho phép tất cả)
        roleAccessMap.put("Admin", Collections.singletonList("*"));
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;
        HttpSession session = req.getSession(false);

        String path = req.getServletPath();
        if (path.startsWith("/login") || path.startsWith("/register")
                || path.startsWith("/home")
                || path.startsWith("/css") || path.startsWith("/js")
                || path.startsWith("/images") || path.startsWith("/uploads")) {
            // Cho phép truy cập tài nguyên tĩnh hoặc trang login
            chain.doFilter(request, response);
            return;
        }

        // Kiểm tra session
        if (session == null || session.getAttribute("user") == null) {
            res.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        Users user = (Users) session.getAttribute("user");
        String role = user.getRole();

        // Nếu là admin → bỏ qua kiểm tra
        if ("Admin".equalsIgnoreCase(role)) {
            chain.doFilter(request, response);
            return;
        }

        // Lấy danh sách path được phép với vai trò hiện tại
        List<String> allowedPaths = roleAccessMap.get(role);

        if (allowedPaths != null) {
            for (String allowed : allowedPaths) {
                if (path.startsWith(allowed)) {
                    chain.doFilter(request, response);
                    return;
                }
            }
        }

        // Nếu không có quyền → chặn
        res.sendRedirect(req.getContextPath() + "/login");
    }

    @Override
    public void destroy() {
    }
}
