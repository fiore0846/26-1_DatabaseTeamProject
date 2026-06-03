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
 * 마이페이지 패널
 * 고객의 예약 내역을 조회(Select), 수정(Update), 취소(Delete)하는 통합 관리 화면입니다.
 */
public class MyPagePanel extends JPanel {

    private final ReservationService reservationService;
    private Customer loggedInCustomer;

    private DefaultTableModel tableModel;
    private JTable reservationTable;

    public MyPagePanel(ReservationService reservationService) {
        this.reservationService = reservationService;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(20, 20, 20, 20));
        setBackground(Color.WHITE);
        initComponents();
    }

    // 마이페이지 진입 시 호출하여 로그인된 유저 정보를 세팅하고 표를 새로고침합니다.
    public void setLoggedInCustomer(Customer customer) {
        this.loggedInCustomer = customer;
        refreshTableData();
    }

    private void initComponents() {
        // 상단 타이틀
        JLabel titleLabel = new JLabel("내 예약 관리 (마이페이지)");
        titleLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        add(titleLabel, BorderLayout.NORTH);

        // 중앙 테이블 (예약 내역 출력)
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

        // 하단 버튼 영역
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

    // 예약 내역 갱신 (Select)
    private void refreshTableData() {
        tableModel.setRowCount(0);
        if (loggedInCustomer == null) return;

        List<String[]> reservations = reservationService.getReservationsByCustomerId(loggedInCustomer.getCustomerId());
        for (String[] row : reservations) {
            tableModel.addRow(row);
        }
    }

    // 예약 취소 (Delete)
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
                refreshTableData(); // 삭제 후 표 새로고침
            } else {
                JOptionPane.showMessageDialog(this, "예약 취소에 실패했습니다.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // 예약 수정 (Update)
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

        // 수정 입력창 띄우기
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
                    refreshTableData(); // 수정 후 표 새로고침
                } else {
                    JOptionPane.showMessageDialog(this, "날짜가 유효하지 않거나 수정에 실패했습니다.");
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "입력 형식이 잘못되었습니다. (날짜 형식: YYYY-MM-DD)");
            }
        }
    }
}