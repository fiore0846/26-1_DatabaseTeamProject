package db_2026_team06.menu;

import db_2026_team06.model.Attraction;
import db_2026_team06.model.Hotel;
import db_2026_team06.model.Review;
import db_2026_team06.model.Room;
import db_2026_team06.service.HotelService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;
import java.util.function.IntConsumer;

/**
 * 특정 호텔의 상세 정보를 보여주는 패널입니다.
 * 조인(Join) 및 뷰(View) 쿼리를 통해 가져온 기본 정보, 객실, 리뷰, 주변 관광지 데이터를 출력합니다.
 */
public class HotelDetailPanel extends JPanel {
    private final HotelService hotelService = new HotelService();

    private int hotelId;
    private Runnable backListener;
    private IntConsumer reservationListener;

    /** 예약 요청 시 외부(부모 컨테이너)와 통신하기 위한 콜백 인터페이스입니다. */
    public interface ReservationListener {
        void onReservationRequested(int hotelId);
    }
    private ReservationListener customReservationListener;

    private JLabel hotelNameLabel;
    private JLabel locationLabel;
    private JLabel contactLabel;
    private JLabel ratingLabel;

    private JTextArea descriptionArea;
    private JPanel attractionListPanel;
    private JTabbedPane tabbedPane;

    private final Color bgColor = new Color(245, 248, 252);
    private final Color lightBlue = new Color(232, 244, 255);
    private final Color buttonBlue = new Color(210, 229, 245);
    private final Color borderBlue = new Color(170, 200, 230);
    private final Color panelWhite = Color.WHITE;
    private final Color cardBg = new Color(250, 252, 255);
    private final Color cardLine = new Color(220, 230, 240);
    private final Color titleColor = new Color(35, 45, 60);
    private final Color subTextColor = new Color(90, 100, 115);
    private final Color tabSelectedColor = new Color(220, 236, 250);

    /**
     * HotelDetailPanel 생성자
     * 상세 정보 패널의 UI 구조를 초기화합니다.
     */
    public HotelDetailPanel() {
        initComponents();
    }

    /**
     * 출력할 대상 호텔의 ID를 설정하고 데이터를 갱신합니다.
     * @param hotelId 조회할 호텔의 기본키(PK)
     */
    public void setTargetHotel(int hotelId) {
        this.hotelId = hotelId;
        refreshTabs();
        loadHotelInfo();
    }

    /**
     * 탐색 패널 등 외부에서 호출하여 호텔 상세 정보를 렌더링합니다.
     * @param hotelId 출력할 호텔 ID
     */
    public void showHotelDetail(int hotelId) {
        setTargetHotel(hotelId);
    }

    /** 뒤로가기 버튼 클릭 이벤트 리스너를 등록합니다. */
    public void setBackListener(Runnable backListener) {
        this.backListener = backListener;
    }

    /** (하위 호환용) 예약 버튼 클릭 이벤트 리스너를 등록합니다. */
    public void setReservationListener(IntConsumer reservationListener) {
        this.reservationListener = reservationListener;
    }

    /** 예약 버튼 클릭 이벤트 리스너를 등록합니다. */
    public void setReservationListener(ReservationListener listener) {
        this.customReservationListener = listener;
    }

    /** 패널의 기본 레이아웃과 탭 뷰를 구성합니다. */
    private void initComponents() {
        setLayout(new BorderLayout(12, 12));
        setBackground(bgColor);
        setBorder(new EmptyBorder(15, 15, 15, 15));

        add(createTopPanel(), BorderLayout.NORTH);

        tabbedPane = new JTabbedPane();
        tabbedPane.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        tabbedPane.setBackground(panelWhite);
        tabbedPane.setForeground(titleColor);
        UIManager.put("TabbedPane.selected", tabSelectedColor);

        add(tabbedPane, BorderLayout.CENTER);

        refreshTabs();
        loadHotelInfo();
    }

