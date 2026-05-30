package db_2026_team06.menu;

import db_2026_team06.model.Attraction;
import db_2026_team06.model.Hotel;
import db_2026_team06.model.Review;
import db_2026_team06.model.Room;
import db_2026_team06.service.HotelService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 호텔 세부 정보를 표시하는 패널
 *
 * 탭 구성:
 *  - 기본 정보 탭: 호텔명, 위치, 연락처, 평균 별점, 주변 관광지, 설명
 *  - 룸 정보 탭:   룸 번호, 유형, 1박 가격, 수용 인원 테이블
 *  - 리뷰 탭:      리뷰 ID, 별점, 날짜, 내용 테이블
 */
public class HotelDetailPanel extends JPanel {

    private final HotelService hotelService;

    // UI 컴포넌트 - 기본 정보 탭
    private JLabel  lblHotelName;
    private JLabel  lblLocation;
    private JLabel  lblContact;
    private JLabel  lblAvgRating;
    private JLabel  lblAttractions;
    private JTextArea taDescription;

    // UI 컴포넌트 - 룸 정보 탭
    private DefaultTableModel roomTableModel;

    // UI 컴포넌트 - 리뷰 탭
    private DefaultTableModel reviewTableModel;

    // 예약 버튼 클릭 리스너 (예약 파트와 연동)
    private ReservationListener reservationListener;

    // 현재 표시 중인 호텔 ID
    private int currentHotelId = -1;

    /**
     * 예약 버튼 클릭 이벤트를 외부로 전달하는 리스너 인터페이스
     */
    public interface ReservationListener {
        void onReservationRequested(int hotelId);
    }

    public HotelDetailPanel(HotelService hotelService) {
        this.hotelService = hotelService;
        setLayout(new BorderLayout());
        setPreferredSize(new Dimension(400, 400));
        initComponents();
        showPlaceholder();
    }

    // ────────────────────────────────────────────────────────────────────
    // UI 초기화
    // ────────────────────────────────────────────────────────────────────

