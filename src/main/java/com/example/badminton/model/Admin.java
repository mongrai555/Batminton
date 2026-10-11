package com.example.badminton.model;

/**
 * ผู้ใช้ระบบบทบาทผู้ดูแลระบบ (Admin)
 * มีสิทธิ์สูงสุดในระบบ: จัดการสมาชิก (รวมถึงลบสมาชิก) และดูรายงานสรุปรายได้
 */
public class Admin extends User {

    public Admin(String id, String name) {
        super(id, name, "ADMIN");
    }

    @Override
    public boolean canRegisterMembers() {
        return true;
    }

    @Override
    public boolean canDeleteMembers() {
        return true; // Admin มีสิทธิ์ลบสมาชิก
    }

    @Override
    public boolean canViewFinancialReports() {
        return true; // Admin มีสิทธิ์ดูสรุปรายได้
    }

    @Override
    public boolean canCreateBookings() {
        return true;
    }
}
