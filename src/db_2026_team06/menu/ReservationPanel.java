package db_2026_team06.menu;

import db_2026_team06.model.Customer;
import db_2026_team06.model.Room;
import db_2026_team06.service.HotelService;
import db_2026_team06.service.ReservationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * 예약 화면 패널
 *
 * 화면 흐름 (요구사항 명시 예약 기능 흐름):
 * 호텔 선택(완료) → 날짜/인원 입력 → 예약 가능 룸 조회
 * → 룸 선택 → 고객 정보 입력 → 예약 생성 → 결과 확인
 *
 * 구현된 기능:
 * - Insert : 예약 생성 (트랜잭션 포함)
 * - Select : 예약 가능 룸 조회, 예약 결과 조회 (뷰 사용)
 */
public class ReservationPanel extends JPanel {

    private final ReservationService reservationService;
    private final HotelService       hotelService;

    // 현재 시스템에 로그인되어 인증을 마친 사용자 세션 객체를 보관합니다.
    private Customer loggedInCustomer;

    // 현재 화면에 띄워지는 룸 목록을 기억합니다.
    private List<Room> currentAvailableRooms;

    // 현재 선택된 호텔 ID (HotelExplorePanel에서 전달받음)
    private int targetHotelId = -1;

    // ── 날짜/인원 입력 컴포넌트 ──────────────────────────────────────
    private JTextField tfCheckIn;
    private JTextField tfCheckOut;
    private JSpinner   spGuests;

    // ── 룸 목록 테이블 ───────────────────────────────────────────────
    private DefaultTableModel roomTableModel;
    private JTable             roomTable;

    // ── 고객 정보 입력 컴포넌트 ─────────────────────────────────────
    private JTextField tfName;
    private JTextField tfEmail;
    private JTextField tfPhone;

    // ── 결과 표시 영역 ───────────────────────────────────────────────
    private JTextArea taResult;

    // 화면 전환 콜백
    private Runnable backListener;
    private Runnable reservationSuccessListener;

    public ReservationPanel() {
        this.reservationService = new ReservationService();
        this.hotelService       = new HotelService();
        setLayout(new BorderLayout(0, 0));
        initComponents();
    }

    // ────────────────────────────────────────────────────────────────────
    // UI 초기화
    // ────────────────────────────────────────────────────────────────────

    private void initComponents() {
        // 상단 타이틀 + 뒤로가기
        add(buildTopBar(),    BorderLayout.NORTH);
        // 중앙: 예약 생성 화면
        add(buildCreateTab(), BorderLayout.CENTER);
    }

