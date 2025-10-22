<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
                <!DOCTYPE html>
                <html lang="en">

                <head>
                    <meta charset="utf-8" />
                    <meta http-equiv="X-UA-Compatible" content="IE=edge" />
                    <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
                    <meta name="description" content="Hỏi Dân IT - Dự án FURNITURE STORE" />
                    <meta name="author" content="Hỏi Dân IT" />
                    <title>Dashboard - FURNITURE STORE</title>
                    <link href="/css/styles.css" rel="stylesheet" />
                    <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js"
                        crossorigin="anonymous"></script>
                </head>

                <body class="sb-nav-fixed">
                    <!-- Cách này để tái sử dụng header bằng cách truy cập vào file header.jsp ở trong folder layout trong WEB-INF/view/dashboard, có thể search cả câu lệnh để đọc hiểu thêm -->
                    <jsp:include page="../layout/header.jsp" />

                    <div id="layoutSidenav">
                        <jsp:include page="../layout/sidebar.jsp" />
                        <div id="layoutSidenav_content">
                            <main>
                                <div class="container-fluid px-4">
                                    <h1 class="mt-4">Manage Orders</h1>
                                    <!-- cái breadcrumb này sẽ đóng vai trò điều hướng trang hiển thị như này :
                                Home/Chi Tiết Sản Phẩm Mà lúc này khi bấm vào có thể chuyển qua chuyển lại (kiểu thế) 
                                -->
                                    <nav aria-label="breadcrumb">
                                        <ol class="breadcrumb">
                                            <li class="breadcrumb-item">
                                                <a href="/admin">Dashboard</a>
                                            </li>
                                            <li class="breadcrumb-item active" aria-current="page">
                                                Order
                                            </li>
                                        </ol>
                                    </nav>
                                </div>
                                <div class="mt-5">
                                    <div class="row">
                                        <div class="col-12 mx-auto">
                                            <div class="d-flex justify-content-between">
                                                <h3>Table Orders</h3>
                                            </div>
                                            <hr />
                                            <table class="table table-bordered table-hover">
                                                <thead>
                                                    <tr>
                                                        <th>ID</th>
                                                        <th>Total Price</th>
                                                        <th>User</th>
                                                        <th>Status</th>
                                                        <th>Action</th>
                                                    </tr>
                                                </thead>
                                                <tbody>
                                                    <c:forEach var="order" items="${orders}">
                                                        <tr>
                                                            <th>${order.id}</th>
                                                            <td>
                                                                <fmt:formatNumber value="${order.totalPrice}" />
                                                            </td>
                                                            <td>${order.user.role.name}</td>
                                                            <td>${order.status}</td>
                                                            <td>
                                                                <a href="/admin/order/${order.id}"
                                                                    class="btn btn-success">
                                                                    View
                                                                </a>
                                                                <a href="/admin/order/update/${order.id}"
                                                                    class="btn btn-warning mx-2">
                                                                    Update
                                                                </a>
                                                                <a href="/admin/order/delete/${order.id}"
                                                                    class="btn btn-danger">
                                                                    Delete
                                                                </a>
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </div>
                                    </div>
                                </div>
                            </main>
                            <jsp:include page="../layout/footer.jsp" />
                        </div>
                    </div>
                    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                        crossorigin="anonymous"></script>
                    <script src="client/js/scripts.js"></script>

                </body>

                </html>