// ============ Cập nhật giá tiền khi tăng/giảm số lượng ============ //
$(document).ready(function () {
  // Hàm định dạng tiền VNĐ
  function formatCurrency(number) {
    return number.toLocaleString("vi-VN") + " đ";
  }

  // Cập nhật tổng tiền toàn bộ (chỉ lấy phần tử hiển thị giá - thẻ p)
  function updateTotal() {
    let total = 0;
    $("p[data-cart-detail-id]").each(function () {
      const priceText = $(this).text().replace(/[^\d]/g, "");
      const price = parseInt(priceText || 0, 10);
      total += price;
    });
    $("[data-cart-total-price]").text(formatCurrency(total));
  }

  // Lưu lượng vào localStorage
  const STORAGE_KEY = "furniture_store_cart_quantities";

  function loadSavedQuantities() {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      return raw ? JSON.parse(raw) : {};
    } catch (e) {
      console.warn("Failed to parse saved cart quantities", e);
      return {};
    }
  }

  function saveQuantitiesMap(map) {
    try {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(map));
    } catch (e) {
      console.warn("Failed to save cart quantities", e);
    }
  }

  function saveQuantity(id, qty) {
    if (typeof id === "undefined") return;
    const map = loadSavedQuantities();
    map[id] = qty;
    saveQuantitiesMap(map);
  }

  function clearSavedQuantities() {
    try {
      localStorage.removeItem(STORAGE_KEY);
    } catch (e) {
      /* ignore */
    }
  }

  // Áp dụng các giá trị đã lưu (nếu có) lên DOM khi load trang
  function applySavedQuantities() {
    const map = loadSavedQuantities();
    Object.keys(map).forEach((id) => {
      const qty = parseInt(map[id], 10);
      if (!isFinite(qty)) return;
      const visibleInput = $(`input[data-cart-detail-id='${id}']`);
      if (visibleInput.length) {
        visibleInput.val(qty);
        // Cập nhật thẻ p giá cho dòng
        const pricePerItem =
          parseInt(visibleInput.data("cart-detail-price"), 10) || 0;
        const totalForItem = pricePerItem * qty;
        $(`p[data-cart-detail-id='${id}']`).text(formatCurrency(totalForItem));

        // Cập nhật input ẩn nếu có
        const index = visibleInput.data("cart-detail-index");
        if (typeof index !== "undefined") {
          const hiddenQty = $(`input[name='cartDetails[${index}].quantity']`);
          if (hiddenQty.length) hiddenQty.val(qty);
        }
      }
    });
    updateTotal();
  }

  // Khi bấm nút cộng
  $(".btn-plus")
    .off("click")
    .on("click", function () {
      const input = $(this).closest(".quantity").find("input");
      let value = parseInt(input.val(), 10) || 0;
      value = value + 1;
      input.val(value);

      // Cập nhật input ẩn trong form checkout (tên sẽ là cartDetails[INDEX].quantity)
      const index = input.data("cart-detail-index");
      if (typeof index !== "undefined") {
        const hiddenQty = $(`input[name='cartDetails[${index}].quantity']`);
        if (hiddenQty.length) {
          hiddenQty.val(value);
        }
      }

      const pricePerItem = parseInt(input.data("cart-detail-price"), 10) || 0;
      const cartDetailId = input.data("cart-detail-id");

      // Cập nhật lại thành tiền của sản phẩm (chỉ cập nhật thẻ p chứa giá)
      const totalForItem = pricePerItem * value;
      const itemPrice = $(`p[data-cart-detail-id='${cartDetailId}']`);
      itemPrice.text(formatCurrency(totalForItem)).addClass("highlight");
      setTimeout(() => itemPrice.removeClass("highlight"), 500);

      // Lưu số lượng mới
      saveQuantity(cartDetailId, value);

      updateTotal();
    });

  // Khi bấm nút trừ
  $(".btn-minus")
    .off("click")
    .on("click", function () {
      const input = $(this).closest(".quantity").find("input");
      let value = parseInt(input.val(), 10) || 0;
      if (value > 1) {
        value = value - 1;
        input.val(value);

        // Cập nhật input ẩn trong form checkout
        const index = input.data("cart-detail-index");
        if (typeof index !== "undefined") {
          const hiddenQty = $(`input[name='cartDetails[${index}].quantity']`);
          if (hiddenQty.length) {
            hiddenQty.val(value);
          }
        }

        const pricePerItem = parseInt(input.data("cart-detail-price"), 10) || 0;
        const cartDetailId = input.data("cart-detail-id");
        const totalForItem = pricePerItem * value;

        $(`p[data-cart-detail-id='${cartDetailId}']`).text(
          formatCurrency(totalForItem)
        );

        // Lưu số lượng mới
        saveQuantity(cartDetailId, value);

        updateTotal();
      }
    });

  // Áp dụng các giá trị đã lưu ngay khi trang load xong
  applySavedQuantities();

  // Khi submit checkout form, xóa localStorage (vì server sẽ xử lý theo dữ liệu server side)
  $("form[action='/confirm-checkout']").on("submit", function () {
    clearSavedQuantities();
  });
});