    /** 상단 타이틀 바 */
    private JPanel buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setBackground(new Color(40, 90, 180));
        bar.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));

        JLabel title = new JLabel("호텔 예약");
        title.setFont(new Font("맑은 고딕", Font.BOLD, 16));
        title.setForeground(Color.WHITE);

        JButton btnBack = new JButton("← 호텔 탐색으로");
        btnBack.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        btnBack.setForeground(Color.WHITE);
        btnBack.setBackground(new Color(60, 110, 200));
        btnBack.setFocusPainted(false);
        btnBack.setBorderPainted(false);
        btnBack.addActionListener(e -> { if (backListener != null) backListener.run(); });

        bar.add(title,   BorderLayout.WEST);
        bar.add(btnBack, BorderLayout.EAST);
        return bar;
    }

    // ────────────────────────────────────────────────────────────────────
    // 예약 생성 화면 구성
    // ────────────────────────────────────────────────────────────────────

    private JPanel buildCreateTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(12, 14, 12, 14));

        // 상단: 호텔 정보 + 날짜/인원 입력
        panel.add(buildHotelInfoAndDateInput(), BorderLayout.NORTH);
        // 중앙: 예약 가능 룸 테이블
        panel.add(buildRoomTable(),             BorderLayout.CENTER);
        // 하단: 고객 정보 입력 + 예약 버튼 + 결과
        panel.add(buildCustomerInputArea(),     BorderLayout.SOUTH);

        return panel;
    }

    /** 호텔 정보 라벨 + 날짜/인원 입력 */
    private JPanel buildHotelInfoAndDateInput() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180,180,180)),
                "① 날짜 및 인원 입력", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 12)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5, 8, 5, 8);
        g.anchor = GridBagConstraints.WEST;

        Font lf = new Font("맑은 고딕", Font.BOLD,  12);
        Font vf = new Font("맑은 고딕", Font.PLAIN, 12);

        // 체크인
        g.gridx=0; g.gridy=0; panel.add(lbl("체크인 (YYYY-MM-DD)", lf), g);
        g.gridx=1; tfCheckIn = new JTextField(12); tfCheckIn.setFont(vf);
        panel.add(tfCheckIn, g);

        // 체크아웃
        g.gridx=2; panel.add(lbl("체크아웃 (YYYY-MM-DD)", lf), g);
        g.gridx=3; tfCheckOut = new JTextField(12); tfCheckOut.setFont(vf);
        panel.add(tfCheckOut, g);

        // 인원
        g.gridx=0; g.gridy=1; panel.add(lbl("인원", lf), g);
        g.gridx=1; spGuests = new JSpinner(new SpinnerNumberModel(1,1,10,1));
        spGuests.setFont(vf); panel.add(spGuests, g);

        // 조회 버튼
        g.gridx=2; g.gridy=1; g.gridwidth=2;
        JButton btnSearch = makeBtn("예약 가능 룸 조회", new Color(50,120,220));
        btnSearch.addActionListener(e -> onSearchRooms());
        panel.add(btnSearch, g);

        return panel;
    }

    /** 예약 가능 룸 테이블 */
    private JScrollPane buildRoomTable() {
        String[] cols = {"룸 번호", "유형", "1박 가격(원)", "수용 인원"};
        roomTableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        roomTable = new JTable(roomTableModel);
        roomTable.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        roomTable.setRowHeight(24);
        roomTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        roomTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 12));

        JScrollPane sp = new JScrollPane(roomTable);
        sp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180,180,180)),
                "② 룸 선택 (클릭으로 선택)", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 12)));
        sp.setPreferredSize(new Dimension(0, 160));
        return sp;
    }

    /** 고객 정보 입력 + 예약 버튼 + 결과 출력 */
    private JPanel buildCustomerInputArea() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));

        // 고객 정보 입력
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180,180,180)),
                "③ 고객 정보 확인", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 12)));
        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(5,8,5,8);
        g.anchor = GridBagConstraints.WEST;

        Font lf = new Font("맑은 고딕", Font.BOLD,  12);
        Font vf = new Font("맑은 고딕", Font.PLAIN, 12);

        g.gridx=0; g.gridy=0; inputPanel.add(lbl("이름", lf), g);
        g.gridx=1; tfName = new JTextField(10); tfName.setFont(vf);
        inputPanel.add(tfName, g);

        g.gridx=2; inputPanel.add(lbl("이메일", lf), g);
        g.gridx=3; tfEmail = new JTextField(14); tfEmail.setFont(vf);
        inputPanel.add(tfEmail, g);

        g.gridx=4; inputPanel.add(lbl("휴대폰", lf), g);
        g.gridx=5; tfPhone = new JTextField(12); tfPhone.setFont(vf);
        inputPanel.add(tfPhone, g);

        g.gridx=6;
        JButton btnReserve = makeBtn("예약 완료", new Color(30,140,80));
        btnReserve.addActionListener(e -> onCreateReservation());
        inputPanel.add(btnReserve, g);

        // 결과 출력
        taResult = new JTextArea(5, 0);
        taResult.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
        taResult.setEditable(false);
        taResult.setBackground(new Color(245, 250, 245));
        JScrollPane resultSp = new JScrollPane(taResult);
        resultSp.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(180,180,180)),
                "예약 대기 상태", 0, 0,
                new Font("맑은 고딕", Font.BOLD, 12)));

        panel.add(inputPanel, BorderLayout.NORTH);
        panel.add(resultSp,   BorderLayout.CENTER);
        return panel;
    }

    // ────────────────────────────────────────────────────────────────────
    // 이벤트 핸들러
    // ────────────────────────────────────────────────────────────────────

    /** ① 예약 가능 룸 조회 버튼 */
    private void onSearchRooms() {
        if (targetHotelId == -1) {
            JOptionPane.showMessageDialog(this,
                    "호텔 탐색 화면에서 호텔을 먼저 선택해주세요.", "안내",
                    JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        LocalDate checkIn, checkOut;
        try {
            checkIn  = LocalDate.parse(tfCheckIn.getText().trim());
            checkOut = LocalDate.parse(tfCheckOut.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "날짜 형식이 올바르지 않습니다.\nYYYY-MM-DD 형식으로 입력해주세요.", "오류",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        String err = reservationService.validateDates(checkIn, checkOut);
        if (err != null) {
            JOptionPane.showMessageDialog(this, err, "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int guests = (int) spGuests.getValue();
        currentAvailableRooms = reservationService.getAvailableRooms(
                targetHotelId, checkIn, checkOut, guests);

        roomTableModel.setRowCount(0);
        if (currentAvailableRooms.isEmpty()) {
            JOptionPane.showMessageDialog(this, "선택한 조건에 예약 가능한 룸이 없습니다.", "조회 결과", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Room r : currentAvailableRooms) {
                roomTableModel.addRow(new Object[]{
                        r.getRoomNumber(),
                        r.getType(),
                        String.format("%,d", r.getPricePerNight()),
                        r.getCapacity() + "명"
                });
            }
        }
    }

    /** ② 예약 완료 버튼 */
    private void onCreateReservation() {
        // 룸 선택 확인
        int selectedRow = roomTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "룸을 선택해주세요.", "안내", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        int roomId = currentAvailableRooms.get(selectedRow).getRoomId();

        // 날짜 파싱
        LocalDate checkIn, checkOut;
        try {
            checkIn  = LocalDate.parse(tfCheckIn.getText().trim());
            checkOut = LocalDate.parse(tfCheckOut.getText().trim());
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                    "날짜 형식을 확인해주세요.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 로그인된 세션 상태를 검증하여 정상적인 사용자인지 확인합니다.
        if (this.loggedInCustomer == null) {
            JOptionPane.showMessageDialog(this,
                    "로그인 정보가 만료되었거나 접근 권한이 없습니다.", "인증 오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 입력된 텍스트 대신, 안전한 세션에 보관된 고객 객체를 예약 서비스로 전달합니다.
        Customer customer = this.loggedInCustomer;
        int guests = (int) spGuests.getValue();

        // 예약 생성 (트랜잭션)
        int result = reservationService.createReservation(
                customer, roomId, checkIn, checkOut, guests);

        if (result == -2) {
            JOptionPane.showMessageDialog(this,
                    "선택한 기간에 이미 예약이 있습니다.\n다른 날짜나 룸을 선택해주세요.",
                    "예약 불가", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (result == -1) {
            JOptionPane.showMessageDialog(this,
                    "예약 생성 중 오류가 발생했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // 뷰(View)를 활용한 예약 상세 정보 조회 로직 (요구사항 view)
        String[] detail = reservationService.getReservationDetail(result);
        if (detail != null) {
            String priceStr = roomTableModel.getValueAt(selectedRow, 2).toString().replace(",", "");
            int pricePerNight = Integer.parseInt(priceStr);
            int totalPrice = reservationService.calcTotalPrice(pricePerNight, checkIn, checkOut);

            String successMessage =
                      "╔══════════════════════════╗\n" +
                      "   🎉 예약이 성공적으로 완료되었습니다!\n" +
                      "╚══════════════════════════╝\n" +
                      "  예약 ID   : " + result        + "\n" +
                      "  호텔명    : " + detail[0]      + "\n" +
                      "  룸 유형   : " + detail[1]      + "\n" +
                      "  체크인    : " + detail[2]      + "\n" +
                      "  체크아웃  : " + detail[3]      + "\n" +
                      "  인원      : " + detail[4] + "명\n" +
                      "  총 요금   : " + String.format("%,d", totalPrice) + "원\n" +
                      "  예약자    : " + detail[6] + "\n\n" +
                      "▶ 확인을 누르시면 호텔 탐색 화면으로 이동합니다.";

            JOptionPane.showMessageDialog(this, successMessage, "예약 성공", JOptionPane.INFORMATION_MESSAGE);
        }
        // 입력 필드 및 테이블 초기화
        tfCheckIn.setText("");
        tfCheckOut.setText("");
        spGuests.setValue(1);
        roomTableModel.setRowCount(0);
        taResult.setText("예약 대기 상태입니다.");

        if (reservationSuccessListener != null) {
            reservationSuccessListener.run();
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // 외부 연동 메서드
    // ────────────────────────────────────────────────────────────────────

    /**
     * 메인 프레임 등에서 성공적으로 로그인된 사용자의 정보를 이 패널에 주입합니다.
     * 주입된 고객 정보는 UI 텍스트 필드에 고정 출력되며 사용자가 임의로 변조할 수 없습니다.
     * @param customer 세션에 보관된 현재 로그인 유저
     */
    public void setLoggedInCustomer(Customer customer) {
        this.loggedInCustomer = customer;
        if (customer != null) {
            tfName.setText(customer.getName());
            tfEmail.setText(customer.getEmail());
            tfPhone.setText(customer.getPhone() != null ? customer.getPhone() : "");

            tfName.setEditable(false);
            tfEmail.setEditable(false);
            tfPhone.setEditable(false);

            Color readOnlyColor = new Color(240, 240, 240);
            tfName.setBackground(readOnlyColor);
            tfEmail.setBackground(readOnlyColor);
            tfPhone.setBackground(readOnlyColor);
        }
    }

    /**
     * 호텔 탐색 화면에서 예약 버튼 클릭 시 호출됩니다.
     * 선택된 호텔 ID를 설정하고 화면 상단에 호텔명을 표시합니다.
     * @param hotelId 선택된 호텔 ID
     */
    public void setTargetHotel(int hotelId) {
        this.targetHotelId = hotelId;
        // 호텔명 조회해서 안내 표시
        var hotel = hotelService.getHotelById(hotelId);
        if (hotel != null) {
            tfCheckIn.setToolTipText("선택된 호텔: " + hotel.getHotelName());
            taResult.setText("선택된 호텔: " + hotel.getHotelName()
                    + "\n\n날짜와 인원을 입력하고 [예약 가능 룸 조회] 버튼을 눌러주세요.");
        }
        // 이전 룸 목록 초기화
        roomTableModel.setRowCount(0);
    }

    /**
     * 뒤로가기(호텔 탐색 화면) 콜백 등록
     */
    public void setBackListener(Runnable listener) {
        this.backListener = listener;
    }

    /**
     * 예약 성공 시 이동 등 추가 동작을 위한 콜백 등록
     */
    public void setReservationSuccessListener(Runnable listener) {
        this.reservationSuccessListener = listener;
    }

    // ────────────────────────────────────────────────────────────────────
    // 내부 헬퍼
    // ────────────────────────────────────────────────────────────────────

    private JLabel lbl(String text, Font font) {
        JLabel l = new JLabel(text); l.setFont(font); return l;
    }

    private JButton makeBtn(String text, Color bg) {
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