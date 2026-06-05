package db_2026_team06.menu;

import db_2026_team06.service.AuthService;
import javax.swing.*;
import java.awt.*;

/**
 * 사용자 인증을 처리하는 GUI 프레임입니다.
 * 로그인, 회원가입, 비밀번호 변경 화면을 카드 레이아웃으로 관리하며,
 * 인증 완료 시 MainFrame으로 제어권을 넘깁니다.
 */
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

    private boolean isEmailVerified = false;
    private String verifiedEmail = "";

    private JTextField chgEmailField;
    private JPasswordField chgOldPasswordField;
    private JPasswordField chgNewPasswordField;

    /**
     * AuthGUI 생성자
     * 인증 프레임의 기본 설정 및 레이아웃을 초기화합니다.
     */
    public AuthGUI() {
        this.authService = new AuthService();

        setTitle("호텔 예약 시스템 - 인증");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(400, 550);
        setLocationRelativeTo(null);

        cardLayout = new CardLayout();
        mainPanel = new JPanel(cardLayout);

        mainPanel.add(createLoginPanel(), "Login");
        mainPanel.add(createRegisterPanel(), "Register");
        mainPanel.add(createChangePasswordPanel(), "ChangePassword");

        add(mainPanel);
        cardLayout.show(mainPanel, "Login");
    }

    /**
     * 로그인 화면 패널을 구성합니다.
     * @return 초기화된 로그인 JPanel 객체
     */
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
        JButton toExploreButton = new JButton("호텔 탐색 화면으로");

        panel.add(titleLabel);
        panel.add(new JLabel("이메일 주소:"));
        panel.add(loginEmailField);
        panel.add(new JLabel("비밀번호:"));
        panel.add(loginPasswordField);

        JPanel btnPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        btnPanel.add(loginButton);
        btnPanel.add(toRegButton);
        btnPanel.add(toChgButton);
        btnPanel.add(toExploreButton);
        panel.add(btnPanel);

        loginButton.addActionListener(e -> handleLogin());
        toRegButton.addActionListener(e -> cardLayout.show(mainPanel, "Register"));
        toChgButton.addActionListener(e -> cardLayout.show(mainPanel, "ChangePassword"));

        toExploreButton.addActionListener(e -> {
            MainFrame mainFrame = new MainFrame(null);
            mainFrame.setVisible(true);
            this.dispose();
        });

        return panel;
    }

    /**
     * 회원가입 화면 패널을 구성합니다.
     * @return 초기화된 회원가입 JPanel 객체
     */
    private JPanel createRegisterPanel() {
        JPanel panel = new JPanel(new GridLayout(11, 1, 5, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("새 멤버 회원가입", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        regNameField = new JTextField();
        regEmailField = new JTextField();

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

    /**
     * 비밀번호 변경 화면 패널을 구성합니다.
     * @return 초기화된 비밀번호 변경 JPanel 객체
     */
    private JPanel createChangePasswordPanel() {
        JPanel panel = new JPanel(new GridLayout(9, 1, 10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));

        JLabel titleLabel = new JLabel("비밀번호 재설정", JLabel.CENTER);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 20));

        chgEmailField = new JTextField();
        chgOldPasswordField = new JPasswordField();
        chgNewPasswordField = new JPasswordField();

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

    /**
     * 로그인 요청을 처리하고, 성공 시 세션을 포함하여 MainFrame을 실행합니다.
     */
    private void handleLogin() {
        String email = loginEmailField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (authService.login(email, password)) {
            JOptionPane.showMessageDialog(this, authService.getLoggedInCustomer().getName() + "님, 환영합니다!", "성공", JOptionPane.INFORMATION_MESSAGE);

            MainFrame mainFrame = new MainFrame(authService.getLoggedInCustomer());
            mainFrame.setVisible(true);

            this.dispose();
        } else {
            JOptionPane.showMessageDialog(this, "이메일 또는 비밀번호를 다시 확인해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * 회원가입 전 이메일 중복 검사를 수행합니다.
     */
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
            verifiedEmail = email;
        }
    }

    /**
     * 입력된 정보를 바탕으로 데이터베이스에 신규 고객을 등록합니다.
     */
    private void handleRegister() {
        String name = regNameField.getText().trim();
        String email = regEmailField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String password = new String(regPasswordField.getPassword());

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "필수 항목을 모두 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

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

    /**
     * 사용자 인증 후 데이터베이스의 비밀번호를 업데이트합니다.
     */
    private void handleChangePassword() {
        String email = chgEmailField.getText().trim();
        String oldPassword = new String(chgOldPasswordField.getPassword());
        String newPassword = new String(chgNewPasswordField.getPassword());

        if (email.isEmpty() || oldPassword.isEmpty() || newPassword.isEmpty()) {
            JOptionPane.showMessageDialog(this, "모든 항목을 입력해 주세요.", "알림", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (authService.changePassword(email, oldPassword, newPassword)) {
            JOptionPane.showMessageDialog(this, "비밀번호가 성공적으로 변경되었습니다.", "성공", JOptionPane.INFORMATION_MESSAGE);
            cardLayout.show(mainPanel, "Login");
            clearChangeFields();
        } else {
            JOptionPane.showMessageDialog(this, "계정 정보(이메일 또는 기존 비밀번호)가 일치하지 않습니다.", "인증 실패", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** 회원가입 입력 폼을 초기화합니다. */
    private void clearRegisterFields() {
        regNameField.setText("");
        regEmailField.setText("");
        regPhoneField.setText("");
        regPasswordField.setText("");
        isEmailVerified = false;
        verifiedEmail = "";
    }

    /** 비밀번호 변경 입력 폼을 초기화합니다. */
    private void clearChangeFields() {
        chgEmailField.setText("");
        chgOldPasswordField.setText("");
        chgNewPasswordField.setText("");
    }
}