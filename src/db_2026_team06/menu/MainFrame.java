package db_2026_team06.menu;

import db_2026_team06.model.Customer;
import db_2026_team06.service.ReservationService;
import db_2026_team06.util.DBConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * 응용프로그램의 전체 레이아웃과 화면 전환 흐름을 통제하는 메인 프레임 클래스입니다.
 * JTabbedPane을 활용하여 메인 화면과 마이페이지를 구분하고,
 * 예약 프로세스 진입 시 CardLayout을 통해 예약 전용 화면으로 전환합니다.
 */
public class MainFrame extends JFrame {

    private static final String SCREEN_MAIN        = "MAIN_TABS";
    private static final String SCREEN_RESERVATION = "RESERVATION";

    private final CardLayout        cardLayout;
    private final JPanel            cardPanel;
    private final JTabbedPane       mainTabs;

    private final HotelExplorePanel explorePanel;
    private final ReservationPanel  reservationPanel;
    private final MyPagePanel       myPagePanel;

    private final ReservationService reservationService;
    private Customer loggedInCustomer;

    /**
     * MainFrame 생성자
     * 로그인 과정에서 생성된 세션(Customer) 객체를 주입받아 애플리케이션을 초기화합니다.
     * @param loggedInCustomer 인증을 완료한 사용자 객체
     */
    public MainFrame(Customer loggedInCustomer) {
        this.loggedInCustomer = loggedInCustomer;

        setTitle("호텔 선택 및 예약 프로그램 - DB2026Team06");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(1050, 680));

        reservationService = new ReservationService();

        cardLayout       = new CardLayout();
        cardPanel        = new JPanel(cardLayout);

        mainTabs         = new JTabbedPane();
        mainTabs.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        mainTabs.setBackground(Color.WHITE);
        UIManager.put("TabbedPane.selected", new Color(220, 236, 250));

        explorePanel     = new HotelExplorePanel();
        reservationPanel = new ReservationPanel();
        myPagePanel      = new MyPagePanel(reservationService);

        explorePanel.setLoggedInCustomer(loggedInCustomer);
        reservationPanel.setLoggedInCustomer(loggedInCustomer);

        mainTabs.addTab("   호텔 탐색   ", explorePanel);
        mainTabs.addTab("   마이페이지   ", myPagePanel);

        // 마이페이지 탭 클릭 시 접근 권한을 확인하고 예약 내역을 동기화합니다.
        mainTabs.addChangeListener(e -> {
            if (mainTabs.getSelectedIndex() == 1) {
                if (this.loggedInCustomer == null) {
                    JOptionPane.showMessageDialog(this, "로그인이 필요합니다.", "안내", JOptionPane.WARNING_MESSAGE);
                    mainTabs.setSelectedIndex(0);
                } else {
                    myPagePanel.setLoggedInCustomer(this.loggedInCustomer);
                }
            }
        });

        cardPanel.add(mainTabs,         SCREEN_MAIN);
        cardPanel.add(reservationPanel, SCREEN_RESERVATION);

        explorePanel.setReservationListener(hotelId -> {
            reservationPanel.setTargetHotel(hotelId);
            showScreen(SCREEN_RESERVATION);
        });

        reservationPanel.setBackListener(() -> showScreen(SCREEN_MAIN));

        reservationPanel.setReservationSuccessListener(() -> {
            myPagePanel.setLoggedInCustomer(this.loggedInCustomer);
            mainTabs.setSelectedIndex(0);
            showScreen(SCREEN_MAIN);
        });

        add(cardPanel, BorderLayout.CENTER);

        // 애플리케이션 종료 시 DB Connection 자원을 반환합니다.
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

    /**
     * CardLayout을 사용하여 지정된 이름의 패널로 화면을 즉시 전환합니다.
     * @param screenName 전환할 화면의 등록 이름
     */
    public void showScreen(String screenName) {
        cardLayout.show(cardPanel, screenName);
    }

    /**
     * 프로그램 단독 실행 및 UI 렌더링 테스트를 위한 메인 진입점입니다.
     */
    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // 기본 Look And Feel 사용
        }

        SwingUtilities.invokeLater(() -> new MainFrame(null).setVisible(true));
    }
}