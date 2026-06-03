package db_2026_team06.menu;

import db_2026_team06.service.AuthService;
import javax.swing.*;
import java.awt.*;

public class AuthGUI extends JFrame {
    private AuthService authService;
    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTextField loginEmailField;
    private JPasswordField loginPasswordField;

    private JTextField regNameField;
    private JTextField regEmailField;
    private JTextField regPhoneField;
    private JPasswordField regPasswordField;

    // 이메일 중복 검사 통과 여부를 저장
    private boolean isEmailVerified = false;
    private String verifiedEmail = "";

    private JTextField chgEmailField;
    private JPasswordField chgOldPasswordField; // 기존 비밀번호 입력창
    private JPasswordField chgNewPasswordField; // 새 비밀번호 입력창

    public AuthGUI() {
        this.authService = new AuthService();

        setTitle("MIRICOM 호텔 예약 시스템 - 인증");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 550); // 창 크기를 조금 넉넉하게 조정
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createRegisterPanel(), "Register");
        mainPanel.add(createChangePasswordPanel(), "ChangePassword");

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }

    private JPanel createLoginPanel() {
        JPanel panel = new JPanel(new GridLayout(6, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));

        JLabel titleLabel = new JLabel("로그인", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));

        loginEmailField = new JTextField();
        loginPasswordField = new JPasswordField();

        JButton loginButton = new JButton("로그인");
        JButton toRegButton = new JButton("회원가입");
        JButton toChgButton = new JButton("비밀번호 변경");

        panel.add(titleLabel);
        panel.add(new JLabel("이메일 주소:"));
        panel.add(loginEmailField);
        panel.add(new JLabel("비밀번호:"));
        panel.add(loginPasswordField);

        JPanel btnPanel = new JPanel(new GridLayout(1, 3, 5, 5));
        btnPanel.add(loginButton);
        btnPanel.add(toRegButton);
        btnPanel.add(toChgButton);
        panel.add(btnPanel);

        loginButton.addActionListener(e -> handleLogin());
        toRegButton.addActionListener(e -> cardLayout.show(mainPanel, "Register"));
        toChgButton.addActionListener(e -> cardLayout.show(mainPanel, "ChangePassword"));

        return panel;
    }

    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("새 멤버 회원가입", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        regNameField = new JTextField();
        regEmailField = new JTextField();

        // 이메일 입력창 옆에 중복검사 버튼
        JPanel emailInputPanel = new JPanel(new BorderLayout(5, 0));
        JButton checkEmailBtn = new JButton("중복 확인");
        emailInputPanel.add(regEmailField, BorderLayout.CENTER);
        emailInputPanel.add(checkEmailBtn, BorderLayout.EAST);

        regPhoneField = new JTextField();
        regPasswordField = new JPasswordField();

        JButton regButton = new JButton("가입하기");
        JButton backButton = new JButton("취소 (로그인으로)");

        panel.add(titleLabel);
        panel.add(new JLabel("이름:"));
        panel.add(regNameField);
        panel.add(new JLabel("이메일:"));
        panel.add(emailInputPanel);
        panel.add(new JLabel("전화번호:"));
        panel.add(regPhoneField);
        panel.add(new JLabel("비밀번호:"));
        panel.add(regPasswordField);
        panel.add(regButton);
        panel.add(backButton);

        checkEmailBtn.addActionListener(e -> handleEmailDuplicateCheck());

        regButton.addActionListener(e -> handleRegister());
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Login");
            clearRegisterFields();
        });

        return panel;
    }

    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("비밀번호 재설정", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        chgEmailField = new JTextField();
        chgOldPasswordField = new JPasswordField(); // 기존 비밀번호
        chgNewPasswordField = new JPasswordField(); // 새 비밀번호

        JButton chgButton = new JButton("변경 완료");
        JButton backButton = new JButton("취소 (로그인으로)");

        panel.add(titleLabel);
        panel.add(new JLabel("등록된 이메일:"));
        panel.add(chgEmailField);
        panel.add(new JLabel("기존 비밀번호:"));
        panel.add(chgOldPasswordField);
        panel.add(new JLabel("새로운 비밀번호:"));
        panel.add(chgNewPasswordField);
        panel.add(chgButton);
        panel.add(backButton);

        chgButton.addActionListener(e -> handleChangePassword());
        backButton.addActionListener(e -> {
            cardLayout.show(mainPanel, "Login");
            clearChangeFields();
        });

        return panel;
    }

    // 기능 로직

    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (authService.login(email, password)) {
            JOptionPane.showMessageDialog(this, authService.getLoggedInCustomer().getName() + "님, 환영합니다!", "성공", JOptionPane.INFORMATION_MESSAGE);

            // 로그인 성공 시 MainFrame을 생성하고 사용자 정보를 전달하여 화면을 띄웁니다.
            MainFrame mainFrame = new MainFrame(authService.getLoggedInCustomer());
            mainFrame.setVisible(true);

            this.dispose(); // 기존 로그인 창 닫기
        } else {
            JOptionPane.showMessageDialog(this, "이메일 또는 비밀번호를 다시 확인해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    // 중복 확인 로직
    private void handleEmailDuplicateCheck() {
        String email = regEmailField.getText().trim();

        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "이메일을 먼저 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authService.isEmailDuplicated(email)) {
            JOptionPane.showMessageDialog(this, "이미 사용 중인 이메일입니다. 다른 이메일을 입력하세요.", "중복 오류", JOptionPane.ERROR_MESSAGE);
            isEmailVerified = false;
        } else {
            JOptionPane.showMessageDialog(this, "사용 가능한 이메일입니다!", "확인 완료", JOptionPane.INFORMATION_MESSAGE);
            isEmailVerified = true;
            verifiedEmail = email; // 인증받은 이메일을 저장
        }
    }

    private void handleRegister() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String password = new String(regPasswordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "필수 항목을 모두 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 중복 확인을 안 했거나, 중복 확인을 한 후 이메일을 수정해버린 경우 예외 처리
        if (!isEmailVerified || !email.equals(verifiedEmail)) {
            JOptionPane.showMessageDialog(this, "이메일 중복 확인을 완료해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authService.register(name, email, phone, password)) {
            JOptionPane.showMessageDialog(this, "회원가입이 정상적으로 완료되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "Login");
            clearRegisterFields();
        } else {
            JOptionPane.showMessageDialog(this, "회원가입 중 서버 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void handleChangePassword() {
        String email = chgEmailField.getText().trim();
        String oldPassword = new String(chgOldPasswordField.getPassword());
        String newPassword = new String(chgNewPasswordField.getPassword());

        if (email.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 항목을 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // oldPassword 값도 함께 검증 파라미터로 넘겨서 확인
        if (authService.changePassword(email, oldPassword, newPassword)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "Login");
            clearChangeFields();
        } else {
            JOptionPane.showMessageDialog(this, "계정 정보(이메일 또는 기존 비밀번호)가 일치하지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void clearRegisterFields() {
        regNameField.setText("");
        regEmailField.setText("");
        regPhoneField.setText("");
        regPasswordField.setText("");
        isEmailVerified = false;
        verifiedEmail = "";
    }

    private void clearChangeFields() {
        chgEmailField.setText("");
        chgOldPasswordField.setText("");
        chgNewPasswordField.setText("");
    }
}