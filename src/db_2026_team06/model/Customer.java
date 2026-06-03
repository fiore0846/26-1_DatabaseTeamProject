package db_2026_team06.model;

/**
 * Customer 테이블에 대응하는 DTO 클래스
 * 고객 정보를 담습니다.
 */
public class Customer {
    private int    customerId;
    private String name;
    private String email;
    private String phone;

    public Customer() {}

    public Customer(int customerId, String name, String email, String phone) {
        this.customerId = customerId;
        this.name       = name;
        this.email      = email;
        this.phone      = phone;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getCustomerId()           { return customerId; }
    public void setCustomerId(int v)     { this.customerId = v; }

    public String getName()              { return name; }
    public void setName(String v)        { this.name = v; }

    public String getEmail()             { return email; }
    public void setEmail(String v)       { this.email = v; }

    public String getPhone()             { return phone; }
    public void setPhone(String v)       { this.phone = v; }
}
