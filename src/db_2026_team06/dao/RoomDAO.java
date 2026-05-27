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
     특정 호텔의 모든 객실 정보를 가져오는 메서드
     */
    public List<Room> getRoomsByHotelId(int hotelId) {
        List<Room> roomList = new ArrayList<>();
        String sql = "SELECT * FROM Room WHERE hotel_id = ?";

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

    /**
     특정 객실의 상세 정보를 가져오는 메서드
     */
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

    /**
     특정 객실이 해당 날짜에 예약 가능한지 확인하는 메서드
     */
    public boolean checkRoomAvailability(int roomNumber, LocalDate checkIn, LocalDate checkOut) {
        boolean isAvailable = false;

        // 겹치는 일정이 있는지 확인 (COUNT가 0이면 예약 가능)
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
                    isAvailable = (count == 0); // 겹치는 예약이 0개면 true 반환
                }
            }
        } catch (SQLException e) {
            System.err.println("객실 예약 가능 여부 확인 중 문제가 발생했습니다.");
            e.printStackTrace();
        }
        return isAvailable;
    }
}