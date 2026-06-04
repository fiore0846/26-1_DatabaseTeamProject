package db_2026_team06.dao;

import db_2026_team06.model.Hotel;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotel 테이블에 대한 데이터 접근 객체(DAO)
 *
 * 프로젝트 요구사항 대응:
 * - 인덱스 활용  (요구사항 7번)  : searchHotelsByLocation() 등 - location 컬럼 등에 생성된 인덱스를 활용하여 검색 최적화
 * - 부속질의     (요구사항 10번) : findHotelsAboveAvgRating() - 전체 리뷰의 평균 평점 이상을 계산하기 위해 WHERE 절과 HAVING 절에 중첩 서브쿼리(Subquery) 활용
 * - 동적 쿼리    (요구사항 12번) : searchHotelsByLocation(), findHotelById() - 사용자로부터 입력받은 값을 PreparedStatement 매개변수(?)로 바인딩하여 동적으로 쿼리 생성
 * - Select       (요구사항 17번) : findAllHotels(), findHotelById(), searchHotelsByLocation(), findHotelsAboveAvgRating() 등 전체 기능
 */
public class HotelDAO {

    /**
     * 전체 호텔 목록을 조회합니다.
     */
    public List<Hotel> findAllHotels() {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT hotel_id, hotel_name, location, contact, h_description "
                + "FROM Hotel ORDER BY hotel_id";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) {
                list.add(mapHotel(rs));
            }
        } catch (SQLException e) {
            System.err.println("[오류] 호텔 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * hotel_id로 호텔 1건을 조회합니다.
     */
    public Hotel findHotelById(int hotelId) {
        String sql = "SELECT hotel_id, hotel_name, location, contact, h_description "
                + "FROM Hotel WHERE hotel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hotelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return mapHotel(rs);
            }
        } catch (SQLException e) {
            System.err.println("[오류] 호텔 단건 조회 실패: " + e.getMessage());
        }
        return null;
    }

    /**
     * 사용자가 입력한 지역명으로 호텔을 검색합니다. (동적 쿼리)
     */
    public List<Hotel> searchHotelsByLocation(String location) {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT hotel_id, hotel_name, location, contact, h_description "
                + "FROM Hotel WHERE location LIKE ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "%" + location + "%");
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) list.add(mapHotel(rs));
            }
        } catch (SQLException e) {
            System.err.println("[오류] 호텔 검색 실패: " + e.getMessage());
        }
        return list;
    }

    /**
     * 전체 평균 별점 이상인 호텔 목록을 조회합니다. (부속질의 요구사항 충족)
     */
    public List<Hotel> findHotelsAboveAvgRating() {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT h.hotel_id, h.hotel_name, h.location, h.contact, h.h_description "
                + "FROM Hotel h "
                + "WHERE h.hotel_id IN ( "
                + "  SELECT r.hotel_id FROM Review r "
                + "  GROUP BY r.hotel_id "
                + "  HAVING AVG(r.rating) >= (SELECT AVG(rating) FROM Review) "
                + ")";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            while (rs.next()) list.add(mapHotel(rs));
        } catch (SQLException e) {
            System.err.println("[오류] 우수 호텔 조회 실패: " + e.getMessage());
        }
        return list;
    }

    private Hotel mapHotel(ResultSet rs) throws SQLException {
        return new Hotel(
                rs.getInt("hotel_id"),
                rs.getString("hotel_name"),
                rs.getString("location"),
                rs.getString("contact"),
                rs.getString("h_description")
        );
    }
}