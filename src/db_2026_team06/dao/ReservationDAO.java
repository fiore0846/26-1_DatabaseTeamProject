package db_2026_team06.dao;

import db_2026_team06.model.Customer;
import db_2026_team06.model.Room;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Reservation, Customer 테이블에 대한 데이터 접근 객체(DAO)
 *
 * 프로젝트 요구사항 대응:
 * - 인덱스 사용  (요구사항 ７번) : checkAvailability(), findAvailableRooms()
 * - 트랜잭션     (요구사항 9번)  : createReservation() - 신규 고객 등록(Insert)과 예약 생성(Insert)을 원자적으로 처리
 * - 부속질의     (요구사항 10번) : findAvailableRooms() - 예약된 방을 걸러내는 NOT IN 서브쿼리 사용
 * insertOrGetCustomer() - 이메일 중복 방지를 위한 서브쿼리 사용
 * - 뷰 사용      (요구사항 8번)  : findReservationDetail() - reservation_detail 뷰 사용
 * findReservationsByCustomerId() - vReservationDetail 뷰 사용
 * - 조인         (요구사항 11번) : findAvailableRooms() - Room 테이블과 Hotel 테이블 INNER JOIN
 * - 동적 쿼리    (요구사항 12번) : findAvailableRooms() 등 다수 - PreparedStatement를 통한 날짜/인원 파라미터(?) 바인딩
 * - Insert       (요구사항 14번) : createReservation(), insertOrGetCustomer()
 * - Update       (요구사항 15번) : updateReservation()
 * - Delete       (요구사항 16번) : cancelReservation()
 * - Select       (요구사항 17번) : findAvailableRooms(), findReservationDetail() 등 다수
 */
public class ReservationDAO {

    // ────────────────────────────────────────────────────────────────────
    // 1. 룸 및 예약 가능 여부 조회 기능
    // ────────────────────────────────────────────────────────────────────

    /**
     * 선택한 호텔에서 날짜와 인원 조건에 맞는 예약 가능한 룸 목록을 조회합니다.
     * Room JOIN Hotel 사용 (요구사항 11번 - 조인)
     * 날짜/인원 파라미터 동적 적용 (요구사항 12번 - 동적 쿼리)
     *
     * @param hotelId  선택한 호텔 ID
     * @param checkIn  체크인 날짜
     * @param checkOut 체크아웃 날짜
     * @param guests   인원 수
     * @return 예약 가능한 Room 리스트
     */
    public List<Room> findAvailableRooms(int hotelId, LocalDate checkIn,
                                         LocalDate checkOut, int guests) {
        List<Room> list = new ArrayList<>();
        String sql = "SELECT r.room_number, r.type, r.price_per_night, r.capacity, r.hotel_id "
                + "FROM Room r "
                + "INNER JOIN Hotel h ON r.hotel_id = h.hotel_id "
                + "WHERE r.hotel_id = ? "
                + "  AND r.capacity >= ? "
                + "  AND r.room_number NOT IN ( "
                + "      SELECT room_number FROM Reservation "
                + "      WHERE NOT (check_out <= ? OR check_in >= ?) "
                + "  ) "
                + "ORDER BY r.price_per_night";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt (1, hotelId);
            pstmt.setInt (2, guests);
            pstmt.setDate(3, Date.valueOf(checkIn));
            pstmt.setDate(4, Date.valueOf(checkOut));
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Room(
                            rs.getInt("room_number"),
                            rs.getString("type"),
                            rs.getInt("price_per_night"),
                            rs.getInt("capacity"),
                            rs.getInt("hotel_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[오류] 예약 가능 룸 조회 실패: " + e.getMessage());
        }
        return list;
    }

