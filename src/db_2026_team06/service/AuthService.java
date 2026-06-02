package db_2026_team06.service;

import db_2026_team06.dao.CustomerDAO;
import db_2026_team06.model.Customer;

public class AuthService {
    private CustomerDAO customerDAO;
    private Customer loggedInCustomer; // 현재 로그인된 사용자 세션 관리

    public AuthService() {
        this.customerDAO = new CustomerDAO();
    }
    public boolean isEmailDuplicated(String email) {
        return customerDAO.checkEmailExists(email);
    }

    public boolean register(String name, String email, String phone, String password) {
        return customerDAO.insertCustomer(name, email, phone, password);
    }

    public boolean login(String email, String password) {
        Customer customer = customerDAO.getCustomerByEmailAndPassword(email, password);
        if (customer != null) {
            this.loggedInCustomer = customer;
            return true;
        }
        return false;
    }

    public void logout() {
        this.loggedInCustomer = null;
    }

    //기존 비밀번호 검증 후 비밀번호 변경
    public boolean changePassword(String email, String oldPassword, String newPassword) {
        // 1. 기존 이메일과 기존 비밀번호가 일치하는지 먼저 검증
        Customer customer = customerDAO.getCustomerByEmailAndPassword(email, oldPassword);

        // 2. 검증을 통과했다면 새 비밀번호로 업데이트
        if (customer != null) {
            return customerDAO.updatePassword(email, newPassword);
        }

        // 검증 실패 시 false 반환
        return false;
    }

    public Customer getLoggedInCustomer() {
        return loggedInCustomer;
    }
}