package com.shop.shopmanagement.controller;

import com.shop.shopmanagement.entity.Product;
import com.shop.shopmanagement.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.math.BigDecimal;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("productForm", new Product());
        return "products";
    }

    @PostMapping("/products/save")
    public String saveProduct(Product product,
                              // QUANTITY INPUTS
                              @RequestParam("buyingQty") Double buyingQty,
                              @RequestParam("subItemsPerUnit") Double subItems,
                              @RequestParam("baseSize") Double baseSize,

                              // COST PRICE
                              @RequestParam("totalCostPrice") BigDecimal totalCost,

                              // RETAIL PRICE INPUTS (Either/Or)
                              @RequestParam(value = "retailPricePerPackage", required = false) BigDecimal retailPricePerPackage,
                              @RequestParam(value = "retailPricePerUnit", required = false) BigDecimal retailPricePerUnit,

                              // WHOLESALE PRICE INPUTS (Either/Or) -- NEW!
                              @RequestParam(value = "wholesalePricePerPackage", required = false) BigDecimal wholesalePricePerPackage,
                              @RequestParam(value = "wholesalePricePerUnit", required = false) BigDecimal wholesalePricePerUnit
    ) {

        productService.processAndSaveProduct(
                product, buyingQty, subItems, baseSize,
                totalCost,
                retailPricePerPackage, retailPricePerUnit,
                wholesalePricePerPackage, wholesalePricePerUnit // Passing new inputs
        );

        return "redirect:/products";
    }
}