    // 예약 가능 여부 확인 (날짜가 겹치는 예약이 있는지 체크)
    public boolean checkAvailability(int roomId, LocalDate checkIn, LocalDate checkOut) throws Exception {
        String sql = "SELECT COUNT(*) FROM Reservation "
                + "WHERE room_number = ? AND (check_in <= ? AND check_out >= ?)";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // sql문에 입력값 넣어 동적으로 조회
            pstmt.setInt(1, roomId);
            pstmt.setDate(2, Date.valueOf(checkOut));
            pstmt.setDate(3, Date.valueOf(checkIn));

            ResultSet rs = pstmt.executeQuery();
            rs.next();
            return rs.getInt(1) == 0; //0이면 예약 가능 -> true 반환
        } catch (SQLException e){
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
            return false;
        }
    }



    /**
     * GUI 기반의 예약 시스템에서 사용되는 메서드로, 이메일로 기존 고객을 조회하고 없으면 새로 삽입합니다.
     * @return customer_id
     */
    private int insertOrGetCustomer(Connection conn, Customer customer) throws SQLException {
        String selectSql = "SELECT customer_id FROM Customer WHERE customer_id = "
                + "(SELECT MIN(customer_id) FROM Customer WHERE email = ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, customer.getEmail());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("customer_id");
            }
        }
        String insertSql = "INSERT INTO Customer (name, email, phone) VALUES (?, ?, ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setString(1, customer.getName());
            pstmt.setString(2, customer.getEmail());
            pstmt.setString(3, customer.getPhone());
            pstmt.executeUpdate();
            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("고객 삽입 실패");
    }

    // ────────────────────────────────────────────────────────────────────
    // 3. 예약 생성 및 관리 (CRUD)
    // ────────────────────────────────────────────────────────────────────

    /**
     * GUI 예약 시스템에서 호출되는 메서드로, 고객 정보 저장과 예약 생성을 하나의 트랜잭션으로 처리합니다.
     * 트랜잭션으로 묶어 둘 중 하나라도 실패하면 전체 롤백합니다.
     */
    public int createReservation(Customer customer, int roomNumber,
                                 LocalDate checkIn, LocalDate checkOut, int guests) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            int customerId = insertOrGetCustomer(conn, customer);

            String sql = "INSERT INTO Reservation "
                    + "(check_in, check_out, reservation_date, guests, room_number, customer_id) "
                    + "VALUES (?, ?, ?, ?, ?, ?)";
            int reservationId = -1;
            try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setDate(1, Date.valueOf(checkIn));
                pstmt.setDate(2, Date.valueOf(checkOut));
                pstmt.setDate(3, Date.valueOf(LocalDate.now()));
                pstmt.setInt (4, guests);
                pstmt.setInt (5, roomNumber);
                pstmt.setInt (6, customerId);
                pstmt.executeUpdate();

                try (ResultSet keys = pstmt.getGeneratedKeys()) {
                    if (keys.next()) reservationId = keys.getInt(1);
                }
            }
            conn.commit();
            return reservationId;

        } catch (SQLException e) {
            System.err.println("[오류] 예약 생성 실패, 롤백합니다: " + e.getMessage());
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            return -1;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    /**
     * GUI 기반 시스템에서 reservation_detail 뷰를 사용하여 예약 상세 정보를 배열 형태로 반환합니다.
     */
    public String[] findReservationDetail(int reservationId) {
        String sql = "SELECT hotel_name, room_type, check_in, check_out, "
                + "       guests, total_price, customer_name "
                + "FROM reservation_detail "
                + "WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new String[]{
                            rs.getString("hotel_name"),
                            rs.getString("room_type"),
                            rs.getDate  ("check_in").toString(),
                            rs.getDate  ("check_out").toString(),
                            String.valueOf(rs.getInt("guests")),
                            String.format("%,d원", rs.getInt("total_price")),
                            rs.getString("customer_name")
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("[오류] 예약 상세 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 예약의 체크인/체크아웃 날짜와 인원을 업데이트합니다.
     */
    public boolean updateReservation(int reservationId, LocalDate checkIn,
                                     LocalDate checkOut, int guests) {
        String sql = "UPDATE Reservation "
                + "SET check_in = ?, check_out = ?, guests = ? "
                + "WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(checkIn));
            pstmt.setDate(2, Date.valueOf(checkOut));
            pstmt.setInt (3, guests);
            pstmt.setInt (4, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[오류] 예약 수정 실패: " + e.getMessage());
            return false;
        }
    }

    public boolean cancelReservation(int reservationId) throws Exception {
        // 예약 취소 (DELETE)
        String sql = "DELETE FROM Reservation WHERE reservation_id = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // sql문에 입력값 넣어 동적으로 조회
            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
            return false;
        }
    }

    /**
     * 특정 고객의 예약 내역을 리스트로 반환합니다. (뷰 활용)
     */
    public List<String[]> findReservationsByCustomerId(int customerId) {
        List<String[]> list = new ArrayList<>();
        // vReservationDetail 뷰를 활용합니다.
        String sql = "SELECT reservationId, room_number, room_type, guests, check_in, check_out, reservation_date "
                + "FROM vReservationDetail WHERE customerId = ? ORDER BY check_in DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new String[]{
                            String.valueOf(rs.getInt("reservationId")),
                            String.valueOf(rs.getInt("room_number")),
                            rs.getString("room_type"),
                            String.valueOf(rs.getInt("guests")),
                            rs.getString("check_in"),
                            rs.getString("check_out"),
                            rs.getString("reservation_date")
                    });
                }
            }
        } catch (SQLException e) {
            System.err.println("마이페이지 예약 조회 실패: " + e.getMessage());
        }
        return list;
    }
}