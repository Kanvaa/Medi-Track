package com.meditrack.pharmacy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "medicines")
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Medicine name is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Category is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String category;

    @NotBlank(message = "Manufacturer is required")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String manufacturer;

    @NotBlank(message = "Batch number is required")
    @Size(max = 50)
    @Column(nullable = false, length = 50)
    private String batchNumber;

    @NotNull(message = "Expiry date is required")
    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Min(value = 0, message = "Stock quantity cannot be negative")
    @Column(nullable = false)
    private int stockQuantity;

    @Min(value = 0, message = "Reorder threshold cannot be negative")
    @Column(nullable = false)
    private int reorderThreshold = 20;

    @Column(nullable = false)
    private boolean prescriptionRequired = false;

    public Medicine() {}

    // Helper methods
    public boolean isLowStock() {
        return stockQuantity <= reorderThreshold;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    public boolean isNearExpiry() {
        return !isExpired() && expiryDate.isBefore(LocalDate.now().plusDays(30));
    }

    public long daysUntilExpiry() {
        return ChronoUnit.DAYS.between(LocalDate.now(), expiryDate);
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getManufacturer() { return manufacturer; }
    public void setManufacturer(String manufacturer) { this.manufacturer = manufacturer; }

    public String getBatchNumber() { return batchNumber; }
    public void setBatchNumber(String batchNumber) { this.batchNumber = batchNumber; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public int getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

    public int getReorderThreshold() { return reorderThreshold; }
    public void setReorderThreshold(int reorderThreshold) { this.reorderThreshold = reorderThreshold; }

    public boolean isPrescriptionRequired() { return prescriptionRequired; }
    public void setPrescriptionRequired(boolean prescriptionRequired) { this.prescriptionRequired = prescriptionRequired; }
}
