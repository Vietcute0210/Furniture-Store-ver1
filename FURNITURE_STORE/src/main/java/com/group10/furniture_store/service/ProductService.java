package com.group10.furniture_store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.group10.furniture_store.domain.Cart;
import com.group10.furniture_store.domain.CartDetails;
import com.group10.furniture_store.domain.Order;
import com.group10.furniture_store.domain.OrderDetail;
import com.group10.furniture_store.domain.Product;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.repository.CartDetailsRepository;
import com.group10.furniture_store.repository.CartRepository;
import com.group10.furniture_store.repository.OrderDetailRepository;
import com.group10.furniture_store.repository.OrderRepository;
import com.group10.furniture_store.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailsRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailsRepository cartDetailsRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailsRepository = cartDetailsRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return this.productRepository.findAll(pageable);
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public Product getProductById(long id) {
        Optional<Product> productOptional = this.productRepository.findById(id);
        Product product = productOptional.isPresent() ? productOptional.get() : null;
        return product;
    }

    public void handleDeleteProduct(long id) {
        this.productRepository.deleteById(id);
    }

    public void handleAddProductToCart(String email, long productId, HttpSession session, long quantity) {
        User user = this.userService.getUserByEmail(email);
        if (user != null) {
            // check user đã có Cart chưa ? Nếu chưa => Tạo mới
            Cart cart = this.cartRepository.findByUser(user);
            if (cart == null) {
                // tạo mới cart khi user chưa có cart
                Cart otherCart = new Cart();
                otherCart.setUser(user);
                otherCart.setSum(0);

                cart = this.cartRepository.save(otherCart);
            }

            // save cart detail
            // tìm productById

            Optional<Product> producOptional = this.productRepository.findById(productId);
            if (producOptional.isPresent()) {
                Product pr = producOptional.get();

                // check sản phẩm đã từng được thêm vào giỏ hàng trước đó chưa
                CartDetails oldCartDetail = this.cartDetailsRepository.findByCartAndProduct(cart, pr);
                //

                // Nếu chưa được thêm , thì phải thêm vào giỏ
                if (oldCartDetail == null) {
                    CartDetails cd = new CartDetails();

                    cd.setCart(cart);
                    cd.setProduct(pr);
                    cd.setPrice(pr.getPrice());
                    cd.setQuantity(quantity);

                    this.cartDetailsRepository.save(cd);

                    // update cart(sum)
                    int newSum = cart.getSum() + 1;
                    cart.setSum(newSum);
                    this.cartRepository.save(cart);
                    // update session(sum)
                    session.setAttribute("sum", newSum);
                }
                //

                // Nếu sp đã được thêm vào giỏ hàng trước đó rồi , thì update quantity cho nó
                else {
                    oldCartDetail.setQuantity(oldCartDetail.getQuantity() + quantity);
                    this.cartDetailsRepository.save(oldCartDetail);
                }
            }
        }
    }

    public void handleRemoveCartDetail(long cartDetailId, HttpSession session) {
        Optional<CartDetails> cartDetailsOptional = this.cartDetailsRepository.findById(cartDetailId);
        if (cartDetailsOptional.isPresent()) {
            CartDetails cartDetails = cartDetailsOptional.get();
            Cart currentCart = cartDetails.getCart();

            // delete cartDetail
            this.cartDetailsRepository.deleteById(cartDetailId);

            // update cart
            if (currentCart.getSum() > 1) {
                int newSum = currentCart.getSum() - 1;
                currentCart.setSum(newSum);
                session.setAttribute("sum", newSum);
                this.cartRepository.save(currentCart);
            } else {
                // xóa cả cart nếu không còn sản phẩm
                this.cartRepository.deleteById(currentCart.getId());
                // update session
                session.setAttribute("sum", 0);
            }
        }
    }

    public void handleUpdateCartBeforeCheckout(List<CartDetails> cartDetails) {
        for (CartDetails cd : cartDetails) {
            Optional<CartDetails> cdOptional = this.cartDetailsRepository.findById(cd.getId());
            if (cdOptional.isPresent()) {
                CartDetails currentCartDetails = cdOptional.get();
                currentCartDetails.setQuantity(cd.getQuantity());
                this.cartDetailsRepository.save(currentCartDetails);
            }
        }
    }

    @Transactional
    public void handlePlaceOrder(User user, HttpSession session, String receiverName, String receiverAddress,
            String receiverPhone, String paymentMethod, String uuid, Double totalPrice) {
        // Get cart by user
        Cart cart = this.cartRepository.findByUser(user);
        if (cart != null) {
            List<CartDetails> cartDetails = cart.getCartDetails();
            if (cartDetails != null) {
                // Create order
                Order order = new Order();
                order.setUser(user);
                order.setReceiverName(receiverName);
                order.setReceiverAddress(receiverAddress);
                order.setReceiverPhone(receiverPhone);
                order.setStatus("Chưa được xử lý");
                //

                order.setPaymentMethod(paymentMethod);
                order.setPaymentStatus("Thanh toán thành công");
                order.setPaymentRef(paymentMethod.equals("COD") ? "UNKNOWN" : uuid);
                order.setTotalPrice(totalPrice);
                order = this.orderRepository.save(order);

                // create orderDetail

                for (CartDetails cd : cartDetails) {
                    OrderDetail orderDetail = new OrderDetail();
                    orderDetail.setOrder(order);
                    orderDetail.setProduct(cd.getProduct());
                    orderDetail.setPrice(cd.getPrice());
                    orderDetail.setQuantity(cd.getQuantity());

                    this.orderDetailRepository.save(orderDetail);
                }

                // Delete cart_detail and cart sau đó
                for (CartDetails cd : cartDetails) {
                    // xóa từng cartdetails trong cart trước rồi mới xóa cart
                    this.cartDetailsRepository.deleteById(cd.getId());
                }
                this.cartRepository.deleteById(cart.getId());
                // update session
                session.setAttribute("sum", 0);
            }
        }
    }

    public Cart fetchCartByUser(User user) {
        return this.cartRepository.findByUser(user);
    }

    public void updatePaymentStatus(String paymentRef, String paymentStatus) {
        Order order = this.orderRepository.findByPaymentRef(paymentRef);
        order.setPaymentStatus(paymentStatus);
    }
}
