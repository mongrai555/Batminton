package com.example.badminton.model;

/**
 * อินเตอร์เฟซสำหรับรายการสินค้าและบริการที่สามารถคิดเงินลงในบิลได้ (OOP Interface)
 */
public interface Billable {
    String getItemName();
    int getQuantity();
    double getUnitPrice();
    double calculateTotal();
    String getItemType();
    String getDetails();
}
