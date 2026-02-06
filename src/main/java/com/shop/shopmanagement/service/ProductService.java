package com.shop.shopmanagement.service;

import com.shop.shopmanagement.entity.Product;
import com.shop.shopmanagement.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public void processAndSaveProduct(Product incoming,
                                      Double buyingQty, Double subItems, Double baseSize,
                                      BigDecimal totalCost,
                                      BigDecimal retailPricePerPackage,
                                      BigDecimal retailPricePerUnit,
                                      BigDecimal wholesalePricePerPackage,
                                      BigDecimal wholesalePricePerUnit) {

        // --- 1. CALCULATE TOTAL STOCK ---
        Double totalBaseUnits = buyingQty * subItems * baseSize;
        incoming.setTotalStock(totalBaseUnits);

        // --- 2. COST PRICE LOGIC ---
        // Cost per Unit = Total Cost / Total Stock
        BigDecimal costPerUnit = totalCost.divide(BigDecimal.valueOf(totalBaseUnits), 4, RoundingMode.HALF_UP);
        incoming.setCostPricePerUnit(costPerUnit);

        // --- 3. RETAIL PRICE LOGIC ---
        Double unitsPerPackage = subItems * baseSize; // e.g. 50kg in 1 Sack

        if (retailPricePerPackage != null) {
            // User entered Price Per Package (e.g. 300 per Sack)
            // 300 / 50 = 6 Rs/kg
            BigDecimal price = retailPricePerPackage.divide(BigDecimal.valueOf(unitsPerPackage), 4, RoundingMode.HALF_UP);
            incoming.setRetailPricePerUnit(price);
        } else if (retailPricePerUnit != null) {
            // User entered Price Per Unit (e.g. 6 Rs/kg)
            incoming.setRetailPricePerUnit(retailPricePerUnit);
        }

        // --- 4. WHOLESALE PRICE LOGIC (NEW) ---
        if (wholesalePricePerPackage != null) {
            // User entered Wholesale Per Package (e.g. 250 per Sack)
            // 250 / 50 = 5 Rs/kg
            BigDecimal price = wholesalePricePerPackage.divide(BigDecimal.valueOf(unitsPerPackage), 4, RoundingMode.HALF_UP);
            incoming.setWholesalePricePerUnit(price);
        } else if (wholesalePricePerUnit != null) {
            // User entered Wholesale Per Unit (e.g. 5 Rs/kg)
            incoming.setWholesalePricePerUnit(wholesalePricePerUnit);
        } else {
            // If neither is entered, default to Retail Price (No discount)
            incoming.setWholesalePricePerUnit(incoming.getRetailPricePerUnit());
        }

        // --- 5. SAVE OR MERGE ---
        List<Product> existingProducts = productRepository.findByNameAndUnitType(
                incoming.getName(),
                incoming.getUnitType()
        );

        boolean merged = false;
        for (Product p : existingProducts) {
            // We check if Retail Price matches to merge batches
            if (p.getRetailPricePerUnit().compareTo(incoming.getRetailPricePerUnit()) == 0) {
                p.setTotalStock(p.getTotalStock() + totalBaseUnits);

                // Update metadata
                p.setSupplierName(incoming.getSupplierName());
                p.setSupplierPhone(incoming.getSupplierPhone());
                p.setBuyingDate(incoming.getBuyingDate());
                p.setExpiryDate(incoming.getExpiryDate());

                // Update Wholesale price to latest batch value
                if(incoming.getWholesalePricePerUnit() != null) {
                    p.setWholesalePricePerUnit(incoming.getWholesalePricePerUnit());
                }

                productRepository.save(p);
                merged = true;
                break;
            }
        }

        if (!merged) {
            productRepository.save(incoming);
        }
    }
}