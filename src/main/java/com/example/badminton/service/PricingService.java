package com.example.badminton.service;

/**
 * Service สำหรับคำนวณราคาและส่วนลดตามกฎธุรกิจ
 */
public class PricingService {
    public static final double COURT_HOURLY_RATE = 180.0;
    public static final double SHOE_RENTAL_RATE = 50.0;
    public static final double RACKET_RENTAL_RATE = 50.0;
    public static final double PACKAGE_15_HOURS_PRICE = 2600.0;
    public static final double TWENTIETH_HOUR_DISCOUNT_PERCENT = 0.30; // ลด 30%

    /**
     * คำนวณส่วนลดสำหรับชั่วโมงที่ 20
     * กฎ: เมื่อสมาชิกสะสมครบ 19 ชม. แล้วจองถึงชั่วโมงที่ 20 ชั่วโมงที่ 20 จะได้รับส่วนลด 30% (ลด 54 บาท)
     *
     * @param prevAccumulatedHours ชั่วโมงที่สะสมมาก่อนหน้านี้
     * @param bookingHours จำนวนชั่วโมงที่ต้องการจองในครั้งนี้
     * @return จำนวนเงินส่วนลด (บาท)
     */
    public static double calculateTwentiethHourDiscount(int prevAccumulatedHours, int bookingHours) {
        if (bookingHours <= 0) {
            return 0.0;
        }

        int startHour = prevAccumulatedHours + 1;
        int endHour = prevAccumulatedHours + bookingHours;

        // ตรวจสอบว่าช่วงชั่วโมงที่จองครอบคลุมชั่วโมงที่ 20 หรือไม่
        if (startHour <= 20 && endHour >= 20) {
            return COURT_HOURLY_RATE * TWENTIETH_HOUR_DISCOUNT_PERCENT; // 180 * 0.30 = 54.00 บาท
        }
        return 0.0;
    }

    /**
     * คำนวณค่าเช่าสนามรวมหลังหักส่วนลด
     */
    public static double calculateNetCourtFee(int prevAccumulatedHours, int bookingHours) {
        double rawCourtFee = bookingHours * COURT_HOURLY_RATE;
        double discount = calculateTwentiethHourDiscount(prevAccumulatedHours, bookingHours);
        return Math.max(0.0, rawCourtFee - discount);
    }
}
