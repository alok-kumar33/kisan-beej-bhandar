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
        Double unitsPerPackage = subItems * baseSize;

        if (retailPricePerPackage != null) {
            BigDecimal price = retailPricePerPackage.divide(BigDecimal.valueOf(unitsPerPackage), 4, RoundingMode.HALF_UP);
            incoming.setRetailPricePerUnit(price);
        } else if (retailPricePerUnit != null) {
            incoming.setRetailPricePerUnit(retailPricePerUnit);
        }

        // --- 4. WHOLESALE PRICE LOGIC ---
        if (wholesalePricePerPackage != null) {
            BigDecimal price = wholesalePricePerPackage.divide(BigDecimal.valueOf(unitsPerPackage), 4, RoundingMode.HALF_UP);
            incoming.setWholesalePricePerUnit(price);
        } else if (wholesalePricePerUnit != null) {
            incoming.setWholesalePricePerUnit(wholesalePricePerUnit);
        } else {
            // Default to Retail if empty
            incoming.setWholesalePricePerUnit(incoming.getRetailPricePerUnit());
        }

        // --- 5. SAVE OR MERGE LOGIC ---
        // We look for existing products with the SAME Name and SAME Unit Type
        List<Product> existingProducts = productRepository.findByNameAndUnitType(
                incoming.getName(),
                incoming.getUnitType()
        );

        boolean merged = false;

        // Loop through existing batches to see if we can just add stock
        for (Product p : existingProducts) {

            // MERGE CONDITION: If the Retail Price matches, we treat it as the same "Batch"
            // (You can change this logic if you want to merge regardless of price)
            if (p.getRetailPricePerUnit().compareTo(incoming.getRetailPricePerUnit()) == 0) {

                // 1. Update Stock
                p.setTotalStock(p.getTotalStock() + totalBaseUnits);

                // 2. Update Metadata (Overwrite old info with latest batch info)
                p.setSupplierName(incoming.getSupplierName());
                p.setSupplierPhone(incoming.getSupplierPhone());
                p.setBuyingDate(incoming.getBuyingDate());
                p.setExpiryDate(incoming.getExpiryDate());

                // *** NEW: Update Remarks ***
                p.setRemarks(incoming.getRemarks());

                // 3. Update Wholesale Price (Use latest)
                if(incoming.getWholesalePricePerUnit() != null) {
                    p.setWholesalePricePerUnit(incoming.getWholesalePricePerUnit());
                }

                // 4. Update Cost Price (Use Weighted Average or Latest? Here we use Latest)
                p.setCostPricePerUnit(incoming.getCostPricePerUnit());

                productRepository.save(p);
                merged = true;
                break;
            }
        }

        // If no matching batch found, save as a NEW row
        if (!merged) {
            productRepository.save(incoming);
        }
    }
}