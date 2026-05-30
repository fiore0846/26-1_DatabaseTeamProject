package db_2026_team06.menu;

import db_2026_team06.util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 데이터베이스 응용프로그램 메인 프레임
 *
 * 카드 레이아웃으로 화면 전환을 관리합니다.
 *
 * 현재 구현된 화면:
 *  - EXPLORE : 호텔 탐색 화면 (HotelExplorePanel)
 *
 * 다른 파트 담당자 연결 가이드:
 *  1) 예약 화면(ReservationPanel)을 만든 뒤 아래 주석을 따라 추가하세요.
 *  2) 리뷰 등록 화면도 동일한 방식으로 연결할 수 있습니다.
 */
public class MainFrame extends JFrame {

    // 카드 레이아웃 화면 이름 상수
    private static final String SCREEN_EXPLORE     = "EXPLORE";
    private static final String SCREEN_RESERVATION = "RESERVATION";  // 예약 파트 담당자 구현

    private final CardLayout   cardLayout;
    private final JPanel       cardPanel;
    private final HotelExplorePanel explorePanel;

    public MainFrame() {
        setTitle("호텔 선택 및 예약 프로그램 - DB2026Team06");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 650));

        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);

        // ── 호텔 탐색 화면 등록 ──────────────────────────────────────
        explorePanel = new HotelExplorePanel();
        cardPanel.add(explorePanel, SCREEN_EXPLORE);

        // ── [다른 파트 연결] 예약 화면 등록 예시 ──────────────────────
        // ReservationPanel reservationPanel = new ReservationPanel();
        // cardPanel.add(reservationPanel, SCREEN_RESERVATION);
        //
        // 호텔 탐색 → 예약 화면 전환:
        // explorePanel.setReservationListener(hotelId -> {
        //     reservationPanel.setTargetHotel(hotelId);  // 선택한 호텔 ID 전달
        //     showScreen(SCREEN_RESERVATION);
        // });
        // ──────────────────────────────────────────────────────────────

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
        setLocationRelativeTo(null); // 화면 중앙 배치
        showScreen(SCREEN_EXPLORE);
    }

    /**
     * 지정한 이름의 화면으로 전환합니다.
     * @param screenName 화면 이름 상수 (SCREEN_EXPLORE 등)
     */
    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
    }

    /**
     * 애플리케이션 진입점
     * EDT(Event Dispatch Thread)에서 메인 프레임을 시작합니다.
     */
    public static void main(String[] args) {
        // 시스템 외관(Look and Feel) 적용
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 기본 LAF 사용
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
