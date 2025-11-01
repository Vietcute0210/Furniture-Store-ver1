package com.group10.furniture_store.controller.client;

import java.util.List;

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
import org.springframework.web.bind.annotation.RequestParam;

import com.group10.furniture_store.constant.AppConstant;
import com.group10.furniture_store.controller.BaseController;
import com.group10.furniture_store.domain.Order;
import com.group10.furniture_store.domain.PasswordResetToken;
import com.group10.furniture_store.domain.Product;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.domain.DTO.ForgotPasswordDTO;
import com.group10.furniture_store.domain.DTO.RegisterDTO;
import com.group10.furniture_store.domain.DTO.ResetPasswordDTO;
import com.group10.furniture_store.messaging.message.EmailRequest;
import com.group10.furniture_store.messaging.producer.EmailProducer;
import com.group10.furniture_store.service.OrderService;
import com.group10.furniture_store.service.ProductService;
import com.group10.furniture_store.service.TokenService;
import com.group10.furniture_store.service.UserService;
import com.group10.furniture_store.service.sendEmail.SendEmailService;
import com.group10.furniture_store.utils.AppUtil;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class HomePageController extends BaseController {
    private final ProductService productService;
    private final UserService userService;
    private final OrderService orderService;
    private final PasswordEncoder passwordEncoder;
    private final EmailProducer emailProducer;
    private final SendEmailService sendEmailService;
    private final TokenService tokenService;

    public HomePageController(ProductService productService, UserService userService, OrderService orderService,
            PasswordEncoder passwordEncoder, EmailProducer emailProducer, SendEmailService sendEmailService,
            TokenService tokenService) {
        this.productService = productService;
        this.userService = userService;
        this.orderService = orderService;
        this.passwordEncoder = passwordEncoder;
        this.emailProducer = emailProducer;
        this.sendEmailService = sendEmailService;
        this.tokenService = tokenService;
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
            BindingResult result,
            Model model) {

        List<FieldError> errors = result.getFieldErrors();
        for (FieldError error : errors) {
            System.out.println("Field: " + error.getField() + " - Message: " + error.getDefaultMessage());
        }

        if (result.hasErrors()) {
            for (FieldError error : errors) {
                model.addAttribute(
                        "error" + error.getField().substring(0, 1).toUpperCase() + error.getField().substring(1),
                        error.getDefaultMessage());
            }
            model.addAttribute("registerUser", registerDTO);
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
        return "redirect:/login?registerSuccess=true";
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
            String password = registerDTO.getPassword();
            String confirmPassword = registerDTO.getConfirmPassword();
            String regexp = AppConstant.REGEX_EMAIL;
            if (this.userService.checkEmailExist(email))
                model.addAttribute("errorEmailExist", "Email already exists");
            if (!email.matches(regexp))
                model.addAttribute("errorEmail", "Invalid email format");
            if (password.length() < 6)
                model.addAttribute("errorPassword", "Password must have at least 6 characters");
            else if (!confirmPassword.equals(password))
                model.addAttribute("errorConfirmPassword", "Password confirmation does not match");
            return "client/auth/register";
        }
        String OTP = AppUtil.getRandomOTP();
        EmailRequest emailRequest = new EmailRequest();
        emailProducer.sendEmailToQueue(emailRequest);
        registerDTO.setOTP(OTP);
        model.addAttribute("registerDTO", registerDTO);
        return "client/auth/verify-mail";
    }

    @GetMapping("/forgot-password")
    public String getForgotPasswordPage(Model model) {
        model.addAttribute("forgotPasswordDTO", new ForgotPasswordDTO());
        return "client/homepage/forgotPassword";
    }

    @PostMapping("/forgot-password")
    public String handleForgotPassword(@ModelAttribute("forgotPasswordDTO") @Valid ForgotPasswordDTO forgotPasswordDTO,
            BindingResult result, Model model, HttpServletRequest request) {

        if (result.hasErrors()) {
            return "client/homepage/forgotPassword";
        }

        String email = forgotPasswordDTO.getEmail();

        // Check if email exists
        if (!this.userService.checkEmailExist(email)) {
            model.addAttribute("errorMessage", "Email does not exist in the system.");
            return "client/homepage/forgotPassword";
        }

        try {
            User user = this.userService.getUserByEmail(email);

            // Create token
            String token = this.tokenService.createPasswordResetToken(user);

            // Create reset password link
            String baseUrl = request.getScheme() + "://" + request.getServerName();
            if (request.getServerPort() != 80 && request.getServerPort() != 443) {
                baseUrl += ":" + request.getServerPort();
            }
            baseUrl += request.getContextPath();
            String resetLink = baseUrl + "/reset-password?token=" + token;

            // Send email
            this.sendEmailService.sendPasswordResetEmail(email, user.getFullName(), resetLink);

            model.addAttribute("successMessage", "Password reset email has been sent! Please check your inbox.");
            return "client/homepage/forgotPassword";

        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "An error occurred while sending email. Please try again later.");
            return "client/homepage/forgotPassword";
        }
    }

    @GetMapping("/reset-password")
    public String getResetPasswordPage(@RequestParam(required = false) String token, Model model) {
        if (token == null || token.isEmpty()) {
            model.addAttribute("errorMessage", "Invalid token.");
            return "client/homepage/resetPasswordError";
        }

        // Validate token
        PasswordResetToken resetToken = this.tokenService.validatePasswordResetToken(token);

        if (resetToken == null) {
            model.addAttribute("errorMessage", "The password reset link is invalid or has expired.");
            return "client/homepage/resetPasswordError";
        }

        // Tạo DTO và set token vào đó
        ResetPasswordDTO dto = new ResetPasswordDTO();
        dto.setToken(token);

        model.addAttribute("token", token);
        model.addAttribute("resetPasswordDTO", dto);
        return "client/homepage/resetPassword";
    }

    @PostMapping("/reset-password")
    public String handleResetPasswordWithToken(
            @ModelAttribute("resetPasswordDTO") @Valid ResetPasswordDTO resetPasswordDTO,
            BindingResult result, Model model) {

        // Get token from DTO
        String token = resetPasswordDTO.getToken();

        // Debug log
        System.out.println("=== POST /reset-password ===");
        System.out.println("Token from DTO: " + token);

        // If token is null, show error
        if (token == null || token.isEmpty()) {
            System.out.println("Token is null or empty!");
            model.addAttribute("errorMessage", "Invalid token.");
            return "client/homepage/resetPasswordError";
        }

        model.addAttribute("token", token);

        // Validate token first
        PasswordResetToken resetToken = this.tokenService.validatePasswordResetToken(token);
        System.out.println("Reset token validation result: " + (resetToken != null ? "Valid" : "Invalid"));

        if (resetToken == null) {
            model.addAttribute("errorMessage", "The password reset link is invalid or has expired.");
            return "client/homepage/resetPasswordError";
        }

        // Check validation errors
        if (result.hasErrors()) {
            List<FieldError> errors = result.getFieldErrors();
            for (FieldError error : errors) {
                String fieldName = error.getField();
                String errorMessage = error.getDefaultMessage();
                model.addAttribute("error" + fieldName.substring(0, 1).toUpperCase() + fieldName.substring(1),
                        errorMessage);
            }
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }

        String newPassword = resetPasswordDTO.getNewPassword();
        String confirmPassword = resetPasswordDTO.getConfirmPassword();

        if (newPassword.length() < 6) {
            model.addAttribute("errorMessage", "Password must be at least 6 characters long.");
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }

        if (!newPassword.matches(".*[a-z].*")) {
            model.addAttribute("errorMessage", "Password must contain at least one lowercase letter.");
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            model.addAttribute("errorMessage", "Password must contain at least one uppercase letter.");
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }

        if (!newPassword.matches(".*[0-9].*")) {
            model.addAttribute("errorMessage", "Password must contain at least one number.");
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }

        if (!newPassword.equals(confirmPassword)) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }

        String hashPassword = this.passwordEncoder.encode(newPassword);
        boolean success = this.tokenService.changePasswordWithToken(token, hashPassword);

        if (success) {
            return "redirect:/login?resetSuccess=true";
        } else {
            model.addAttribute("errorMessage", "An error occurred. Please try again.");
            model.addAttribute("resetPasswordDTO", resetPasswordDTO);
            return "client/homepage/resetPassword";
        }
    }
}
