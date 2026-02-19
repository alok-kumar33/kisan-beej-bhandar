package com.shop.shopmanagement.controller;



import com.shop.shopmanagement.entity.Product;

import com.shop.shopmanagement.repository.ProductRepository;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;



@Controller

public class ProductController {



    @Autowired

    private ProductRepository productRepository;



// 1. LIST PRODUCTS

    @GetMapping("/products")

    public String listProducts(Model model) {

        model.addAttribute("products", productRepository.findAll());

        return "products";

    }



// 2. SHOW ADD FORM

    @GetMapping("/products/new")

    public String showAddProductForm(Model model) {

        model.addAttribute("product", new Product());

        return "product-add";

    }



// 3. SAVE NEW PRODUCT

    @PostMapping("/products/add")

    public String addProduct(@ModelAttribute Product product) {

        productRepository.save(product);

        return "redirect:/products";

    }



// 4. SHOW EDIT FORM

    @GetMapping("/products/edit/{id}")

    public String showEditForm(@PathVariable Long id, Model model) {

        Product product = productRepository.findById(id)

                .orElseThrow(() -> new RuntimeException("Product not found"));

        model.addAttribute("product", product);

        return "product-edit";

    }



// 5. SAVE EDITS (Compatible with your BigDecimal fields)

    @PostMapping("/products/update")

    public String updateProduct(@ModelAttribute Product product) {

        Product existing = productRepository.findById(product.getId()).get();



        existing.setName(product.getName());

        existing.setCategory(product.getCategory());

        existing.setTotalStock(product.getTotalStock());

        existing.setUnitType(product.getUnitType());



// BigDecimal Fields

        existing.setCostPricePerUnit(product.getCostPricePerUnit());

        existing.setRetailPricePerUnit(product.getRetailPricePerUnit());

        existing.setWholesalePricePerUnit(product.getWholesalePricePerUnit());



// Batch Info Fields

        existing.setSupplierName(product.getSupplierName());

        existing.setSupplierPhone(product.getSupplierPhone());

        existing.setBuyingDate(product.getBuyingDate());

        existing.setExpiryDate(product.getExpiryDate());

        existing.setRemarks(product.getRemarks());



        productRepository.save(existing);



        return "redirect:/products";

    }

}