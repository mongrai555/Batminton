package com.example.badminton.service;

import com.example.badminton.model.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * จัดการข้อมูลจำลอง (In-Memory Repository) สำหรับสมาชิก ผู้ใช้ระบบ สินค้า และประวัติบิล
 */
public class DataManager {
    private static DataManager instance;

    private final List<Member> members = new ArrayList<>();
    private final List<User> systemUsers = new ArrayList<>();
    private final List<ProductSaleItem> catalogProducts = new ArrayList<>();
    private final List<Bill> billHistory = new ArrayList<>();

    private final Staff defaultStaff = new Staff("S01", "สมหญิง ใจบริการ (Staff)");
    private final Admin defaultAdmin = new Admin("A01", "ผู้จัดการ สมศักดิ์ (Admin)");
    private User currentUser;

    private final AtomicInteger memberIdSequence = new AtomicInteger(5);

    private DataManager() {
        seedInitialData();
    }

    public static synchronized DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    private void seedInitialData() {
        // บัญชีผู้ใช้งานระบบ (Staff และ Admin)
        systemUsers.add(defaultStaff);
        systemUsers.add(defaultAdmin);
        currentUser = defaultStaff; // ค่าเริ่มต้นเป็น Staff

        members.add(new Member("M001", "สมชาย สายตบ", "081-111-2222", 19, LocalDate.now().minusMonths(3))); // สะสม 19 ชม. (ชม.ถัดไปได้ลด 30%!)
        Member m2 = new Member("M002", "วีระ แบดบอย", "082-333-4444", 18, LocalDate.now().minusMonths(2));
        m2.addPackageHours(15); // ตัวอย่างสมาชิกที่มีแพ็กเกจ 15 ชม. พร้อมใช้งานทันที
        members.add(m2);
        members.add(new Member("M003", "กานดา หน้าเน็ต", "083-555-6666", 5, LocalDate.now().minusWeeks(2)));  // สมาชิกทั่วไป
        members.add(new Member("M004", "ธวัชชัย แชมเปี้ยน", "084-777-8888", 22, LocalDate.now().minusMonths(6))); // สะสมเกิน 20 ชม.

        // แคตตาล็อกสินค้าสำหรับขายหน้าร้าน
        catalogProducts.add(new ProductSaleItem("ลูกขนไก่ RSL (ลูกละ)", 80.0, 1));
        catalogProducts.add(new ProductSaleItem("ลูกขนไก่ RSL Silver (หลอด 12 ลูก)", 850.0, 1));
        catalogProducts.add(new ProductSaleItem("เทปพันด้ามไม้ Yonex", 50.0, 1));
        catalogProducts.add(new ProductSaleItem("ไม้แบดมินตัน Yonex Astrox", 1250.0, 1));
        catalogProducts.add(new ProductSaleItem("น้ำดื่มเกลือแร่สปอนเซอร์", 25.0, 1));
        catalogProducts.add(new ProductSaleItem("น้ำดื่มขวด 600ml", 15.0, 1));
    }

    // --- Role & User Management ---
    public List<User> getSystemUsers() {
        return Collections.unmodifiableList(systemUsers);
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(User user) {
        if (user != null) {
            this.currentUser = user;
        }
    }

    public Staff getDefaultStaff() {
        return defaultStaff;
    }

    public Admin getDefaultAdmin() {
        return defaultAdmin;
    }

    public User getCurrentCashier() {
        return currentUser;
    }

    // --- Membership Management ---
    public List<Member> getMembers() {
        return Collections.unmodifiableList(members);
    }

    public String generateNextMemberId() {
        return String.format("M%03d", memberIdSequence.getAndIncrement());
    }

    public void addMember(Member member) {
        if (member == null) {
            throw new IllegalArgumentException("ข้อมูลสมาชิกต้องไม่เป็นค่าว่าง");
        }
        // ตรวจสอบรหัสซ้ำ
        boolean exists = members.stream().anyMatch(m -> m.getId().equalsIgnoreCase(member.getId()));
        if (exists) {
            throw new IllegalArgumentException("รหัสสมาชิก " + member.getId() + " มีอยู่ในระบบแล้ว");
        }
        members.add(member);
    }

    public boolean updateMember(String id, String newName, String newPhone) {
        Member m = findMemberById(id);
        if (m != null) {
            if (newName != null && !newName.trim().isEmpty()) {
                m.setName(newName.trim());
            }
            if (newPhone != null && !newPhone.trim().isEmpty()) {
                m.setPhone(newPhone.trim());
            }
            return true;
        }
        return false;
    }

    /**
     * ลบสมาชิก (ตรวจสิทธิ์ตามหลัก OOP: เฉพาะผู้มีสิทธิ์ canDeleteMembers เท่านั้น)
     */
    public boolean deleteMember(String memberId, User operator) {
        if (operator == null || !operator.canDeleteMembers()) {
            throw new SecurityException("สิทธิ์ไม่เพียงพอ: บัญชีผู้ใช้นี้ (" +
                    (operator != null ? operator.getRole() : "UNKNOWN") + ") ไม่มีสิทธิ์ลบสมาชิก ต้องใช้สิทธิ์ ADMIN เท่านั้น");
        }
        return members.removeIf(m -> m.getId().equalsIgnoreCase(memberId));
    }

    public Member findMemberById(String id) {
        if (id == null) return null;
        return members.stream()
                .filter(m -> m.getId().equalsIgnoreCase(id.trim()))
                .findFirst()
                .orElse(null);
    }

    public List<Member> searchMembers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getMembers();
        }
        String lower = keyword.trim().toLowerCase();
        return members.stream()
                .filter(m -> m.getId().toLowerCase().contains(lower) ||
                             m.getName().toLowerCase().contains(lower) ||
                             m.getPhone().toLowerCase().contains(lower))
                .collect(Collectors.toList());
    }

    // --- Merchandise & History ---
    public List<ProductSaleItem> getCatalogProducts() {
        return Collections.unmodifiableList(catalogProducts);
    }

    public List<Bill> getBillHistory() {
        return Collections.unmodifiableList(billHistory);
    }

    public void saveBill(Bill bill) {
        if (bill != null) {
            billHistory.add(bill);
        }
    }

    public double calculateTotalRevenue() {
        return billHistory.stream().mapToDouble(Bill::calculateGrandTotal).sum();
    }

    public double calculateRevenueByDate(LocalDate date) {
        return billHistory.stream()
                .filter(b -> b.getTimestamp().toLocalDate().equals(date))
                .mapToDouble(Bill::calculateGrandTotal)
                .sum();
    }

    public double calculateRevenueByMonth(int year, int month) {
        return billHistory.stream()
                .filter(b -> b.getTimestamp().getYear() == year && b.getTimestamp().getMonthValue() == month)
                .mapToDouble(Bill::calculateGrandTotal)
                .sum();
    }
}
