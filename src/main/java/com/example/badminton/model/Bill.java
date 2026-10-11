package com.example.badminton.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * บิล / ใบเสร็จรับเงิน รวมรายการเช่าสนาม อุปกรณ์เช่า สินค้าที่ซื้อ และหลักฐานสลิป
 */
public class Bill {
    private final String billId;
    private final LocalDateTime timestamp;
    private final Member member;
    private final User operator;
    private final List<Billable> items;
    private String slipImagePath;

    public Bill(String billId, Member member, User operator) {
        this.billId = billId;
        this.timestamp = LocalDateTime.now();
        this.member = member;
        this.operator = operator;
        this.items = new ArrayList<>();
    }

    public void addItem(Billable item) {
        if (item != null) {
            items.add(item);
        }
    }

    public void clearItems() {
        items.clear();
    }

    public String getBillId() {
        return billId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public Member getMember() {
        return member;
    }

    public User getOperator() {
        return operator;
    }

    public User getCashier() {
        return operator;
    }

    public List<Billable> getItems() {
        return Collections.unmodifiableList(items);
    }

    public String getSlipImagePath() {
        return slipImagePath;
    }

    public void setSlipImagePath(String slipImagePath) {
        this.slipImagePath = slipImagePath;
    }

    public boolean hasSlip() {
        return slipImagePath != null && !slipImagePath.trim().isEmpty();
    }

    public double calculateCourtTotal() {
        return items.stream()
                .filter(item -> item instanceof CourtRentalItem)
                .mapToDouble(Billable::calculateTotal)
                .sum();
    }

    public double calculateEquipmentRentalTotal() {
        return items.stream()
                .filter(item -> item instanceof EquipmentRentalItem)
                .mapToDouble(Billable::calculateTotal)
                .sum();
    }

    public double calculateProductSaleTotal() {
        return items.stream()
                .filter(item -> item instanceof ProductSaleItem)
                .mapToDouble(Billable::calculateTotal)
                .sum();
    }

    public double calculateTotalDiscount() {
        return items.stream()
                .filter(item -> item instanceof CourtRentalItem)
                .mapToDouble(item -> ((CourtRentalItem) item).getDiscountAmount())
                .sum();
    }

    public double calculateGrandTotal() {
        return items.stream().mapToDouble(Billable::calculateTotal).sum();
    }

    public String getFormattedDate() {
        return timestamp.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    public String generateReceiptText() {
        StringBuilder sb = new StringBuilder();
        sb.append("====================================================\n");
        sb.append("           BADMINTON COURT & SHOP RECEIPT          \n");
        sb.append("====================================================\n");
        sb.append(String.format("เลขที่บิล: %s\n", billId));
        sb.append(String.format("วันที่-เวลา: %s\n", getFormattedDate()));
        sb.append(String.format("ผู้ทำรายการ: %s\n", operator != null ? operator.getName() : "System"));
        if (member != null) {
            sb.append(String.format("สมาชิก: %s (โทร: %s)\n", member.getName(), member.getPhone()));
            sb.append(String.format("ประวัติชั่วโมงสะสม: %d ชั่วโมง\n", member.getAccumulatedHours()));
        } else {
            sb.append("ลูกค้า: บุคคลทั่วไป (ซื้อสินค้าหน้าร้าน)\n");
        }
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("%-26s %5s %8s %10s\n", "รายการ", "จน.", "ราคา", "รวม (บาท)"));
        sb.append("----------------------------------------------------\n");

        for (Billable item : items) {
            sb.append(String.format("%-26s %5d %8.2f %10.2f\n",
                    truncate(item.getItemName(), 26),
                    item.getQuantity(),
                    item.getUnitPrice(),
                    item.calculateTotal()));
            if (!item.getDetails().isEmpty()) {
                sb.append(String.format("  * %s\n", item.getDetails()));
            }
        }

        sb.append("----------------------------------------------------\n");
        double discount = calculateTotalDiscount();
        if (discount > 0) {
            sb.append(String.format("%-41s -%10.2f\n", "ส่วนลดพิเศษ (ชั่วโมงที่ 20 ลด 30%):", discount));
        }
        sb.append(String.format("%-41s %11.2f บาท\n", "ยอดรวมสุทธิ (Grand Total):", calculateGrandTotal()));
        sb.append("----------------------------------------------------\n");
        sb.append(String.format("สถานะการชำระเงิน: %s\n", hasSlip() ? "แนบสลิปเรียบร้อย [PAID]" : "ยังไม่ได้แนบสลิป"));
        if (hasSlip()) {
            sb.append(String.format("ไฟล์สลิป: %s\n", slipImagePath));
        }
        sb.append("====================================================\n");
        sb.append("                 ขอบคุณที่ใช้บริการค่ะ!                 \n");
        sb.append("====================================================\n");
        return sb.toString();
    }

    public boolean hasCourtBooking() {
        return items.stream().anyMatch(item -> item instanceof CourtRentalItem);
    }

    public List<CourtRentalItem> getCourtRentalItems() {
        List<CourtRentalItem> list = new ArrayList<>();
        for (Billable item : items) {
            if (item instanceof CourtRentalItem) {
                list.add((CourtRentalItem) item);
            }
        }
        return list;
    }

    public String getCourtBookingSummary() {
        StringBuilder sb = new StringBuilder();
        for (Billable item : items) {
            if (item instanceof CourtRentalItem) {
                CourtRentalItem cri = (CourtRentalItem) item;
                if (sb.length() > 0) sb.append(", ");
                sb.append(String.format("สนาม %d (%s เวลา %s)",
                        cri.getCourtNumber(), cri.getBookingDate(), cri.getTimeSlot()));
            }
        }
        return sb.length() > 0 ? sb.toString() : "-";
    }

    public int getTotalCourtHours() {
        return items.stream()
                .filter(item -> item instanceof CourtRentalItem)
                .mapToInt(Billable::getQuantity)
                .sum();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
