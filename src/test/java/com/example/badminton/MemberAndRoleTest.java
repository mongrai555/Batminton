package com.example.badminton;

import com.example.badminton.model.Admin;
import com.example.badminton.model.Member;
import com.example.badminton.model.Staff;
import com.example.badminton.service.DataManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MemberAndRoleTest {
    private DataManager dataManager;
    private Staff staff;
    private Admin admin;

    @BeforeEach
    void setUp() {
        dataManager = DataManager.getInstance();
        staff = dataManager.getDefaultStaff();
        admin = dataManager.getDefaultAdmin();
    }

    @Test
    void testRolePermissions() {
        // Staff Permissions
        assertTrue(staff.canRegisterMembers(), "Staff ต้องสามารถสมัครสมาชิกได้");
        assertTrue(staff.canCreateBookings(), "Staff ต้องสามารถเปิดบิล/จองสนามได้");
        assertFalse(staff.canDeleteMembers(), "Staff ต้องไม่สามารถลบสมาชิกได้");
        assertFalse(staff.canViewFinancialReports(), "Staff ต้องไม่มีสิทธิ์ดูรายงานการเงินผู้ดูแลระบบ");

        // Admin Permissions
        assertTrue(admin.canRegisterMembers(), "Admin สามารถสมัครสมาชิกได้");
        assertTrue(admin.canCreateBookings(), "Admin สามารถจองสนามได้");
        assertTrue(admin.canDeleteMembers(), "Admin สามารถลบสมาชิกได้");
        assertTrue(admin.canViewFinancialReports(), "Admin สามารถดูรายงานการเงินได้");
    }

    @Test
    void testRegisterNewMember() {
        String newId = dataManager.generateNextMemberId();
        Member newMember = new Member(newId, "ณเดชน์ คอร์ดแบด", "089-999-8888", 0);
        dataManager.addMember(newMember);

        Member retrieved = dataManager.findMemberById(newId);
        assertNotNull(retrieved);
        assertEquals("ณเดชน์ คอร์ดแบด", retrieved.getName());
        assertEquals("089-999-8888", retrieved.getPhone());
        assertEquals(0, retrieved.getAccumulatedHours());
    }

    @Test
    void testStaffCannotDeleteMember_ThrowsSecurityException() {
        Member testMember = new Member("TEST01", "ทดสอบ ลบ", "099-000-0000", 0);
        dataManager.addMember(testMember);

        // Staff พยายามลบสมาชิก -> ต้องเกิด SecurityException
        assertThrows(SecurityException.class, () -> {
            dataManager.deleteMember("TEST01", staff);
        }, "เมื่อ Staff สั่งลบสมาชิก ต้องปฏิเสธสิทธิ์ด้วย SecurityException");

        // ตรวจสอบว่าสมาชิกยังคงอยู่
        assertNotNull(dataManager.findMemberById("TEST01"));
    }

    @Test
    void testAdminCanDeleteMember() {
        Member testMember = new Member("TEST02", "ทดสอบ ลบจริง", "099-111-1111", 0);
        dataManager.addMember(testMember);

        // Admin สั่งลบสมาชิก -> สำเร็จ
        boolean deleted = dataManager.deleteMember("TEST02", admin);
        assertTrue(deleted);
        assertNull(dataManager.findMemberById("TEST02"));
    }

    @Test
    void testMemberDiscountStatus() {
        Member m19 = dataManager.findMemberById("M001"); // สะสม 19 ชม.
        assertNotNull(m19);
        assertTrue(m19.isEligibleFor20thHourDiscount());
        assertTrue(m19.getDiscountStatusDescription().contains("30%"));
    }
}
