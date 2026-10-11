package com.example.badminton.model;

/**
 * คลาสแม่แบบ Abstract สำหรับรายการในบิลทั้งหมด (OOP Abstract Class)
 */
public abstract class AbstractBillItem implements Billable {
    protected String itemName;
    protected double unitPrice;
    protected int quantity;

    public AbstractBillItem(String itemName, double unitPrice, int quantity) {
        this.itemName = itemName;
        this.unitPrice = unitPrice;
        this.quantity = Math.max(0, quantity);
    }

    @Override
    public String getItemName() {
        return itemName;
    }

    @Override
    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    @Override
    public double getUnitPrice() {
        return unitPrice;
    }

    @Override
    public double calculateTotal() {
        return unitPrice * quantity;
    }

    @Override
    public abstract String getItemType();

    @Override
    public abstract String getDetails();

    @Override
    public String toString() {
        return String.format("%s - %s (%d x %.2f บาท) = %.2f บาท",
                getItemType(), itemName, quantity, unitPrice, calculateTotal());
    }
}
