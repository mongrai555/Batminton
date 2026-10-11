package com.example.badminton.model;

/**
 * ผู้ใช้ระบบบทบาทพนักงาน / แคชเชียร์ (Staff)
 * มีสิทธิ์สมัครสมาชิก บันทึกการจอง เช่าอุปกรณ์ และคิดเงิน
 * แต่ไม่มีสิทธิ์ลบสมาชิก หรือดูรายงานสรุปการเงินระดับผู้ดูแลระบบ
 */
public class Staff extends User {

    public Staff(String id, String name) {
        super(id, name, "STAFF");
    }

    @Override
    public boolean canRegisterMembers() {
        return true;
    }

    @Override
    public boolean canDeleteMembers() {
        return false; // สิทธิ์จำกัด: Staff ไม่สามารถลบสมาชิกได้
    }

    @Override
    public boolean canViewFinancialReports() {
        return false; // สิทธิ์จำกัด: Staff ไม่สามารถดูรายงานการเงินเชิงลึกได้
    }

    @Override
    public boolean canCreateBookings() {
        return true;
    }

    @Override
    public boolean canViewMemberBookingHistory() {
        return false; // สิทธิ์จำกัด: Staff ไม่สามารถดูประวัติการจองของสมาชิกทั้งหมดได้ (สงวนสำหรับ Admin)
    }
}
