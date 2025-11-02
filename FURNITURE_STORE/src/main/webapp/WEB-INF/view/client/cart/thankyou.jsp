<!-- thankyou.jsp version giữ nguyên cấu trúc gốc, chỉ thêm class để dễ style -->
<%@page contentType="text/html" pageEncoding="UTF-8" %> <%@ taglib prefix="c"
uri="http://java.sun.com/jsp/jstl/core" %> <%@ taglib prefix="fmt"
uri="http://java.sun.com/jsp/jstl/fmt" %> <%@ taglib prefix="form"
uri="http://www.springframework.org/tags/form" %>
<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0" />
    <title>Đơn hàng - FURNITURE STORE</title>

    <!-- CSS LIBRARIES -->
    <link rel="preconnect" href="https://fonts.googleapis.com" />
    <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin />
    <link
      href="https://fonts.googleapis.com/css2?family=Open+Sans:wght@400;600&family=Raleway:wght@600;800&display=swap"
      rel="stylesheet"
    />
    <link
      rel="stylesheet"
      href="https://use.fontawesome.com/releases/v5.15.4/css/all.css"
    />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.4.1/font/bootstrap-icons.css"
      rel="stylesheet"
    />
    <link href="/client/lib/lightbox/css/lightbox.min.css" rel="stylesheet" />
    <link
      href="/client/lib/owlcarousel/assets/owl.carousel.min.css"
      rel="stylesheet"
    />

    <!-- PROJECT CSS -->
    <link href="/client/css/bootstrap.min.css" rel="stylesheet" />
    <link href="/client/css/style.css" rel="stylesheet" />
    <link href="/client/css/thankyou.css" rel="stylesheet" />
    <!-- thêm file riêng cho hiệu ứng -->

    <link rel="icon" type="image/png" href="/client/img/logo.png" />
  </head>
  <body>
    <!-- Spinner -->
    <div
      id="spinner"
      class="show w-100 vh-100 bg-white position-fixed translate-middle top-50 start-50 d-flex align-items-center justify-content-center"
    >
      <div class="spinner-grow text-primary" role="status"></div>
    </div>

    <jsp:include page="../layout/header.jsp" />

    <!-- THANK YOU MAIN -->
    <div class="thankyou-container container text-center">
      <div class="thankyou-box">
        <div class="thankyou-icon">
          <i class="fas fa-check-circle"></i>
        </div>
        <h1 class="thankyou-title">Cảm ơn bạn đã đặt hàng!</h1>
        <p class="thankyou-message">
          Đơn hàng của bạn đã được xác nhận thành công.
        </p>
        <button
          class="btn btn-primary rounded-pill px-5 py-3 mt-3 continue-btn"
        >
          Tiếp tục mua sắm
        </button>
      </div>
    </div>

    <jsp:include page="../layout/feature.jsp" />
    <jsp:include page="../layout/footer.jsp" />

    <a
      href="#"
      class="btn btn-primary border-3 border-primary rounded-circle back-to-top"
    >
      <i class="fa fa-arrow-up"></i>
    </a>

    <!-- JS LIBRARIES -->
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.6.4/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.0.0/dist/js/bootstrap.bundle.min.js"></script>
    <script src="/client/lib/easing/easing.min.js"></script>
    <script src="/client/lib/waypoints/waypoints.min.js"></script>
    <script src="/client/lib/lightbox/js/lightbox.min.js"></script>
    <script src="/client/lib/owlcarousel/owl.carousel.min.js"></script>

    <!-- PROJECT JS -->
    <script src="/client/js/main.js"></script>
    <script src="/client/js/effects.js"></script>
    <script src="/client/js/thankyou.js"></script>
    <!-- thêm hiệu ứng riêng -->
  </body>
</html>
