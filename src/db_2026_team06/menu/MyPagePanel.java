package db_2026_team06.menu;

import db_2026_team06.model.Customer;
import db_2026_team06.service.ReservationService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.LocalDate;
import java.util.List;

/**
 * 고객의 개인 예약 내역을 통합 관리하는 마이페이지 UI 패널입니다.
 * 예약 목록 조회(Select), 예약 수정(Update), 예약 취소(Delete) 기능을 제공합니다.
 */
public class MyPagePanel extends JPanel {

    private final ReservationService reservationService;
    private Customer loggedInCustomer;

    private DefaultTableModel tableModel;
    private JTable reservationTable;

    /**
     * MyPagePanel 생성자
     * 서비스 계층 객체를 주입받고 레이아웃을 초기화합니다.
     */
    public MyPagePanel(ReservationService reservationService) {
        this.reservationService = reservationService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        initComponents();
    }

    /**
     * 현재 로그인된 고객 세션을 등록하고, 해당 고객의 예약 데이터를 화면에 로드합니다.
     * @param customer 세션에 보관된 현재 로그인 유저
     */
    public void setLoggedInCustomer(Customer customer) {
        this.loggedInCustomer = customer;
        refreshTableData();
    }

    /**
     * 마이페이지 화면의 상단 타이틀, 데이터 출력용 테이블 뷰, 그리고 하단 제어 버튼 영역을 구성합니다.
     */
    private void initComponents() {
        JLabel titleLabel = new JLabel("내 예약 관리 (마이페이지)");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"예약 번호", "객실 번호", "객실 유형", "인원", "체크인", "체크아웃", "예약일"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        reservationTable = new JTable(tableModel);
        reservationTable.setFont(new Font("맑은 고딕", Font.PLAIN, 14));
        reservationTable.setRowHeight(25);
        reservationTable.getTableHeader().setFont(new Font("맑은 고딕", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(reservationTable);
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton btnUpdate = new JButton("예약 날짜/인원 수정");
        btnUpdate.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnUpdate.addActionListener(e -> updateReservation());

        JButton btnCancel = new JButton("예약 취소");
        btnCancel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        btnCancel.setBackground(new Color(220, 80, 80));
        btnCancel.addActionListener(e -> cancelReservation());

        buttonPanel.add(btnUpdate);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 데이터베이스에서 최신 예약 내역을 조회(Select)하여 UI 테이블을 갱신합니다.
     */
    private void refreshTableData() {
        tableModel.setRowCount(0);
        if (loggedInCustomer == null) return;

        List<String[]> reservations = reservationService.getReservationsByCustomerId(loggedInCustomer.getCustomerId());
        for (String[] row : reservations) {
            tableModel.addRow(row);
        }
    }

    /**
     * 사용자가 선택한 예약 건에 대해 데이터베이스 삭제(Delete) 작업을 요청합니다.
     */
    private void cancelReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "취소할 예약을 먼저 선택해주세요.");
            return;
        }

        int reservationId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));

        int confirm = JOptionPane.showConfirmDialog(this, "정말로 이 예약을 취소하시겠습니까?", "예약 취소 확인", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = reservationService.cancelReservation(reservationId);
            if (success) {
                JOptionPane.showMessageDialog(this, "예약이 성공적으로 취소되었습니다.");
                refreshTableData();
            } else {
                JOptionPane.showMessageDialog(this, "예약 취소에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * 사용자가 선택한 예약 건의 체크인, 체크아웃 날짜 및 인원수를 수정(Update) 요청합니다.
     */
    private void updateReservation() {
        int selectedRow = reservationTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "수정할 예약을 먼저 선택해주세요.");
            return;
        }

        int reservationId = Integer.parseInt((String) tableModel.getValueAt(selectedRow, 0));
        String currentCheckIn = (String) tableModel.getValueAt(selectedRow, 4);
        String currentCheckOut = (String) tableModel.getValueAt(selectedRow, 5);
        String currentGuests = (String) tableModel.getValueAt(selectedRow, 3);

        JTextField txtCheckIn = new JTextField(currentCheckIn);
        JTextField txtCheckOut = new JTextField(currentCheckOut);
        JTextField txtGuests = new JTextField(currentGuests);

        Object[] message = {
                "새 체크인 날짜 (YYYY-MM-DD):", txtCheckIn,
                "새 체크아웃 날짜 (YYYY-MM-DD):", txtCheckOut,
                "새 인원 수:", txtGuests
        };

        int option = JOptionPane.showConfirmDialog(this, message, "예약 수정", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            try {
                LocalDate checkIn = LocalDate.parse(txtCheckIn.getText());
                LocalDate checkOut = LocalDate.parse(txtCheckOut.getText());
                int guests = Integer.parseInt(txtGuests.getText());

                boolean success = reservationService.updateReservation(reservationId, checkIn, checkOut, guests);
                if (success) {
                    JOptionPane.showMessageDialog(this, "예약이 성공적으로 수정되었습니다.");
                    refreshTableData();
                } else {
                    JOptionPane.showMessageDialog(this, "날짜가 유효하지 않거나 수정에 실패했습니다.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "입력 형식이 잘못되었습니다. (날짜 형식: YYYY-MM-DD)");
            }
        }
    }
}