package db_2026_team06.menu;

import db_2026_team06.model.Hotel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 호텔 위치를 좌표 기반으로 시각화하는 지도 패널
 *
 * 실제 지도 API 대신 패널 내 고정 좌표에 마커(●)를 표시합니다.
 * 마커 클릭 시 호텔 세부 정보 화면(HotelDetailPanel)으로 이동합니다.
 *
 * 호텔 좌표는 hotel_id를 시드로 패널 크기에 맞게 분산 배치되며,
 * DB에서 좌표 컬럼을 추가할 경우 setHotelCoordinate()로 교체 가능합니다.
 */
public class MapPanel extends JPanel {

    // 마커 크기 (픽셀)
    private static final int MARKER_RADIUS = 12;
    // 마커 위에 표시되는 호텔명 폰트
    private static final Font LABEL_FONT   = new Font("맑은 고딕", Font.BOLD, 11);
    // 마커 클릭 허용 오차 (픽셀)
    private static final int  HIT_MARGIN   = 5;

    // 현재 표시 중인 호텔 목록
    private List<Hotel> hotels;
    // hotel_id → 패널 내 좌표 (x, y) 매핑
    private final Map<Integer, Point> coordMap = new HashMap<>();
    // 마커 클릭 시 호출될 콜백 (hotel_id 전달)
    private HotelSelectListener selectListener;

    /**
     * 마커 클릭 이벤트를 외부로 전달하기 위한 리스너 인터페이스
     */
    public interface HotelSelectListener {
        void onHotelSelected(int hotelId);
    }

