<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
    <html lang="en">

    <head>
        <meta charset="UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>View Profile</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/css/bootstrap.min.css" rel="stylesheet"
            integrity="sha384-1BmE4kWBq78iYhFldvKuhfTAU6auU8tT94WrHftjDbrCEXSU1oBoqyl2QvZ6jIW3" crossorigin="anonymous">
        <link rel="stylesheet" href="../css/viewprofile.css">
    </head>

    <body>
        <section class="bg-light d-flex justify-content-center align-items-center" style="margin-top: 40px;">
            <div class=" container">
                <div class="row justify-content-center">
                    <div class="col-lg-8">
                        <div class="card">
                            <div class="card-body">
                                <div class="row align-items-center">
                                    <div class="col-lg-4 text-center">
                                        <img src="/images/avatar/${user.avatar}" alt="Avatar" class="profile-img">
                                        <div class="btn btn-secondary mt-3">
                                            <a href="/update-profile/${user.id}">Cập nhật</a>
                                        </div>
                                    </div>
                                    <div class="col-lg-8 profile-info">
                                        <h1 style="font-weight: bold;">${user.fullName}</h1>


                                        <ul class="profile-details list-unstyled mt-4">
                                            <li><span>Email:</span> ${user.email}</li>
                                            <li><span>Số điện thoại:</span> ${user.phone}</li>
                                            <li><span>Địa chỉ:</span> ${user.address}</li>
                                            <li><span>Vai trò:</span> ${user.role.name}</li>
                                        </ul>

                                        <div class="btn btn-primary mt-3">
                                            <a href="/">Trở lại trang chủ</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </section>

        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.1.3/dist/js/bootstrap.bundle.min.js"></script>
    </body>

    </html>