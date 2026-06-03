package db_2026_team06.model;

public class Customer {
    private int customerId;
    private String name;
    private String email;
    private String phone;
    private String password; // 새로 추가된 비밀번호 필드

    // 생성자
    public Customer() {}

    public Customer(int customerId, String name, String email, String phone, String password) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.password = password;
    }

    // Getters and Setters
    public int getCustomerId() { return customerId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getPassword() { return password; }

    public void setCustomerId(int customerId) {this.customerId = customerId;}
    public void setEmail(String email) {this.email = email;}
    public void setName(String name) {this.name = name;}
    public void setPassword(String password) {this.password = password;}
    public void setPhone(String phone) {this.phone = phone;}
}