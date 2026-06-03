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
 * - 트랜잭션     (요구사항 9번)  : createReservation() - 고객 저장 + 예약 생성 원자적 처리
 * - 부속질의     (요구사항 10번) : checkAvailability()  - 중복 예약 방지 서브쿼리
 * - 뷰 사용      (요구사항 8번)  : findReservationDetail(), showRoomInfo(), viewReservation()
 * - 조인         (요구사항 11번) : findAvailableRooms()    - Room JOIN Hotel
 * - 동적 쿼리    (요구사항 12번) : findAvailableRooms()    - 날짜/인원 파라미터
 * - Insert       (요구사항 14번) : createReservation()
 * - Update       (요구사항 15번) : updateReservation()
 * - Delete       (요구사항 16번) : cancelReservation()
 * - Select       (요구사항 17번) : findReservationDetail(), findAvailableRooms()
 */
public class ReservationDAO {

    // ────────────────────────────────────────────────────────────────────
    // 1. 룸 및 예약 가능 여부 조회 기능
    // ────────────────────────────────────────────────────────────────────

    // 룸 정보 출력하기 위해 룸 객체 리스트 생성
    public List<Room> showRoomInfo() throws Exception {
        String sql = "SELECT room_number, type, price_per_night, capacity, hotel_id FROM vRoomInfo";

        List<Room> rooms = new ArrayList<>();
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            // rs 다음 객체 없을 때까지 룸 정보 검색하여 리스트에 추가
            while (rs.next()) {
                Room room = new Room(
                        rs.getInt("room_number"),
                        rs.getString("type"),
                        rs.getInt("price_per_night"),
                        rs.getInt("capacity"),
                        rs.getInt("hotel_id")
                );
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
        }
        return rooms;
    }

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

    // 예약 가능 여부 확인 (날짜 겹치는 예약이 있는지 체크)
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

    // ────────────────────────────────────────────────────────────────────
    // 2. 고객 정보 관리 기능
    // ────────────────────────────────────────────────────────────────────

    public int checkInCustomer(String name, String phone, String email) throws Exception {
        // 고객 정보 있는지 확인하고 고객 번호(customer_id) 반환
        String sql = "SELECT customer_id FROM Customer WHERE name = ? AND phone = ? AND email = ?";
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, name);
            pstmt.setString(2, phone);
            pstmt.setString(3, email);

            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1); // SELECT문으로 조회한 customer_id 반환
            }
            return 0;
        } catch (SQLException e) {
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
            return 0;
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
     * 둘 중 하나라도 실패하면 전체 롤백합니다.
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

    public boolean createReservation(Reservation reservation) throws Exception {
        // 예약 생성 (INSERT)
        String sql = "INSERT INTO Reservation (check_in, check_out, reservation_date, guests, room_number, customer_id) VALUES (?, ?, ?, ?, ?, ?)";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            // sql문에 입력값 넣어 동적으로 조회
            pstmt.setDate(1, Date.valueOf(reservation.getCheckIn()));
            pstmt.setDate(2, Date.valueOf(reservation.getCheckOut()));
            pstmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
            pstmt.setInt(4, reservation.getGuests());
            pstmt.setInt(5, reservation.getRoomNumber());
            pstmt.setInt(6, reservation.getCustomerId());

            return pstmt.executeUpdate() > 0; //executeUpdate(): INSERT / DELETE / UPDATE 관련 구문에서는 반영된 레코드의 건수를 반환
        } catch (SQLException e) {
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
            return false;
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

    public boolean viewReservation(int customerId) throws Exception {
        //예약 확인
        String sql = "SELECT customerId, reservationId, customer_name, room_number, room_type, price_per_night, guests, check_in, check_out, reservation_date"
                + " FROM vReservationDetail WHERE customerId = ?";

        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, customerId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("예약 번호 | 고객명 | 객실 번호 | 객실 유형 | 1박 당 가격 | 일행 수 | 체크인 날짜 | 체크아웃 날짜 | 예약일");
            boolean hasReservation = false;

            while (rs.next()) {
                // rs.next()가 true이면 예약 정보 있는 것
                hasReservation = true;

                int reservationId = rs.getInt("reservationId");
                String name = rs.getString("customer_name");
                int roomNumber = rs.getInt("room_number");
                String roomType = rs.getString("room_type");
                int pricePerNight = rs.getInt("price_per_night");
                int guests = rs.getInt("guests");
                String checkIn = rs.getString("check_in");
                String checkOut = rs.getString("check_out");
                String reservationDate = rs.getString("reservation_date");

                // 형식에 맞춰 출력
                System.out.printf("%d, %s, %d, %s, %d, %d, %s, %s, %s\n", reservationId, name, roomNumber, roomType, pricePerNight, guests, checkIn, checkOut, reservationDate);
            }

            if (!hasReservation) {
                System.out.println("해당 고객의 예약 내역이 존재하지 않습니다.");
            }
            return true;
        } catch (SQLException e) {
            System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
            System.out.print("사유 : " + e.getMessage());
            return false;
        }
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
     * [GUI 마이페이지용] 특정 고객의 예약 내역을 리스트로 반환합니다.
     */
    public List<String[]> findReservationsByCustomerId(int customerId) {
        List<String[]> list = new ArrayList<>();
        // 팀원이 만들어둔 vReservationDetail 뷰를 활용합니다.
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
            System.err.println("[오류] 마이페이지 예약 조회 실패: " + e.getMessage());
        }
        return list;
    }
}