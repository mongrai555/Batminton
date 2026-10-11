package com.example.badminton;

import com.example.badminton.service.PricingService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class PricingServiceTest {

    @Test
    void test20thHourDiscountWhenExactlyAt19Hours() {
        // สะสมแล้ว 19 ชม. จองเพิ่ม 1 ชม. (คือชม.ที่ 20)
        double discount = PricingService.calculateTwentiethHourDiscount(19, 1);
        assertEquals(54.0, discount, 0.001, "ชั่วโมงที่ 20 ต้องลด 30% จาก 180 = 54 บาท");

        double netFee = PricingService.calculateNetCourtFee(19, 1);
        assertEquals(126.0, netFee, 0.001, "ค่าเช่าต้องเหลือ 180 - 54 = 126 บาท");
    }

    @Test
    void test20thHourDiscountWhenSpanningAcross20thHour() {
        // สะสมแล้ว 18 ชม. จองเพิ่ม 2 ชม. (คือชม.ที่ 19 และ 20)
        double discount = PricingService.calculateTwentiethHourDiscount(18, 2);
        assertEquals(54.0, discount, 0.001);

        double netFee = PricingService.calculateNetCourtFee(18, 2);
        assertEquals(306.0, netFee, 0.001, "ค่าเช่าต้องเป็น 180 + 126 = 306 บาท");
    }

    @Test
    void testNoDiscountBefore20Hours() {
        // สะสมแล้ว 10 ชม. จองเพิ่ม 2 ชม. (ชม.ที่ 11-12)
        double discount = PricingService.calculateTwentiethHourDiscount(10, 2);
        assertEquals(0.0, discount, 0.001);

        double netFee = PricingService.calculateNetCourtFee(10, 2);
        assertEquals(360.0, netFee, 0.001);
    }

    @Test
    void testNoDiscountAfter20Hours() {
        // สะสมแล้ว 20 ชม. จองเพิ่ม 2 ชม. (ชม.ที่ 21-22)
        double discount = PricingService.calculateTwentiethHourDiscount(20, 2);
        assertEquals(0.0, discount, 0.001);

        double netFee = PricingService.calculateNetCourtFee(20, 2);
        assertEquals(360.0, netFee, 0.001);
    }
}
