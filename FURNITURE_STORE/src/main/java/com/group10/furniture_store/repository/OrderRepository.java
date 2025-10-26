package com.group10.furniture_store.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group10.furniture_store.domain.Order;
import com.group10.furniture_store.domain.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    long count();

    List<Order> findByUser(User user);

    Page<Order> findAll(Pageable pageable);

    Order findByPaymentRef(String paymentRef);
}
