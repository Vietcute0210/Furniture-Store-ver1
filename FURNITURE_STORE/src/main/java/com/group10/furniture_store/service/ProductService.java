package com.group10.furniture_store.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.group10.furniture_store.domain.Product;
import com.group10.furniture_store.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public List<Product> getAllProduct() {
        return this.productRepository.findAll();
    }

    public Product handleSaveProduct(Product product) {
        return this.productRepository.save(product);
    }

    public Product getProductById(long id) {
        Optional<Product> productOptional = this.productRepository.findById(id);
        Product product = productOptional.get() != null ? productOptional.get() : null;
        return product;
    }

    public void handleDeleteProduct(long id) {
        this.productRepository.deleteById(id);
    }
}
