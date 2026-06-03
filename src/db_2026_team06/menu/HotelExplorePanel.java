package db_2026_team06.menu;

import db_2026_team06.model.Customer;
import db_2026_team06.model.Hotel;
import db_2026_team06.service.HotelService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.List;

/**
 * 호텔 탐색 메인 화면
 *
 * 화면 구성:
 * ┌─────────────────────────────────────────────────────────┐
 * │ [검색바: 지역명 입력] [검색] [전체] [추천]   [ㅇㅇㅇ님] [로그아웃] │
 * ├──────────────────────────┬──────────────────────────────┤
 * │                          │                              │
 * │      MapPanel            │       HotelDetailPanel       │
 * │   (지도 + 마커)          │       (탭: 기본/룸/리뷰)       │
 * │                          │                              │
 * └──────────────────────────┴──────────────────────────────┘
 *
 * 마커 클릭 → HotelDetailPanel 자동 업데이트
 * 예약하기 버튼 → ReservationListener 콜백
 */
public class HotelExplorePanel extends JPanel {

    private final HotelService    hotelService;
    private final MapPanel        mapPanel;
    private final HotelDetailPanel detailPanel;

    // 검색 컴포넌트
    private JTextField tfSearch;

    // 로그인 및 사용자 상태 표시 컴포넌트
    private JLabel lblWelcome;
    private JButton btnAuth;
    private Customer loggedInCustomer;

    // 예약 화면 전환 콜백
    private HotelDetailPanel.ReservationListener reservationListener;

    public HotelExplorePanel() {
        this.hotelService = new HotelService();
        this.mapPanel     = new MapPanel();
        this.detailPanel  = new HotelDetailPanel(hotelService);

        setLayout(new BorderLayout(0, 0));
        initTopBar();
        initMainArea();
        linkPanels();

        // 초기 데이터 로드
        loadHotels(hotelService.getAllHotels());
    }

    // ────────────────────────────────────────────────────────────────────
    // UI 초기화
    // ────────────────────────────────────────────────────────────────────

    private void initTopBar() {
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));
        topBar.setBackground(new Color(245, 248, 255));

        // 1. 왼쪽 검색 및 필터 영역
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        leftPanel.setOpaque(false);

        JLabel lblSearch = new JLabel("지역 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        tfSearch = new JTextField(16);
        tfSearch.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        tfSearch.addActionListener(this::onSearch);

        JButton btnSearch = makeButton("검색",      new Color(50, 120, 220));
        JButton btnAll    = makeButton("전체 보기",  new Color(90, 90, 90));
        JButton btnTop    = makeButton("추천 호텔 ★", new Color(190, 130, 20));

        btnSearch.addActionListener(this::onSearch);
        btnAll   .addActionListener(e -> loadHotels(hotelService.getAllHotels()));
        btnTop   .addActionListener(e -> loadHotels(hotelService.getTopRatedHotels()));

        leftPanel.add(lblSearch);
        leftPanel.add(tfSearch);
        leftPanel.add(btnSearch);
        leftPanel.add(btnAll);
        leftPanel.add(btnTop);

        // 2. 오른쪽 로그인/사용자 정보 영역
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightPanel.setOpaque(false);

        lblWelcome = new JLabel("로그인이 필요합니다.");
        lblWelcome.setFont(new Font("맑은 고딕", Font.BOLD, 12));

        btnAuth = makeButton("로그인", new Color(70, 70, 70));
        btnAuth.addActionListener(e -> onAuthButtonClicked());

        rightPanel.add(lblWelcome);
        rightPanel.add(btnAuth);

        topBar.add(leftPanel, BorderLayout.WEST);
        topBar.add(rightPanel, BorderLayout.EAST);

        add(topBar, BorderLayout.NORTH);
    }

    private void initMainArea() {
        JSplitPane splitPane = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, mapPanel, detailPanel);
        splitPane.setDividerLocation(580);
        splitPane.setResizeWeight(0.6);
        splitPane.setOneTouchExpandable(true);
        add(splitPane, BorderLayout.CENTER);
    }

    /**
     * MapPanel 마커 클릭 → HotelDetailPanel 연결
     */
    private void linkPanels() {
        // 지도 마커 클릭 시 세부 정보 패널 업데이트
        mapPanel.setHotelSelectListener(hotelId ->
                detailPanel.showHotelDetail(hotelId));

        // 예약 버튼 클릭을 외부 리스너로 전달
        detailPanel.setReservationListener(hotelId -> {
            // [수정된 부분] 로그인이 되어 있지 않으면 경고창을 띄우고 진입을 차단합니다.
            if (this.loggedInCustomer == null) {
                JOptionPane.showMessageDialog(this,
                        "예약 기능을 사용하려면 로그인이 필요합니다.\n우측 상단의 [로그인] 버튼을 이용해 주세요.",
                        "권한 없음", JOptionPane.WARNING_MESSAGE);
                return; // 여기서 흐름을 강제로 끊습니다.
            }

            // 정상적으로 로그인된 경우에만 메인 프레임에 전환 신호를 보냅니다.
            if (reservationListener != null) {
                reservationListener.onReservationRequested(hotelId);
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────
    // 이벤트 핸들러
    // ────────────────────────────────────────────────────────────────────

    private void onSearch(ActionEvent e) {
        String keyword = tfSearch.getText().trim();
        List<Hotel> result = hotelService.searchHotelsByLocation(keyword);
        if (result.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "'" + keyword + "' 지역에 해당하는 호텔이 없습니다.",
                    "검색 결과 없음", JOptionPane.INFORMATION_MESSAGE);
        }
        loadHotels(result);
    }

    private void loadHotels(List<Hotel> hotels) {
        mapPanel.setHotels(hotels);
    }

    private void onAuthButtonClicked() {
        if (loggedInCustomer != null) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "정말 로그아웃 하시겠습니까?", "로그아웃", JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                Window window = SwingUtilities.getWindowAncestor(this);
                if (window != null) window.dispose();
                new AuthGUI().setVisible(true);
            }
        } else {
            Window window = SwingUtilities.getWindowAncestor(this);
            if (window != null) window.dispose();
            new AuthGUI().setVisible(true);
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // 외부 연동
    // ────────────────────────────────────────────────────────────────────

    public void setLoggedInCustomer(Customer customer) {
        this.loggedInCustomer = customer;
        if (customer != null) {
            lblWelcome.setText(customer.getName() + "님 환영합니다.");
            btnAuth.setText("로그아웃");
            btnAuth.setBackground(new Color(200, 80, 80));
        } else {
            lblWelcome.setText("로그인이 필요합니다.");
            btnAuth.setText("로그인");
            btnAuth.setBackground(new Color(70, 70, 70));
        }
    }

    public void setReservationListener(HotelDetailPanel.ReservationListener listener) {
        this.reservationListener = listener;
    }

    // ────────────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ────────────────────────────────────────────────────────────────────

    private JButton makeButton(String text, Color bg) {
        JButton btn = new JButton(text);
        btn.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        return btn;
    }
}