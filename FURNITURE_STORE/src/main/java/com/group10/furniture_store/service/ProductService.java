package com.group10.furniture_store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.group10.furniture_store.domain.Cart;
import com.group10.furniture_store.domain.CartDetails;
import com.group10.furniture_store.domain.Product;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.repository.CartDetailsRepository;
import com.group10.furniture_store.repository.CartRepository;
import com.group10.furniture_store.repository.OrderDetailRepository;
import com.group10.furniture_store.repository.OrderRepository;
import com.group10.furniture_store.repository.ProductRepository;

import jakarta.servlet.http.HttpSession;

@Service
public class ProductService {
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final CartDetailsRepository cartDetailRepository;
    private final UserService userService;
    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;

    public ProductService(ProductRepository productRepository, CartRepository cartRepository,
            CartDetailsRepository cartDetailRepository, UserService userService, OrderRepository orderRepository,
            OrderDetailRepository orderDetailRepository) {
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.cartDetailRepository = cartDetailRepository;
        this.userService = userService;
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    public List<Product> getAllProducts() {
        return this.productRepository.findAll();
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
                CartDetails oldCartDetail = this.cartDetailRepository.findByCartAndProduct(cart, pr);
                //

                // Nếu chưa được thêm , thì phải thêm vào giỏ
                if (oldCartDetail == null) {
                    CartDetails cd = new CartDetails();

                    cd.setCart(cart);
                    cd.setProduct(pr);
                    cd.setPrice(pr.getPrice());
                    cd.setQuantity(quantity);

                    this.cartDetailRepository.save(cd);

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
                    this.cartDetailRepository.save(oldCartDetail);
                }
            }
        }
    }

    public Cart fetchCartByUser(User user) {
        return this.cartRepository.findByUser(user);
    }
}
