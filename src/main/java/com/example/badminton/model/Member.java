package com.example.badminton.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * คลาสสำหรับสมาชิก (Member) ผู้มีสิทธิ์จองสนามแบดมินตัน
 */
public class Member extends User {
    private String phone;
    private int accumulatedHours;
    private int remainingPackageHours;
    private final LocalDate registeredDate;

    public Member(String id, String name, String phone, int accumulatedHours) {
        this(id, name, phone, accumulatedHours, LocalDate.now());
    }

    public Member(String id, String name, String phone, int accumulatedHours, LocalDate registeredDate) {
        super(id, name, "MEMBER");
        this.phone = phone;
        this.accumulatedHours = Math.max(0, accumulatedHours);
        this.remainingPackageHours = 0;
        this.registeredDate = (registeredDate != null) ? registeredDate : LocalDate.now();
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public int getAccumulatedHours() {
        return accumulatedHours;
    }

    public void addAccumulatedHours(int hours) {
        this.accumulatedHours += Math.max(0, hours);
    }

    public int getRemainingPackageHours() {
        return remainingPackageHours;
    }

    public void addPackageHours(int hours) {
        this.remainingPackageHours += Math.max(0, hours);
    }

    public void deductPackageHours(int hours) {
        this.remainingPackageHours = Math.max(0, this.remainingPackageHours - hours);
    }

    public LocalDate getRegisteredDate() {
        return registeredDate;
    }

    public String getRegisteredDateFormatted() {
        return registeredDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }

    public boolean isEligibleFor20thHourDiscount() {
        return accumulatedHours == 19;
    }

    public String getDiscountStatusDescription() {
        if (accumulatedHours == 19) {
            return "🔥 ได้รับสิทธิ์ลด 30% ทันทีในชม.ถัดไป!";
        } else if (accumulatedHours < 19) {
            return String.format("ขาดอีก %d ชม. จะได้ลด 30%%", 19 - accumulatedHours);
        } else {
            return "สะสมเกิน 20 ชม. แล้ว";
        }
    }

    // สิทธิ์ของสมาชิก (ลูกค้าทั่วไป ไม่มีสิทธิ์ทำงานระบบภายใน)
    @Override
    public boolean canRegisterMembers() {
        return false;
    }

    @Override
    public boolean canDeleteMembers() {
        return false;
    }

    @Override
    public boolean canViewFinancialReports() {
        return false;
    }

    @Override
    public boolean canCreateBookings() {
        return false;
    }

    @Override
    public boolean canViewMemberBookingHistory() {
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s - %s (%s)", id, name, phone);
    }
}
