package com.group10.furniture_store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.group10.furniture_store.domain.Order;
import com.group10.furniture_store.domain.User;
import com.group10.furniture_store.repository.OrderRepository;

@Service
public class OrderService {
    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public List<Order> fetchAllOrders() {
        return this.orderRepository.findAll();
    }

    public Optional<Order> getOrderById(long id) {
        return this.orderRepository.findById(id);
    }

    public void updateOrder(Order updateOrder) {
        Optional<Order> orderOptional = this.orderRepository.findById(updateOrder.getId());
        if (orderOptional.isPresent()) {
            Order currentOrder = orderOptional.get();
            currentOrder.setStatus(updateOrder.getStatus());
            this.orderRepository.save(currentOrder);
        }
    }

    public List<Order> fetchOrderByUser(User user) {
        return this.orderRepository.findByUser(user);
    }

    public void deleteOrderById(long id) {
        this.orderRepository.deleteById(id);
    }
}
