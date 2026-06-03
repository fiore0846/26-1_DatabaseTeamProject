package db_2026_team06.dao;

import db_2026_team06.model.Hotel;
import db_2026_team06.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {

    /**
      사용자의 입력(지역, 날짜, 인원)에 따라 예약 가능한 호텔 목록을 검색하는 동적 쿼리 메서드
     */
    public List<Hotel> searchAvailableHotels(String location, LocalDate checkIn, LocalDate checkOut, int guests) {
        List<Hotel> hotelList = new ArrayList<>();

        // 동적 쿼리 조립을 위한 StringBuilder
        StringBuilder sql = new StringBuilder("SELECT DISTINCT h.* FROM Hotel h ");

        boolean hasLocation = (location != null && !location.trim().isEmpty());
        boolean hasDatesAndGuests = (checkIn != null && checkOut != null && guests > 0);

        // 1. 지역 조건이 있을 경우
        if (hasLocation) {
            sql.append("WHERE h.location = ? ");
        }

        // 2. 날짜 및 인원 조건이 있을 경우 (부속질의)
        if (hasDatesAndGuests) {
            if (hasLocation) sql.append("AND ");
            else sql.append("WHERE ");

            // Reservation 테이블에 해당 기간이 없는 방을 가진 호텔만 필터링
            sql.append("h.hotel_id IN (")
                    .append("  SELECT r.hotel_id FROM Room r ")
                    .append("  WHERE r.capacity >= ? ")
                    .append("  AND r.room_number NOT IN (")
                    .append("    SELECT res.room_number FROM Reservation res ")
                    .append("    WHERE res.check_out > ? AND res.check_in < ?")
                    .append("  )")
                    .append(") ");
        }

        // 자원 반납 처리
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            // 동적 쿼리에 따른 매개변수 인덱스 매핑
            int paramIndex = 1;

            if (hasLocation) {
                pstmt.setString(paramIndex++, location);
            }
            if (hasDatesAndGuests) {
                pstmt.setInt(paramIndex++, guests);
                // LocalDate를 java.sql.Date로 변환하여 세팅
                pstmt.setDate(paramIndex++, java.sql.Date.valueOf(checkIn));
                pstmt.setDate(paramIndex++, java.sql.Date.valueOf(checkOut));
            }

            // 쿼리 실행 및 결과 처리
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Hotel hotel = new Hotel();
                    hotel.setHotelId(rs.getInt("hotel_id"));
                    hotel.setHotelName(rs.getString("hotel_name"));
                    hotel.setLocation(rs.getString("location"));
                    hotel.setContact(rs.getString("contact"));
                    hotel.setHDescription(rs.getString("h_description"));

                    hotelList.add(hotel);
                }
            }
        } catch (SQLException e) {
            System.err.println("호텔 검색 중 문제가 발생했습니다.");
            e.printStackTrace();
        }

        return hotelList;
    }

    /**
     특정 호텔의 상세(기본) 정보를 가져오는 메서드
     사용자가 리스트에서 특정 호텔을 선택했을 때 호출됨
     */
    public Hotel getHotelDetail(int hotelId) {
        Hotel hotel = null;
        String sql = "SELECT * FROM Hotel WHERE hotel_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    hotel = new Hotel();
                    hotel.setHotelId(rs.getInt("hotel_id"));
                    hotel.setHotelName(rs.getString("hotel_name"));
                    hotel.setLocation(rs.getString("location"));
                    hotel.setContact(rs.getString("contact"));
                    hotel.setHDescription(rs.getString("h_description"));
                }
            }
        } catch (SQLException e) {
            System.err.println("호텔 상세 정보 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }

        return hotel; // 해당하는 호텔이 없으면 null 반환
    }

    /**
     조건 없이 모든 호텔의 목록을 조회하는 메서드
     */
    public List<Hotel> getAllHotels() {
        List<Hotel> hotelList = new ArrayList<>();
        String sql = "SELECT * FROM Hotel";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            while (rs.next()) {
                Hotel hotel = new Hotel();
                hotel.setHotelId(rs.getInt("hotel_id"));
                hotel.setHotelName(rs.getString("hotel_name"));
                hotel.setLocation(rs.getString("location"));
                hotel.setContact(rs.getString("contact"));
                hotel.setHDescription(rs.getString("h_description"));

                hotelList.add(hotel);
            }
        } catch (SQLException e) {
            System.err.println("전체 호텔 목록 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }

        return hotelList;
    }
}