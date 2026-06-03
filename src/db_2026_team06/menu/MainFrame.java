package db_2026_team06.menu;

import db_2026_team06.model.Customer;
import db_2026_team06.util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 데이터베이스 응용프로그램 메인 프레임
 * 카드 레이아웃으로 [호텔 탐색 화면] ↔ [예약 화면] 전환을 관리합니다.
 */
public class MainFrame extends JFrame {

    private static final String SCREEN_EXPLORE     = "EXPLORE";
    private static final String SCREEN_RESERVATION = "RESERVATION";

    private final CardLayout        cardLayout;
    private final JPanel            cardPanel;
    private final HotelExplorePanel explorePanel;
    private final ReservationPanel  reservationPanel;

    // 로그인 화면에서 인증을 마친 사용자 객체를 주입받아 메인 프레임을 초기화합니다.
    public MainFrame(Customer loggedInCustomer) {
        setTitle("호텔 선택 및 예약 프로그램 - DB2026Team06");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));

        cardLayout       = new CardLayout();
        cardPanel        = new JPanel(cardLayout);
        explorePanel     = new HotelExplorePanel();
        reservationPanel = new ReservationPanel();

        explorePanel.setLoggedInCustomer(loggedInCustomer);

        // 메인 프레임이 전달받은 세션 정보를 예약 패널로 넘겨주어 고객 정보가 자동으로 채워지게 합니다.
        reservationPanel.setLoggedInCustomer(loggedInCustomer);

        // ── 화면 등록 ─────────────────────────────────────────────────
        cardPanel.add(explorePanel,     SCREEN_EXPLORE);
        cardPanel.add(reservationPanel, SCREEN_RESERVATION);

        // ── 화면 전환 연결 ────────────────────────────────────────────
        // 호텔 탐색 → 예약 화면: 예약하기 버튼 클릭 시
        explorePanel.setReservationListener(hotelId -> {
            reservationPanel.setTargetHotel(hotelId);
            showScreen(SCREEN_RESERVATION);
        });

        // 예약 화면 → 호텔 탐색: 뒤로가기 버튼 클릭 시
        reservationPanel.setBackListener(() -> showScreen(SCREEN_EXPLORE));

        add(cardPanel, BorderLayout.CENTER);

        // 창 닫기 시 DB 연결 안전 종료
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
        showScreen(SCREEN_EXPLORE);
    }

    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
    }

    // 단독 실행 및 UI 디자인 테스트용 메인 메서드입니다.
    // 실제 프로그램 구동 시에는 AuthGUI를 거쳐 MainFrame이 호출됩니다.
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) { /* 기본 LAF 사용 */ }

        // 테스트 시에는 로그인 정보가 없으므로 임시로 null을 전달합니다.
        SwingUtilities.invokeLater(() -> new MainFrame(null).setVisible(true));
    }
}