    /**
     * 탭 패널 및 하위 컴포넌트를 초기화합니다.
     */
    private void initComponents() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.PLAIN, 13));

        tabbedPane.addTab("기본 정보", buildInfoTab());
        tabbedPane.addTab("룸 정보",   buildRoomTab());
        tabbedPane.addTab("리뷰",      buildReviewTab());

        add(tabbedPane, BorderLayout.CENTER);

        // 예약 버튼
        JButton btnReserve = new JButton("예약하기");
        btnReserve.setFont(new Font("맑은 고딕", Font.BOLD, 13));
        btnReserve.setBackground(new Color(50, 120, 220));
        btnReserve.setForeground(Color.WHITE);
        btnReserve.setFocusPainted(false);
        btnReserve.addActionListener(e -> {
            if (currentHotelId == -1) {
                JOptionPane.showMessageDialog(this, "먼저 호텔을 선택해주세요.", "안내", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
            if (reservationListener != null) {
                reservationListener.onReservationRequested(currentHotelId);
            }
        });

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        southPanel.add(btnReserve);
        add(southPanel, BorderLayout.SOUTH);
    }

    /**
     * 기본 정보 탭 패널을 생성합니다.
     */
    private JPanel buildInfoTab() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets  = new Insets(5, 5, 5, 5);
        gbc.anchor  = GridBagConstraints.WEST;
        gbc.fill    = GridBagConstraints.HORIZONTAL;

        Font labelFont = new Font("맑은 고딕", Font.BOLD,  12);
        Font valueFont = new Font("맑은 고딕", Font.PLAIN, 12);

        // 호텔명
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0;
        panel.add(makeLabel("호텔명", labelFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        lblHotelName = makeLabel("-", valueFont);
        panel.add(lblHotelName, gbc);

        // 위치
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0;
        panel.add(makeLabel("위치", labelFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        lblLocation = makeLabel("-", valueFont);
        panel.add(lblLocation, gbc);

        // 연락처
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0;
        panel.add(makeLabel("연락처", labelFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        lblContact = makeLabel("-", valueFont);
        panel.add(lblContact, gbc);

        // 평균 별점
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0;
        panel.add(makeLabel("평균 별점", labelFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        lblAvgRating = makeLabel("-", new Font("맑은 고딕", Font.BOLD, 13));
        lblAvgRating.setForeground(new Color(200, 140, 0));
        panel.add(lblAvgRating, gbc);

        // 주변 관광지
        gbc.gridx = 0; gbc.gridy = 4; gbc.weightx = 0;
        panel.add(makeLabel("주변 관광지", labelFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1;
        lblAttractions = makeLabel("-", valueFont);
        panel.add(lblAttractions, gbc);

        // 호텔 설명
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0; gbc.anchor = GridBagConstraints.NORTHWEST;
        panel.add(makeLabel("설명", labelFont), gbc);
        gbc.gridx = 1; gbc.weightx = 1; gbc.weighty = 1;
        gbc.fill = GridBagConstraints.BOTH;
        taDescription = new JTextArea(4, 20);
        taDescription.setFont(valueFont);
        taDescription.setLineWrap(true);
        taDescription.setWrapStyleWord(true);
        taDescription.setEditable(false);
        taDescription.setBackground(panel.getBackground());
        panel.add(new JScrollPane(taDescription,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), gbc);

        return panel;
    }

    /**
     * 룸 정보 탭 패널을 생성합니다.
     */
    private JScrollPane buildRoomTab() {
        String[] columns = {"룸 번호", "유형", "1박 가격(원)", "수용 인원"};
        roomTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(roomTableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));
        return new JScrollPane(table);
    }

    /**
     * 리뷰 탭 패널을 생성합니다.
     */
    private JScrollPane buildReviewTab() {
        String[] columns = {"리뷰 ID", "별점", "날짜", "내용"};
        reviewTableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(reviewTableModel);
        table.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        table.setRowHeight(24);
        table.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));
        // 내용 컬럼 너비 확장
        table.getColumnModel().getColumn(3).setPreferredWidth(200);
        return new JScrollPane(table);
    }

    // ────────────────────────────────────────────────────────────────────
    // 데이터 표시
    // ────────────────────────────────────────────────────────────────────

    /**
     * 선택한 호텔의 전체 세부 정보를 화면에 표시합니다.
     * @param hotelId 표시할 호텔 ID
     */
    public void showHotelDetail(int hotelId) {
        currentHotelId = hotelId;

        Hotel hotel = hotelService.getHotelById(hotelId);
        if (hotel == null) {
            JOptionPane.showMessageDialog(this, "호텔 정보를 불러오지 못했습니다.",
                    "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 기본 정보 탭 업데이트
        double avgRating = hotelService.getAvgRating(hotelId);
        String stars     = hotelService.formatStars((int) Math.round(avgRating));
        lblHotelName .setText(hotel.getHotelName());
        lblLocation  .setText(hotel.getLocation());
        lblContact   .setText(hotel.getContact());
        lblAvgRating .setText(stars + String.format("  (%.1f / 5.0)", avgRating));
        taDescription.setText(hotel.getHDescription() != null ? hotel.getHDescription() : "-");

        List<Attraction> attractions = hotelService.getAttractionsByHotelId(hotelId);
        if (attractions.isEmpty()) {
            lblAttractions.setText("-");
        } else {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < attractions.size(); i++) {
                if (i > 0) sb.append(", ");
                sb.append(attractions.get(i).getAttractionName());
            }
            lblAttractions.setText(sb.toString());
        }

        // 룸 정보 탭 업데이트
        roomTableModel.setRowCount(0);
        List<Room> rooms = hotelService.getRoomsByHotelId(hotelId);
        for (Room room : rooms) {
            roomTableModel.addRow(new Object[]{
                room.getRoomNumber(),
                room.getType(),
                String.format("%,d", room.getPricePerNight()),
                room.getCapacity() + "명"
            });
        }

        // 리뷰 탭 업데이트
        reviewTableModel.setRowCount(0);
        List<Review> reviews = hotelService.getReviewsByHotelId(hotelId);
        for (Review rv : reviews) {
            String stars5 = hotelService.formatStars(rv.getRating());
            reviewTableModel.addRow(new Object[]{
                rv.getReviewId(),
                stars5,
                rv.getReviewDate() != null ? rv.getReviewDate().toString() : "-",
                rv.getReview()
            });
        }
    }

    /**
     * 호텔 미선택 상태의 초기 안내 화면을 표시합니다.
     */
    private void showPlaceholder() {
        lblHotelName .setText("지도에서 호텔을 선택해주세요.");
        lblLocation  .setText("-");
        lblContact   .setText("-");
        lblAvgRating .setText("-");
        lblAttractions.setText("-");
        taDescription.setText("");
        if (roomTableModel   != null) roomTableModel.setRowCount(0);
        if (reviewTableModel != null) reviewTableModel.setRowCount(0);
    }

    // ────────────────────────────────────────────────────────────────────
    // 리스너 등록
    // ────────────────────────────────────────────────────────────────────

    /**
     * 예약 버튼 클릭 리스너를 등록합니다.
     * @param listener ReservationListener 구현체
     */
    public void setReservationListener(ReservationListener listener) {
        this.reservationListener = listener;
    }

    // ────────────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ────────────────────────────────────────────────────────────────────

    private JLabel makeLabel(String text, Font font) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(font);
        return lbl;
    }
}