    /** * 상단의 호텔 이름, 평점, 연락처 요약 영역과 제어 버튼 영역을 생성합니다.
     * @return 요약 정보가 포함된 JPanel
     */
    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel(new BorderLayout(15, 10));
        topPanel.setBackground(lightBlue);
        topPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderBlue),
                new EmptyBorder(15, 18, 15, 18)
        ));

        JPanel infoPanel = new JPanel(new GridLayout(4, 1, 5, 5));
        infoPanel.setOpaque(false);

        hotelNameLabel = new JLabel("호텔명: ");
        locationLabel = new JLabel("위치: ");
        contactLabel = new JLabel("연락처: ");
        ratingLabel = new JLabel("평균 별점: ");

        hotelNameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 22));
        hotelNameLabel.setForeground(Color.BLACK);

        locationLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        contactLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        ratingLabel.setFont(new Font("맑은 고딕", Font.PLAIN, 14));

        infoPanel.add(hotelNameLabel);
        infoPanel.add(locationLabel);
        infoPanel.add(contactLabel);
        infoPanel.add(ratingLabel);

        JPanel buttonPanel = new JPanel(new GridLayout(2, 1, 8, 8));
        buttonPanel.setOpaque(false);

        JButton reservationButton = createMenuButton("예약하기");
        reservationButton.addActionListener(e -> {
            if (customReservationListener != null) {
                customReservationListener.onReservationRequested(hotelId);
            } else if (reservationListener != null) {
                reservationListener.accept(hotelId);
            } else {
                JOptionPane.showMessageDialog(SwingUtilities.getWindowAncestor(this), "예약 화면으로 이동합니다.\nhotel_id: " + hotelId);
            }
        });

        JButton backButton = createMenuButton("뒤로가기");
        backButton.addActionListener(e -> {
            if (backListener != null) {
                backListener.run();
            }
        });

        buttonPanel.add(reservationButton);
        buttonPanel.add(backButton);

        topPanel.add(infoPanel, BorderLayout.CENTER);
        topPanel.add(buttonPanel, BorderLayout.EAST);

        return topPanel;
    }

    /** 호텔 아이디 변경 시 하단 탭의 데이터를 갱신합니다. */
    private void refreshTabs() {
        if (tabbedPane == null) {
            return;
        }

        tabbedPane.removeAll();
        tabbedPane.addTab("기본 정보", createBasicInfoPanel());
        tabbedPane.addTab("룸 정보", createRoomPanel());
        tabbedPane.addTab("리뷰", createReviewPanel());
    }

    /** * 호텔 소개 및 조인 쿼리로 획득한 주변 관광지 정보를 표시하는 패널을 생성합니다.
     * @return 기본 정보 JPanel
     */
    private JPanel createBasicInfoPanel() {
        JPanel panel = new JPanel(new GridLayout(1, 2, 12, 0));
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(10, 5, 5, 5));

        JPanel descriptionPanel = createSectionPanel("호텔 소개");
        JPanel attractionPanel = createSectionPanel("주변 관광지");

        descriptionArea = new JTextArea();
        descriptionArea.setEditable(false);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        descriptionArea.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        descriptionArea.setForeground(titleColor);
        descriptionArea.setBackground(panelWhite);
        descriptionArea.setMargin(new Insets(10, 10, 10, 10));
        descriptionArea.setBorder(BorderFactory.createLineBorder(cardLine));

        JScrollPane descriptionScrollPane = new JScrollPane(descriptionArea);
        descriptionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        descriptionScrollPane.getViewport().setBackground(panelWhite);

        descriptionPanel.add(descriptionScrollPane, BorderLayout.CENTER);

        attractionListPanel = new JPanel();
        attractionListPanel.setLayout(new BoxLayout(attractionListPanel, BoxLayout.Y_AXIS));
        attractionListPanel.setBackground(panelWhite);
        attractionListPanel.setBorder(new EmptyBorder(4, 4, 4, 4));

        JScrollPane attractionScrollPane = new JScrollPane(attractionListPanel);
        attractionScrollPane.setBorder(BorderFactory.createEmptyBorder());
        attractionScrollPane.getViewport().setBackground(panelWhite);

        attractionPanel.add(attractionScrollPane, BorderLayout.CENTER);

        panel.add(descriptionPanel);
        panel.add(attractionPanel);

        return panel;
    }

    /** * 해당 호텔의 모든 객실 목록을 카드 레이아웃 형태로 렌더링하는 패널을 반환합니다.
     * @return 룸 정보 JPanel
     */
    private JPanel createRoomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(10, 5, 5, 5));

        JPanel roomListPanel = new JPanel();
        roomListPanel.setLayout(new BoxLayout(roomListPanel, BoxLayout.Y_AXIS));
        roomListPanel.setBackground(panelWhite);
        roomListPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        if (hotelId == 0) {
            roomListPanel.add(createEmptyLabel("호텔을 선택해주세요."));
        } else {
            List<Room> rooms = hotelService.getRoomsByHotelId(hotelId);

            if (rooms == null || rooms.isEmpty()) {
                roomListPanel.add(createEmptyLabel("등록된 객실 정보가 없습니다."));
            } else {
                for (Room room : rooms) {
                    roomListPanel.add(createRoomCard(room));
                    roomListPanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(roomListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderBlue));
        scrollPane.getViewport().setBackground(panelWhite);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** * 해당 호텔의 리뷰 목록을 최신순으로 렌더링하는 패널을 반환합니다.
     * @return 리뷰 정보 JPanel
     */
    private JPanel createReviewPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(bgColor);
        panel.setBorder(new EmptyBorder(10, 5, 5, 5));

        JPanel reviewListPanel = new JPanel();
        reviewListPanel.setLayout(new BoxLayout(reviewListPanel, BoxLayout.Y_AXIS));
        reviewListPanel.setBackground(panelWhite);
        reviewListPanel.setBorder(new EmptyBorder(12, 12, 12, 12));

        if (hotelId == 0) {
            reviewListPanel.add(createEmptyLabel("호텔을 선택해주세요."));
        } else {
            List<Review> reviews = hotelService.getReviewsByHotelId(hotelId);

            if (reviews == null || reviews.isEmpty()) {
                reviewListPanel.add(createEmptyLabel("등록된 리뷰가 없습니다."));
            } else {
                for (Review review : reviews) {
                    reviewListPanel.add(createReviewCard(review));
                    reviewListPanel.add(Box.createVerticalStrut(10));
                }
            }
        }

        JScrollPane scrollPane = new JScrollPane(reviewListPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(borderBlue));
        scrollPane.getViewport().setBackground(panelWhite);

        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    /** UI 구역 생성을 위한 헬퍼 메서드입니다. */
    private JPanel createSectionPanel(String title) {
        JPanel sectionPanel = new JPanel(new BorderLayout(0, 10));
        sectionPanel.setBackground(panelWhite);
        sectionPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(borderBlue),
                new EmptyBorder(12, 12, 12, 12)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 17));
        titleLabel.setForeground(titleColor);
        titleLabel.setBorder(new EmptyBorder(0, 2, 0, 0));

        sectionPanel.add(titleLabel, BorderLayout.NORTH);

        return sectionPanel;
    }

    /** 개별 객실 정보를 나타내는 컴포넌트를 생성합니다. */
    private JPanel createRoomCard(Room room) {
        JPanel card = new JPanel(new BorderLayout(10, 8));
        card.setBackground(cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cardLine),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        JLabel titleLabel = new JLabel("객실 " + room.getRoomNumber() + "호");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        titleLabel.setForeground(titleColor);

        JPanel infoPanel = new JPanel(new GridLayout(2, 2, 10, 6));
        infoPanel.setOpaque(false);

        infoPanel.add(createSmallInfoLabel("룸 유형: " + safeText(room.getType())));
        infoPanel.add(createSmallInfoLabel("1박당 가격: " + String.format("%,d원", room.getPricePerNight())));
        infoPanel.add(createSmallInfoLabel("최대 인원: " + room.getCapacity() + "명"));
        infoPanel.add(createSmallInfoLabel("호텔 ID: " + room.getHotelId()));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(infoPanel, BorderLayout.CENTER);

        return card;
    }

    /** 개별 관광지 정보를 나타내는 컴포넌트를 생성합니다. */
    private JPanel createAttractionCard(Attraction attraction) {
        JPanel card = new JPanel(new BorderLayout(8, 5));
        card.setBackground(cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cardLine),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 105));

        JLabel nameLabel = new JLabel("● " + safeText(attraction.getAttractionName()));
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        nameLabel.setForeground(titleColor);

        JTextArea descArea = new JTextArea();
        descArea.setEditable(false);
        descArea.setLineWrap(true);
        descArea.setWrapStyleWord(true);
        descArea.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        descArea.setForeground(subTextColor);
        descArea.setBackground(cardBg);
        descArea.setBorder(BorderFactory.createEmptyBorder());

        descArea.setText(safeText(attraction.getADescription()));

        card.add(nameLabel, BorderLayout.NORTH);
        card.add(descArea, BorderLayout.CENTER);

        return card;
    }

    /** 개별 리뷰 정보를 나타내는 컴포넌트를 생성합니다. */
    private JPanel createReviewCard(Review review) {
        JPanel card = new JPanel(new BorderLayout(8, 8));
        card.setBackground(cardBg);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(cardLine),
                new EmptyBorder(12, 14, 12, 14)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 125));

        JLabel topLabel = new JLabel("★ " + review.getRating() + "점    |    " + review.getReviewDate());
        topLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        topLabel.setForeground(new Color(200, 130, 20));

        JTextArea reviewText = new JTextArea(safeText(review.getReview()));
        reviewText.setEditable(false);
        reviewText.setLineWrap(true);
        reviewText.setWrapStyleWord(true);
        reviewText.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        reviewText.setForeground(titleColor);
        reviewText.setBackground(cardBg);
        reviewText.setBorder(BorderFactory.createEmptyBorder());

        card.add(topLabel, BorderLayout.NORTH);
        card.add(reviewText, BorderLayout.CENTER);

        return card;
    }

    /** UI 텍스트 출력을 위한 헬퍼 메서드입니다. */
    private JLabel createSmallInfoLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 13));
        label.setForeground(subTextColor);
        return label;
    }

    /** 데이터가 없을 때 표시할 빈 라벨을 생성합니다. */
    private JLabel createEmptyLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        label.setForeground(subTextColor);
        return label;
    }

    /** 공통 디자인이 적용된 메뉴 버튼을 생성합니다. */
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 45));
        button.setBackground(buttonBlue);
        button.setForeground(Color.BLACK);
        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(new Color(120, 150, 180)));
        return button;
    }

    /**
     * 현재 설정된 hotelId를 바탕으로 DB에서 호텔 상세 정보 및 평균 별점을 조회하여 UI에 반영합니다.
     */
    private void loadHotelInfo() {
        if (hotelId == 0) {
            hotelNameLabel.setText("호텔명: ");
            locationLabel.setText("위치: ");
            contactLabel.setText("연락처: ");
            ratingLabel.setText("평균 별점: ");

            if (descriptionArea != null) {
                descriptionArea.setText("");
            }

            if (attractionListPanel != null) {
                attractionListPanel.removeAll();
                attractionListPanel.add(createEmptyLabel("호텔을 선택해주세요."));
                attractionListPanel.revalidate();
                attractionListPanel.repaint();
            }

            return;
        }

        Hotel hotel = hotelService.getHotelDetail(hotelId);

        if (hotel == null) {
            JOptionPane.showMessageDialog(this, "호텔 정보를 찾을 수 없습니다.");
            return;
        }

        double avgRating = hotelService.getAverageRatingByHotelId(hotelId);
        List<Attraction> attractions = hotelService.getNearbyAttractions(hotelId);

        hotelNameLabel.setText("호텔명: " + safeText(hotel.getHotelName()));
        locationLabel.setText("위치: " + safeText(hotel.getLocation()));
        contactLabel.setText("연락처: " + safeText(hotel.getContact()));
        int roundedRating = (int) Math.round(avgRating);
        String stars = hotelService.formatStars(roundedRating);
        ratingLabel.setText(String.format("평균 별점: %s (%.1f)", stars, avgRating));

        descriptionArea.setText(safeText(hotel.getHDescription()));

        attractionListPanel.removeAll();

        if (attractions == null || attractions.isEmpty()) {
            attractionListPanel.add(createEmptyLabel("주변 관광지 정보가 없습니다."));
        } else {
            for (Attraction attraction : attractions) {
                attractionListPanel.add(createAttractionCard(attraction));
                attractionListPanel.add(Box.createVerticalStrut(10));
            }
        }

        attractionListPanel.revalidate();
        attractionListPanel.repaint();
    }

    /** null 데이터 처리를 위한 헬퍼 메서드입니다. */
    private String safeText(String text) {
        return text == null || text.isBlank() ? "-" : text;
    }
}