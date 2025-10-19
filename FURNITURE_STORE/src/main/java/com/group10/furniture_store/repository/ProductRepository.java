package com.group10.furniture_store.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.group10.furniture_store.domain.Product;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

}
