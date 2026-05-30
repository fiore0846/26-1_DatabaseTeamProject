package db_2026_team06.menu;

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
 *  ┌─────────────────────────────────────────────────────┐
 *  │  [검색바: 지역명 입력]  [검색]  [전체 보기]  [추천 호텔]  │
 *  ├──────────────────────────┬──────────────────────────┤
 *  │                          │                          │
 *  │      MapPanel            │   HotelDetailPanel       │
 *  │   (지도 + 마커)          │   (탭: 기본/룸/리뷰)     │
 *  │                          │                          │
 *  └──────────────────────────┴──────────────────────────┘
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

    /**
     * 상단 검색 바 영역을 초기화합니다.
     */
    private void initTopBar() {
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        topBar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(180, 180, 180)));
        topBar.setBackground(new Color(245, 248, 255));

        JLabel lblSearch = new JLabel("지역 검색:");
        lblSearch.setFont(new Font("맑은 고딕", Font.BOLD, 13));

        tfSearch = new JTextField(16);
        tfSearch.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        // 엔터키로도 검색 가능
        tfSearch.addActionListener(this::onSearch);

        JButton btnSearch = makeButton("검색",      new Color(50, 120, 220));
        JButton btnAll    = makeButton("전체 보기",  new Color(90, 90, 90));
        JButton btnTop    = makeButton("추천 호텔 ★", new Color(190, 130, 20));

        btnSearch.addActionListener(this::onSearch);
        btnAll   .addActionListener(e -> loadHotels(hotelService.getAllHotels()));
        btnTop   .addActionListener(e -> loadHotels(hotelService.getTopRatedHotels()));

        topBar.add(lblSearch);
        topBar.add(tfSearch);
        topBar.add(btnSearch);
        topBar.add(btnAll);
        topBar.add(btnTop);

        add(topBar, BorderLayout.NORTH);
    }

    /**
     * 지도 + 세부 정보 분할 영역을 초기화합니다.
     */
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
            if (reservationListener != null) {
                reservationListener.onReservationRequested(hotelId);
            }
        });
    }

    // ────────────────────────────────────────────────────────────────────
    // 이벤트 핸들러
    // ────────────────────────────────────────────────────────────────────

    /**
     * 검색 버튼 또는 엔터 입력 시 호텔 목록을 필터링합니다. (동적 쿼리)
     */
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

    /**
     * 지도 패널에 호텔 목록을 표시합니다.
     * @param hotels 표시할 Hotel 리스트
     */
    private void loadHotels(List<Hotel> hotels) {
        mapPanel.setHotels(hotels);
    }

    // ────────────────────────────────────────────────────────────────────
    // 외부 연동
    // ────────────────────────────────────────────────────────────────────

    /**
     * 예약 화면 전환 리스너를 등록합니다.
     * 예약 파트 담당자가 이 메서드로 콜백을 연결하면 됩니다.
     * @param listener ReservationListener 구현체
     */
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
