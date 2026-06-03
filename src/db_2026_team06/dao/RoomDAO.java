package db_2026_team06.dao;

import db_2026_team06.model.Room;
import db_2026_team06.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomDAO {

    /**
     * 특정 호텔의 모든 객실 정보를 가져오는 메서드 (JOIN 활용)
     */
    public List<Room> getRoomsByHotelId(int hotelId) {
        List<Room> roomList = new ArrayList<>();
        // 요구사항 11번 조인 충족을 위해 Hotel 테이블과 조인하는 쿼리로 변경
        String sql = "SELECT r.room_number, r.type, r.price_per_night, r.capacity, r.hotel_id "
                + "FROM Room r "
                + "INNER JOIN Hotel h ON r.hotel_id = h.hotel_id "
                + "WHERE r.hotel_id = ? "
                + "ORDER BY r.room_number";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Room room = new Room();
                    room.setRoomNumber(rs.getInt("room_number"));
                    room.setType(rs.getString("type"));
                    room.setPricePerNight(rs.getInt("price_per_night"));
                    room.setCapacity(rs.getInt("capacity"));
                    room.setHotelId(rs.getInt("hotel_id"));
                    roomList.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("객실 목록 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }

        return roomList;
    }

    public Room getRoomDetail(int roomNumber) {
        Room room = null;
        String sql = "SELECT * FROM Room WHERE room_number = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    room = new Room();
                    room.setRoomNumber(rs.getInt("room_number"));
                    room.setType(rs.getString("type"));
                    room.setPricePerNight(rs.getInt("price_per_night"));
                    room.setCapacity(rs.getInt("capacity"));
                    room.setHotelId(rs.getInt("hotel_id"));
                }
            }
        } catch (SQLException e) {
            System.err.println("객실 상세 정보 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }
        return room;
    }

    public boolean checkRoomAvailability(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        boolean isAvailable = false;
        String sql = "SELECT COUNT(*) FROM Reservation "
                + "WHERE room_number = ? "
                + "AND check_out > ? AND check_in < ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, roomNumber);
            pstmt.setObject(2, checkIn);
            pstmt.setObject(3, checkOut);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    isAvailable = (count == 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("객실 예약 가능 여부 확인 중 문제가 발생했습니다.");
            e.printStackTrace();
        }
        return isAvailable;
    }
}