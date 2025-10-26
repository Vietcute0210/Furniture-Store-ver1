package com.group10.furniture_store.controller.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import com.group10.furniture_store.domain.Cart;
import com.group10.furniture_store.domain.CartDetails;
import com.group10.furniture_store.domain.Product;
import com.group10.furniture_store.domain.Product_;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.domain.DTO.ProductCriteriaDTO;
import com.group10.furniture_store.service.ProductService;
import com.group10.furniture_store.service.UserService;
import com.group10.furniture_store.service.sendEmail.SendEmail;

@Controller
public class ItemController {
    private final ProductService productService;
    private final UserService userService;
    private final SendEmail sendEmail;

    public ItemController(ProductService productService, UserService userService, SendEmail sendEmail) {
        this.productService = productService;
        this.userService = userService;
        this.sendEmail = sendEmail;
    }

    @GetMapping("/product/{id}")
    public String getProductPage(Model model, @PathVariable long id) {
        Product product = this.productService.getProductById(id);
        model.addAttribute("product", product);
        return "client/product/detail";
    }

    @GetMapping("/products")
    public String getProductPage(Model model,
            @RequestParam("page") Optional<String> pageOptional) {
        int page = 1;
        try {
            if (pageOptional.isPresent()) {
                page = Integer.parseInt(pageOptional.get());
            }
        } catch (Exception ex) {
        }

        Pageable pageable = PageRequest.of(page - 1, 3);
        Page<Product> products = this.productService.getAllProducts(pageable);

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        return "client/product/show";
    }

    @PostMapping("/add-product-to-cart/{id}")
    public String addProductToCart(Model model, @PathVariable long id, HttpServletRequest request) {
        long productId = id;
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, productId, session, 1);
        return "redirect:/";
    }

    @GetMapping("/cart")
    public String getCartPage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        HttpSession session = request.getSession();
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);
        Cart cart = this.productService.fetchCartByUser(currentUser);
        List<CartDetails> cartDetails = cart == null ? new ArrayList<>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetails cd : cartDetails) {
            totalPrice += cd.getQuantity() * cd.getPrice();
        }
        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);
        model.addAttribute("cart", cart);
        return "client/cart/show";
    }

    @PostMapping("/delete-cart-product/{id}")
    public String deleteCartDetail(@PathVariable long id, HttpServletRequest request) {
        HttpSession session = request.getSession();
        long cartDetailId = id;
        this.productService.handleRemoveCartDetail(cartDetailId, session);
        return "redirect:/cart";
    }

    @GetMapping("/checkout")
    public String getCheckoutPage(Model model, HttpServletRequest request) {
        HttpSession session = request.getSession();
        User currentUser = new User();
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        Cart cart = this.productService.fetchCartByUser(currentUser);
        List<CartDetails> cartDetails = cart == null ? new ArrayList<>() : cart.getCartDetails();

        double totalPrice = 0;
        for (CartDetails cd : cartDetails) {
            totalPrice += cd.getPrice() * cd.getQuantity();
        }

        model.addAttribute("cartDetails", cartDetails);
        model.addAttribute("totalPrice", totalPrice);

        return "client/cart/checkout";
    }

    @PostMapping("/confirm-checkout")
    public String getCheckoutPage(@ModelAttribute("cart") Cart cart) {
        List<CartDetails> cartDetails = cart == null ? new ArrayList<>() : cart.getCartDetails();
        this.productService.handleUpdateCartBeforeCheckout(cartDetails);
        return "redirect:/checkout";
    }

    @PostMapping("/place-order")
    public String handlePlaceOrder(
            HttpServletRequest request,
            @RequestParam("receiverName") String receiverName,
            @RequestParam("receiverAddress") String receiverAddress,
            @RequestParam("receiverPhone") String receiverPhone,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("totalPrice") String totalPrice) {
        User currentUser = new User();
        HttpSession session = request.getSession();
        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        final String uuid = UUID.randomUUID().toString().replace("-", "");
        this.productService.handlePlaceOrder(currentUser, session, receiverName, receiverAddress, receiverPhone,
                paymentMethod, uuid, Double.parseDouble(totalPrice));

        // if (!paymentMethod.equals("COD")) {
        // String ip = this.vnpayService.getIPAddress(request);
        // String vnpayUrl =
        // this.vnpayService.generateVNPayURL(Double.parseDouble(totalPrice), uuid, ip);
        // return "redirect:" + vnpayUrl;
        // }
        return "redirect:/afterOrder";
    }

    @GetMapping("/afterOrder")
    public String getAfterOrderPage(HttpServletRequest request
    // @RequestParam("vnp_ResponseCode") Optional<String> vnpayResponseOptional,
    // @RequestParam("vnp_TxnRef") Optional<String> paymentRef
    ) {
        HttpSession session = request.getSession();
        long id = (long) session.getAttribute("id");

        User user = this.userService.getUserById(id);
        String email = user.getEmail();
        sendEmail.sendEmail(email, "Xác nhận đơn hàng",
                "Furniture Store chân thành cảm ơn bạn đã tin tưởng sử dụng sản phẩm của chúng tôi!");
        // if (vnpayResponseOptional.isPresent() && paymentRef.isPresent()) {
        // // update trạng thái cho order
        // String paymentStatus = vnpayResponseOptional.get().equals("00") ? "Thanh toán
        // thành công"
        // : "Thanh toán thất bại";
        // this.productService.updatePaymentStatus(paymentRef.get(), paymentStatus);
        // }
        return "client/cart/after-order";
    }

    @GetMapping("/thankyou")
    public String getThankyouPage() {
        return "client/cart/thankyou";
    }

    @PostMapping("/add-product-from-view-detail")
    public String handleAddProductFromViewDetail(
            @RequestParam("id") long id,
            @RequestParam("quantity") long quantity,
            HttpServletRequest request) {
        HttpSession session = request.getSession();
        String email = (String) session.getAttribute("email");
        this.productService.handleAddProductToCart(email, id, session, quantity);
        return "redirect:/products/" + id;
    }
}