    public MapPanel() {
        setPreferredSize(new Dimension(600, 400));
        setBackground(new Color(220, 235, 245));  // 연한 하늘색 배경 (지도 느낌)
        setBorder(BorderFactory.createLineBorder(new Color(100, 140, 180), 2));

        // 마우스 클릭 이벤트: 마커 히트 판정
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMapClick(e.getX(), e.getY());
            }
        });
    }

    /**
     * 표시할 호텔 목록을 설정하고 좌표를 배정합니다.
     * @param hotels 표시할 Hotel 리스트
     */
    public void setHotels(List<Hotel> hotels) {
        this.hotels = hotels;
        coordMap.clear();
        assignCoordinates();
        repaint();
    }

    /**
     * 마커 클릭 리스너를 등록합니다.
     * @param listener HotelSelectListener 구현체
     */
    public void setHotelSelectListener(HotelSelectListener listener) {
        this.selectListener = listener;
    }

    // ────────────────────────────────────────────────────────────────────
    // 좌표 배정: 패널 크기 기준으로 호텔을 균등 분산
    // ────────────────────────────────────────────────────────────────────

    /**
     * 호텔 목록에 패널 내 좌표를 배정합니다.
     * hotel_id 해시를 기반으로 패널 내 고정 위치를 결정합니다.
     * 실제 지도 좌표(위도/경도)가 DB에 추가되면 이 메서드를 수정하세요.
     */
    private void assignCoordinates() {
        if (hotels == null || hotels.isEmpty()) return;

        int w = Math.max(getWidth(),  600);
        int h = Math.max(getHeight(), 400);
        int padding = 60;

        for (int i = 0; i < hotels.size(); i++) {
            Hotel hotel = hotels.get(i);
            // hotel_id 기반 의사 랜덤 좌표 (같은 호텔은 항상 같은 위치)
            long seed = (long) hotel.getHotelId() * 2654435761L;
            int x = padding + (int)(Math.abs(seed % (w - padding * 2)));
            int y = padding + (int)(Math.abs((seed >> 16) % (h - padding * 2)));
            coordMap.put(hotel.getHotelId(), new Point(x, y));
            hotel.setMapX(x);
            hotel.setMapY(y);
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // 마우스 클릭 처리: 마커 히트 판정
    // ────────────────────────────────────────────────────────────────────

    /**
     * 클릭한 좌표 근처에 호텔 마커가 있으면 리스너를 호출합니다.
     * @param mx 클릭 X 좌표
     * @param my 클릭 Y 좌표
     */
    private void handleMapClick(int mx, int my) {
        if (hotels == null || selectListener == null) return;
        for (Hotel hotel : hotels) {
            Point p = coordMap.get(hotel.getHotelId());
            if (p == null) continue;
            int dist = (int) p.distance(mx, my);
            if (dist <= MARKER_RADIUS + HIT_MARGIN) {
                selectListener.onHotelSelected(hotel.getHotelId());
                return;
            }
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // 렌더링
    // ────────────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        // 안티앨리어싱
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawMapBackground(g2);

        if (hotels == null || hotels.isEmpty()) {
            drawEmptyMessage(g2);
            return;
        }

        // 좌표 재계산 (처음 paint 시 패널 크기가 확정되므로)
        if (coordMap.isEmpty()) assignCoordinates();

        for (Hotel hotel : hotels) {
            Point p = coordMap.get(hotel.getHotelId());
            if (p == null) continue;
            drawMarker(g2, p.x, p.y, hotel.getHotelName());
        }
    }

    /**
     * 지도 배경(격자선)을 그립니다.
     */
    private void drawMapBackground(Graphics2D g2) {
        g2.setColor(new Color(190, 215, 235));
        int step = 50;
        for (int x = 0; x < getWidth(); x += step) {
            g2.drawLine(x, 0, x, getHeight());
        }
        for (int y = 0; y < getHeight(); y += step) {
            g2.drawLine(0, y, getWidth(), y);
        }
    }

    /**
     * 호텔 마커와 이름 라벨을 그립니다.
     * @param g2    Graphics2D
     * @param x     마커 중심 X
     * @param y     마커 중심 Y
     * @param name  표시할 호텔 이름
     */
    private void drawMarker(Graphics2D g2, int x, int y, String name) {
        // 마커 그림자
        g2.setColor(new Color(0, 0, 0, 50));
        g2.fillOval(x - MARKER_RADIUS + 2, y - MARKER_RADIUS + 2,
                    MARKER_RADIUS * 2, MARKER_RADIUS * 2);
        // 마커 본체
        g2.setColor(new Color(220, 50, 50));
        g2.fillOval(x - MARKER_RADIUS, y - MARKER_RADIUS,
                    MARKER_RADIUS * 2, MARKER_RADIUS * 2);
        // 마커 테두리
        g2.setColor(Color.WHITE);
        g2.setStroke(new BasicStroke(2f));
        g2.drawOval(x - MARKER_RADIUS, y - MARKER_RADIUS,
                    MARKER_RADIUS * 2, MARKER_RADIUS * 2);

        // 호텔명 라벨 (마커 위쪽)
        g2.setFont(LABEL_FONT);
        FontMetrics fm = g2.getFontMetrics();
        int labelW = fm.stringWidth(name);

        // 라벨 배경
        g2.setColor(new Color(255, 255, 255, 200));
        g2.fillRoundRect(x - labelW / 2 - 3, y - MARKER_RADIUS - 20,
                         labelW + 6, 16, 4, 4);
        // 라벨 텍스트
        g2.setColor(new Color(30, 30, 30));
        g2.drawString(name, x - labelW / 2, y - MARKER_RADIUS - 7);
    }

    /**
     * 호텔이 없을 때 안내 메시지를 표시합니다.
     */
    private void drawEmptyMessage(Graphics2D g2) {
        g2.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        g2.setColor(new Color(100, 100, 100));
        String msg = "표시할 호텔이 없습니다.";
        FontMetrics fm = g2.getFontMetrics();
        int x = (getWidth()  - fm.stringWidth(msg)) / 2;
        int y = (getHeight() + fm.getAscent())      / 2;
        g2.drawString(msg, x, y);
    }
}
