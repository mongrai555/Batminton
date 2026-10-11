package com.example.badminton.model;

/**
 * รายการซื้อสินค้า/อุปกรณ์ เช่น ลูกขนไก่, ไม้แบดมินตัน, เทปพันด้ามไม้
 */
public class ProductSaleItem extends AbstractBillItem {

    public ProductSaleItem(String productName, double unitPrice, int quantity) {
        super(productName, unitPrice, quantity);
    }

    @Override
    public String getItemType() {
        return "ซื้อสินค้า";
    }

    @Override
    public String getDetails() {
        return String.format("%s จำนวน %d ชิ้น (@ %.2f บาท)", itemName, quantity, unitPrice);
    }
}
