<%@page contentType="text/html" pageEncoding="UTF-8" %>
    <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
        <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
            <%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

               OCTYPE html>
            <html lang="en">
            
            <head>
                <meta charset="utf-8" />
                <meta http-equiv="X-UA-Compatible" content="IE=edge" />
                <meta name="viewport" content="width=device-width, initial-scale=1, shrink-to-fit=no" />
                <meta name="description" content="Furniture Store" />
                <meta name="author" content="Furniture Store" />
                <title>Create Products - Furniture Store</title>
                <link href="/css/styles.css" rel="stylesheet" />
                <script src="https://use.fontawesome.com/releases/v6.3.0/js/all.js" crossorigin="anonymous"></script>
                <script src="https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js"></script>
            
                <script>
                    $(document).ready(() => {
                        const avatarFile = $("#avatarFile");
                        avatarFile.change(function (e) {
                            const imgURL = URL.createObjectURL(e.target.files[0]);
                            $("#avatarPreview").attr("src", imgURL);
                            $("#avatarPreview").css({ "display": "block" });
                        });
                    });
                </script>
            
            </head>
            
            <body class="sb-nav-fixed">
                <jsp:include page="../layout/header.jsp" />
                <div id="layoutSidenav">
                    <jsp:include page="../layout/sidebar.jsp" />
                    <div id="layoutSidenav_content">
                        <main>
                            <div class="mt-5">
                                <div class="row">
                                    <div class="col-md-6 col-12 mx-auto">
                                        <h3>Create a product</h3>
                                        <hr/>
                                        <form:form method="post" action="/admin/product/create" modelAttribute="newProduct"
                                            class="row" enctype="multipart/form-data">
                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var="errorName">
                                                    <form:errors path="name" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label class="form-label">Name :</label>
                                                <form:input type="text"
                                                    class="form-control ${not empty errorName ? 'is-invalid' : ''}" path="name" />
                                                ${errorName}
                                            </div>
            
                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var="errorPrice">
                                                    <form:errors path="price" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label class="form-label">Price :</label>
                                                <form:input type="text"
                                                    class="form-control ${not empty errorPrice ? 'is-invalid' : ''}" path="price" />
                                                ${errorPrice}
                                            </div>
            
                                            <div class="col-12">
                                                <c:set var="errordetailDesc">
                                                    <form:errors path="detailDesc" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label class="form-label">Detail description</label>
                                                <form:input type="text"
                                                    class="form-control ${not empty errordetailDesc ? 'is-invalid' : ''}"
                                                    path="detailDesc" />
                                                ${errordetailDesc}
                                            </div>
            
                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var="errorshortDesc">
                                                    <form:errors path="shortDesc" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label class="form-label">Short Description:</label>
                                                <form:input type="text"
                                                    class="form-control ${not empty errorshortDesc ? 'is-invalid' : ''}"
                                                    path="shortDesc" />
                                                ${errorshortDesc}
                                            </div>
            
                                            <div class="mb-3 col-12 col-md-6">
                                                <c:set var="errorQuantity">
                                                    <form:errors path="quantity" cssClass="invalid-feedback" />
                                                </c:set>
                                                <label class="form-label">Quantity :</label>
                                                <form:input type="text"
                                                    class="form-control ${not empty errorQuantity ? 'is-invalid' : ''}"
                                                    path="quantity" />
                                                ${errorQuantity}
                                            </div>
            
                                            <div class="mb-3 col-12 col-md-6">
                                                <label class="form-label">Factory :</label>
                                                <form:select class="form-select" path="factory">
                                                    <!-- <form:option value="" label="-- Select factory --" /> -->
                                                    <form:option value="CTY1">CTY1</form:option>
                                                    <form:option value="CTY2">CTY2</form:option>
                                                    <form:option value="CTY3">CTY3</form:option>                                                
                                                </form:select>
                                            </div>
            
                                            <div class="mb-3 col-12 col-md-6">
                                                <label class="form-label">Target:</label>
                                                <form:select class="form-select" path="target">
                                                    <!-- <form:option value="" label="-- Select target --" /> -->
                                                    <form:option value="LOAI1">LOAI1</form:option>
                                                    <form:option value="LOAI2">LOAI2</form:option>
                                                    <form:option value="LOAI3">LOAI3</form:option>
                                                </form:select>
                                            </div>
            
                                            <div class="mb-3 col-12 col-md-6">
                                                <label for="productFile" class="form-label">Image:</label>
                                                <input class="form-control" type="file" id="productFile" accept=".png, .jpg, .jpeg"
                                                    name="productFile" />
                                            </div>
                                            <div class="col-12 mb-3">
                                                <img style="max-height: 250px; display: none;" alt="product preview"
                                                    id="productPreview" />
                                            </div>
            
                                            <div class="col-12 mb-5">
                                                <button type="submit" class="btn btn-primary">Create</button>
                                            </div>
                                        </form:form>
                                    </div>
                                </div>
                            </div>
                        </main>
                        <jsp:include page="../layout/footer.jsp" />
                    </div>
                </div>
                <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.2.3/dist/js/bootstrap.bundle.min.js"
                    crossorigin="anonymous"></script>
                <script src="js/scripts.js"></script>
            
            </body>
            
            </html>