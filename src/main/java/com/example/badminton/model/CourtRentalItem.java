package com.example.badminton.model;

/**
 * รายการค่าเช่าสนามแบดมินตัน
 * รองรับทั้งการจ่ายปกติ, การใช้สิทธิ์แพ็กเกจเหมาจ่าย 15 ชั่วโมง (0 บาท), และส่วนลดชั่วโมงที่ 20
 */
public class CourtRentalItem extends AbstractBillItem {
    private final int courtNumber;
    private final String bookingDate;
    private final String timeSlot;
    private final double discountAmount;
    private final boolean is20thHourDiscountApplied;
    private final int packageHoursUsed;
    private final int paidHours;

    public CourtRentalItem(int courtNumber, String bookingDate, String timeSlot, int totalHours,
                           int packageHoursUsed, double standardHourlyRate, double discountAmount,
                           boolean is20thHourDiscountApplied) {
        super("ค่าเช่าสนาม " + courtNumber, standardHourlyRate, totalHours);
        this.courtNumber = courtNumber;
        this.bookingDate = bookingDate;
        this.timeSlot = timeSlot;
        this.packageHoursUsed = Math.max(0, packageHoursUsed);
        this.paidHours = Math.max(0, totalHours - this.packageHoursUsed);
        this.discountAmount = Math.max(0.0, discountAmount);
        this.is20thHourDiscountApplied = is20thHourDiscountApplied;
    }

    // Constructor เดิมสำหรับ backward compatibility
    public CourtRentalItem(int courtNumber, String bookingDate, String timeSlot, int hours,
                           double standardHourlyRate, double discountAmount, boolean is20thHourDiscountApplied) {
        this(courtNumber, bookingDate, timeSlot, hours, 0, standardHourlyRate, discountAmount, is20thHourDiscountApplied);
    }

    public int getCourtNumber() {
        return courtNumber;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getTimeSlot() {
        return timeSlot;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public boolean is20thHourDiscountApplied() {
        return is20thHourDiscountApplied;
    }

    public int getPackageHoursUsed() {
        return packageHoursUsed;
    }

    public int getPaidHours() {
        return paidHours;
    }

    @Override
    public double calculateTotal() {
        double raw = paidHours * unitPrice;
        return Math.max(0.0, raw - discountAmount);
    }

    @Override
    public String getItemType() {
        return "เช่าสนาม";
    }

    @Override
    public String getDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("สนาม %d | วันที่ %s เวลา %s (รวม %d ชม.)",
                courtNumber, bookingDate, timeSlot, quantity));

        if (packageHoursUsed > 0) {
            sb.append(String.format(" [ตัดจากแพ็กเกจ: %d ชม. (0 บ.) | ชำระเพิ่ม: %d ชม.]", packageHoursUsed, paidHours));
        }
        if (is20thHourDiscountApplied) {
            sb.append(String.format(" [ส่วนลดชั่วโมงที่ 20 ลด 30%%: -%.2f บาท]", discountAmount));
        }
        return sb.toString();
    }
}
