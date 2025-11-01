<%-- 
    Document   : sidebar
    Created on : Nov 1, 2025, 7:56:01 PM
    Author     : TNO
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<div class="sidebar">
    <h2>Tiếp đón</h2>
    <a href="${pageContext.request.contextPath}/lookUpAppointments" class="active">Tra cứu lịch hẹn</a>
    <a href="${pageContext.request.contextPath}/patientsToday">Bệnh nhân hôm nay</a>
    <a href="${pageContext.request.contextPath}/waitingList">Danh sách chờ</a>
    <a href="${pageContext.request.contextPath}/printBill">In hóa đơn</a>
    <a href="${pageContext.request.contextPath}/logout">Đăng xuất</a>
</div>