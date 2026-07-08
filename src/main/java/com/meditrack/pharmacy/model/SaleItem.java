package com.meditrack.pharmacy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

@Entity
@Table(name = "sale_items")
public class SaleItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Min(value = 1, message = "Quantity must be at least 1")
    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPriceAtSale;

    @Column(length = 50)
    private String batchNumberAtSale;

    public SaleItem() {}

    public BigDecimal getLineTotal() {
        if (unitPriceAtSale == null) return BigDecimal.ZERO;
        return unitPriceAtSale.multiply(BigDecimal.valueOf(quantity));
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Sale getSale() { return sale; }
    public void setSale(Sale sale) { this.sale = sale; }

    public Medicine getMedicine() { return medicine; }
    public void setMedicine(Medicine medicine) { this.medicine = medicine; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPriceAtSale() { return unitPriceAtSale; }
    public void setUnitPriceAtSale(BigDecimal unitPriceAtSale) { this.unitPriceAtSale = unitPriceAtSale; }

    public String getBatchNumberAtSale() { return batchNumberAtSale; }
    public void setBatchNumberAtSale(String batchNumberAtSale) { this.batchNumberAtSale = batchNumberAtSale; }
}
