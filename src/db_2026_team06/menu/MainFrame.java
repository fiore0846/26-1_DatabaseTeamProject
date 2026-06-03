package db_2026_team06.menu;

import db_2026_team06.model.Customer;
import db_2026_team06.service.ReservationService;
import db_2026_team06.util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 데이터베이스 응용프로그램 메인 프레임
 * JTabbedPane(탭)을 활용하여 [호텔 탐색] ↔ [마이페이지]를 이동하고,
 * 예약 시에는 카드 레이아웃으로 [예약 화면]을 띄웁니다.
 */
public class MainFrame extends JFrame {

    // 화면 구분을 위한 상수
    private static final String SCREEN_MAIN        = "MAIN_TABS";
    private static final String SCREEN_RESERVATION = "RESERVATION";

    private final CardLayout        cardLayout;
    private final JPanel            cardPanel;
    private final JTabbedPane       mainTabs; // [추가] 메인 화면용 탭

    private final HotelExplorePanel explorePanel;
    private final ReservationPanel  reservationPanel;
    private final MyPagePanel       myPagePanel;

    private final ReservationService reservationService;
    private Customer loggedInCustomer;

    public MainFrame(Customer loggedInCustomer) {
        this.loggedInCustomer = loggedInCustomer;

        setTitle("호텔 선택 및 예약 프로그램 - DB2026Team06");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));

        reservationService = new ReservationService();

        // ── 패널 초기화 ───────────────────────────────────────────────
        cardLayout       = new CardLayout();
        cardPanel        = new JPanel(cardLayout);

        // 팀원의 디자인을 차용한 메인 탭 생성
        mainTabs         = new JTabbedPane();
        mainTabs.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        mainTabs.setBackground(Color.WHITE);
        UIManager.put("TabbedPane.selected", new Color(220, 236, 250)); // 선택된 탭 색상 (연한 파란색)

        explorePanel     = new HotelExplorePanel();
        reservationPanel = new ReservationPanel();
        myPagePanel      = new MyPagePanel(reservationService);

        // 로그인 정보 주입
        explorePanel.setLoggedInCustomer(loggedInCustomer);
        reservationPanel.setLoggedInCustomer(loggedInCustomer);

        // ── 탭에 화면 등록 ────────────────────────────────────────────
        mainTabs.addTab("   호텔 탐색   ", explorePanel);
        mainTabs.addTab("   마이페이지   ", myPagePanel);

        // 마이페이지 탭을 클릭했을 때의 동작 설정
        mainTabs.addChangeListener(e -> {
            if (mainTabs.getSelectedIndex() == 1) { // 1번 탭(마이페이지)을 눌렀을 때
                if (this.loggedInCustomer == null) {
                    JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    mainTabs.setSelectedIndex(0); // 로그인이 안 되어있으면 다시 탐색 탭으로 튕겨냄
                } else {
                    // 로그인이 되어있다면 최신 예약 정보를 불러옴
                    myPagePanel.setLoggedInCustomer(this.loggedInCustomer);
                }
            }
        });

        // ── 카드 레이아웃에 등록 ──────────────────────────────────────
        // 구조: 카드 1장 = (탐색+마이페이지 탭) / 카드 2장 = (예약화면)
        cardPanel.add(mainTabs,         SCREEN_MAIN);
        cardPanel.add(reservationPanel, SCREEN_RESERVATION);

        // ── 화면 전환 로직 연결 ───────────────────────────────────────
        // 1. 호텔 탐색 → 예약 화면 진입
        explorePanel.setReservationListener(hotelId -> {
            reservationPanel.setTargetHotel(hotelId);
            showScreen(SCREEN_RESERVATION);
        });

        // 2. 예약 화면에서 뒤로가기 클릭 시 → 다시 탭 화면으로 복귀
        reservationPanel.setBackListener(() -> showScreen(SCREEN_MAIN));

        // 3. 예약 완료 시 → 마이페이지 탭으로 강제 이동!
        reservationPanel.setReservationSuccessListener(() -> {
            myPagePanel.setLoggedInCustomer(this.loggedInCustomer);
            mainTabs.setSelectedIndex(1); // 마이페이지 탭으로 강제 전환
            showScreen(SCREEN_MAIN);      // 탭 화면 보여주기
        });

        add(cardPanel, BorderLayout.CENTER);

        // ── 창 닫기 처리 ──────────────────────────────────────────────
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnection.closeConnection();
                dispose();
                System.exit(0);
            }
        });

        pack();
        setLocationRelativeTo(null);
        showScreen(SCREEN_MAIN);
    }

    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* 기본 LAF 사용 */ }

        SwingUtilities.invokeLater(() -> new MainFrame(null).setVisible(true));
    }
}