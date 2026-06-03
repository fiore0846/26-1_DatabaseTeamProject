package db_2026_team06.dao;

import db_2026_team06.model.Customer;
import db_2026_team06.model.Reservation;
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
 *  - 트랜잭션     (요구사항 9번)  : createReservation() - 고객 저장 + 예약 생성 원자적 처리
 *  - 부속질의     (요구사항 10번) : checkAvailability()  - 중복 예약 방지 서브쿼리
 *  - 뷰 사용      (요구사항 8번)  : findReservationDetail() - reservation_detail 뷰
 *  - 조인         (요구사항 11번) : findAvailableRooms()    - Room JOIN Hotel
 *  - 동적 쿼리    (요구사항 12번) : findAvailableRooms()    - 날짜/인원 파라미터
 *  - Insert       (요구사항 14번) : createReservation()
 *  - Update       (요구사항 15번) : updateReservation()
 *  - Delete       (요구사항 16번) : cancelReservation()
 *  - Select       (요구사항 17번) : findReservationDetail(), findAvailableRooms()
 */
public class ReservationDAO {

    // ────────────────────────────────────────────────────────────────────
    // 예약 생성 (트랜잭션 - 요구사항 9번)
    // ────────────────────────────────────────────────────────────────────

    /**
     * 고객 정보 저장 + 예약 생성을 하나의 트랜잭션으로 처리합니다.
     * 둘 중 하나라도 실패하면 전체 롤백합니다. (요구사항 9번 - 트랜잭션)
     *
     * @param customer   예약자 고객 정보
     * @param roomNumber 선택한 룸 번호
     * @param checkIn    체크인 날짜
     * @param checkOut   체크아웃 날짜
     * @param guests     인원 수
     * @return 생성된 reservation_id, 실패 시 -1
     */
    public int createReservation(Customer customer, int roomNumber,
                                  LocalDate checkIn, LocalDate checkOut, int guests) {
        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            // 트랜잭션 시작 (요구사항 9번)
            conn.setAutoCommit(false);

            // ① 고객 정보 저장 (INSERT - 요구사항 14번)
            int customerId = insertOrGetCustomer(conn, customer);

            // ② 예약 생성 (INSERT - 요구사항 14번)
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

            // 트랜잭션 커밋
            conn.commit();
            return reservationId;

        } catch (SQLException e) {
            // 트랜잭션 롤백
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
     * 이메일로 기존 고객을 조회하고, 없으면 새로 삽입합니다.
     * @return customer_id
     */
    private int insertOrGetCustomer(Connection conn, Customer customer) throws SQLException {
        // 이메일로 기존 고객 조회 (부속질의 활용 - 요구사항 10번)
        String selectSql = "SELECT customer_id FROM Customer WHERE customer_id = "
                         + "(SELECT MIN(customer_id) FROM Customer WHERE email = ?)";
        try (PreparedStatement pstmt = conn.prepareStatement(selectSql)) {
            pstmt.setString(1, customer.getEmail());
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt("customer_id");
            }
        }
        // 신규 고객 삽입 (INSERT - 요구사항 14번)
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
    // 예약 가능 룸 조회 (동적 쿼리 + 조인 - 요구사항 11, 12번)
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
        // Room JOIN Hotel + 예약 중복 제외 서브쿼리 (요구사항 10, 11, 12번)
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

    // ────────────────────────────────────────────────────────────────────
    // 예약 상세 조회 - 뷰 사용 (요구사항 8번)
    // ────────────────────────────────────────────────────────────────────

    /**
     * reservation_detail 뷰를 사용하여 예약 상세 정보를 조회합니다.
     * (요구사항 8번 - 뷰를 사용하는 쿼리)
     * @param reservationId 조회할 예약 ID
     * @return [호텔명, 룸유형, 체크인, 체크아웃, 인원, 총가격, 고객명] 배열, 없으면 null
     */
    public String[] findReservationDetail(int reservationId) {
        // reservation_detail 뷰 사용 (요구사항 8번)
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

    // ────────────────────────────────────────────────────────────────────
    // 예약 수정 (요구사항 15번 - Update)
    // ────────────────────────────────────────────────────────────────────

    /**
     * 예약의 체크인/체크아웃 날짜와 인원을 수정합니다.
     * (요구사항 15번 - Update 기능)
     * @return 수정 성공 여부
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

    // ────────────────────────────────────────────────────────────────────
    // 예약 취소 (요구사항 16번 - Delete)
    // ────────────────────────────────────────────────────────────────────

    /**
     * 예약을 취소(삭제)합니다.
     * (요구사항 16번 - Delete 기능)
     * @return 취소 성공 여부
     */
    public boolean cancelReservation(int reservationId) {
        String sql = "DELETE FROM Reservation WHERE reservation_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, reservationId);
            return pstmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[오류] 예약 취소 실패: " + e.getMessage());
            return false;
        }
    }

    // ────────────────────────────────────────────────────────────────────
    // 중복 예약 확인 (요구사항 10번 - 부속질의)
    // ────────────────────────────────────────────────────────────────────

    /**
     * 해당 룸이 선택한 기간에 이미 예약되어 있는지 확인합니다.
     * 서브쿼리로 중복 기간을 판단합니다. (요구사항 10번 - 부속질의)
     * @return true면 예약 불가(중복), false면 예약 가능
     */
    public boolean checkAvailability(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        // 부속질의로 중복 예약 여부 확인 (요구사항 10번)
        String sql = "SELECT COUNT(*) FROM Reservation "
                   + "WHERE room_number = ? "
                   + "  AND room_number IN ( "
                   + "      SELECT room_number FROM Reservation "
                   + "      WHERE NOT (check_out <= ? OR check_in >= ?) "
                   + "  )";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt (1, roomNumber);
            pstmt.setDate(2, Date.valueOf(checkIn));
            pstmt.setDate(3, Date.valueOf(checkOut));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("[오류] 예약 가능 여부 확인 실패: " + e.getMessage());
        }
        return true;
    }
}
