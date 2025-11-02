document.addEventListener("DOMContentLoaded", () => {
  const fadeEls = document.querySelectorAll(".fade-up");
  const observer = new IntersectionObserver(
    (entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) entry.target.classList.add("visible");
      });
    },
    { threshold: 0.2 }
  );
  fadeEls.forEach((el) => observer.observe(el));

  // Add to cart animation
  const btn = document.querySelector(".btnAddToCartDetail");
  if (btn) {
    btn.addEventListener("click", () => {
      $.toast({
        heading: "Đã thêm vào giỏ hàng",
        text: "Sản phẩm của bạn đã được thêm thành công!",
        showHideTransition: "slide",
        icon: "success",
        position: "bottom-right",
      });
    });
  }
});
