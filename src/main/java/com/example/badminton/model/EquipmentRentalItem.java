package com.example.badminton.model;

/**
 * รายการเช่าอุปกรณ์ (รองเท้าคู่ละ 50 บาท, ไม้แบดอันละ 50 บาท ต่อครั้ง/วัน)
 */
public class EquipmentRentalItem extends AbstractBillItem {
    public static final double RENTAL_RATE = 50.0;

    public EquipmentRentalItem(String equipmentName, int quantity) {
        super(equipmentName, RENTAL_RATE, quantity);
    }

    @Override
    public String getItemType() {
        return "เช่าอุปกรณ์";
    }

    @Override
    public String getDetails() {
        return String.format("%s จำนวน %d รายการ (@ %.2f บาท)", itemName, quantity, unitPrice);
    }
}
