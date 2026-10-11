package com.example.badminton.model;

/**
 * คลาสแม่แบบ Abstract สำหรับผู้ใช้งานระบบและบทบาทหน้าที่ (OOP Abstract Class)
 */
public abstract class User {
    protected final String id;
    protected String name;
    protected final String role;

    public User(String id, String name, String role) {
        this.id = id;
        this.name = name;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRole() {
        return role;
    }

    // --- Abstract Permissions (สิทธิ์การทำงานตาม Role) ---
    public abstract boolean canRegisterMembers();
    public abstract boolean canDeleteMembers();
    public abstract boolean canViewFinancialReports();
    public abstract boolean canCreateBookings();

    @Override
    public String toString() {
        return String.format("[%s] %s (%s)", role, name, id);
    }
}
