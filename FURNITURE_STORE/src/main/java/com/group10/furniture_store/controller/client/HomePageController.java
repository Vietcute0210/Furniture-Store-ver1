package com.group10.furniture_store.controller.client;

import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.group10.furniture_store.constant.AppConstant;
import com.group10.furniture_store.controller.BaseController;
import com.group10.furniture_store.domain.Order;
import com.group10.furniture_store.domain.Product;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.domain.DTO.RegisterDTO;
import com.group10.furniture_store.messaging.message.EmailRequest;
import com.group10.furniture_store.messaging.producer.EmailProducer;
import com.group10.furniture_store.service.OrderService;
import com.group10.furniture_store.service.ProductService;
import com.group10.furniture_store.service.UserService;
import com.group10.furniture_store.utils.AppUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
// @RequiredArgsConstructor // Nếu đã có @RequiredArgsConstructor thì không cần
// viết constructor và ngược lại
public class HomePageController extends BaseController {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;
    private final EmailProducer emailProducer;

    public HomePageController(ProductService productService, UserService userService, OrderService orderService,
            PasswordEncoder passwordEncoder, EmailProducer emailProducer) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
        this.emailProducer = emailProducer;
    }

    @GetMapping("/")
    public String getHomePage(Model model) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> products = this.productService.getAllProducts(pageable);
        model.addAttribute("products", products.getContent());
        return "client/homepage/show";
    }

    @GetMapping("/register")
    public String getRegisterPage(Model model) {
        model.addAttribute("registerUser", new RegisterDTO());
        return "client/auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute("registerUser") @Valid RegisterDTO registerDTO,
            BindingResult result) {

        List<FieldError> errors = result.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println("Field: " + error.getField() + " - Message: " + error.getDefaultMessage());
        }

        if (result.hasErrors()) {
            return "client/auth/register";
        }

        // Lấy mật khẩu từ registerDTO và mã hóa nó
        String rawPassword = registerDTO.getPassword();
        String hashPassword = this.passwordEncoder.encode(rawPassword);

        // sử dụng mapper chuyển registerDTO thành User
        User user = this.userService.registerDTOToUser(registerDTO);

        user.setPassword(hashPassword);
        user.setRole(this.userService.getRoleByName("USER"));

        // save
        this.userService.handleSaveUser(user);
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String getLoginPage(Model model) {
        return "client/auth/login";
    }

    @GetMapping("/access-denied")
    public String getDenyPage(Model model) {
        return "client/auth/deny";
    }

    @GetMapping("/order-history")
    public String getOrderHistoryPage(Model model, HttpServletRequest request) {
        User currentUser = new User();
        HttpSession session = request.getSession(false);

        long id = (long) session.getAttribute("id");
        currentUser.setId(id);

        List<Order> orders = this.orderService.fetchOrderByUser(currentUser);
        model.addAttribute("orders", orders);
        return "client/cart/order-history";
    }

    @GetMapping("/verify")
    public String getVerifyPage(@ModelAttribute("registerUser") @Valid RegisterDTO registerDTO,
            BindingResult bindingResult, Model model) {
        log.info("Request to /verify");
        String email = registerDTO.getEmail();
        if (bindingResult.hasErrors()) {
            // Xử lý lỗi validation
            String password = registerDTO.getPassword();
            String confirmPassword = registerDTO.getConfirmPassword();
            String regexp = AppConstant.REGEX_EMAIL;
            String name = registerDTO.getFirstName() + " " + registerDTO.getLastName();
            if (name.length() < 3)
                model.addAttribute("errorFullName", "Họ tên phải có tối thiểu 3 ký tự");
            if (this.userService.checkEmailExist(email))
                model.addAttribute("errorEmailExist", "Email đã được sử dụng");
            if (!email.matches(regexp))
                model.addAttribute("errorEmail", "Email không hợp lệ");
            if (password.length() < 6)
                model.addAttribute("errorPassword", "Mật khẩu phải có tối thiểu 6 ký tự");
            else if (!confirmPassword.equals(password))
                model.addAttribute("errorConfirmPassword", "Mật khẩu xác nhận không khớp");
            return "client/auth/register";
        }
        String OTP = AppUtil.getRandomOTP();
        EmailRequest emailRequest = new EmailRequest();
        emailProducer.sendEmailToQueue(emailRequest);
        registerDTO.setOTP(OTP);
        model.addAttribute("registerDTO", registerDTO);
        return "client/auth/verify-mail";

    }

}
