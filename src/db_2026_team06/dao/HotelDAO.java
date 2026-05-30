package db_2026_team06.dao;

import db_2026_team06.model.Attraction;
import db_2026_team06.model.Hotel;
import db_2026_team06.model.Room;
import db_2026_team06.model.Review;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Hotel, Room, Review, Attraction 테이블에 대한 데이터 접근 객체(DAO)
 *
 * 프로젝트 요구사항 대응:
 *  - 인덱스 활용 쿼리  : hotel_id 기반 조회 (idx_hotel_id)
 *  - 뷰 사용 쿼리     : hotel_avg_rating 뷰 사용
 *  - 조인 쿼리        : Hotel ↔ Room ↔ Hotel_Attraction ↔ Attraction 조인
 *  - 부속질의         : 평균 평점 이상 호텔 조회에 서브쿼리 사용
 *  - 동적 쿼리        : 사용자 입력 location 검색
 */
public class HotelDAO {

    // ────────────────────────────────────────────────────────────────────
    // 호텔 전체 목록 조회
    // 인덱스 활용: hotel_id PRIMARY KEY 인덱스 사용
    // ────────────────────────────────────────────────────────────────────

    /**
     * 전체 호텔 목록을 조회합니다.
     * @return Hotel 객체 리스트
     */
    public List<Hotel> findAllHotels() {
        List<Hotel> list = new ArrayList<>();
        // idx_hotel_id(hotel_id) 인덱스를 활용하는 ORDER BY
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

    // ────────────────────────────────────────────────────────────────────
    // 호텔 단건 조회 (인덱스 활용)
    // ────────────────────────────────────────────────────────────────────

    /**
     * hotel_id로 호텔 1건을 조회합니다.
     * hotel_id PRIMARY KEY 인덱스를 직접 활용합니다.
     * @param hotelId 조회할 호텔 ID
     * @return Hotel 객체, 없으면 null
     */
    public Hotel findHotelById(int hotelId) {
        // idx_hotel_id 인덱스 활용 쿼리
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

    // ────────────────────────────────────────────────────────────────────
    // 동적 쿼리: 사용자 입력 지역명으로 호텔 검색
    // ────────────────────────────────────────────────────────────────────

    /**
     * 사용자가 입력한 지역명으로 호텔을 검색합니다. (동적 쿼리)
     * 빈 문자열 입력 시 전체 목록을 반환합니다.
     * @param location 사용자 입력 지역명 (부분 일치)
     * @return 조건에 맞는 Hotel 리스트
     */
    public List<Hotel> searchHotelsByLocation(String location) {
        List<Hotel> list = new ArrayList<>();
        // 사용자 입력값을 반영하는 동적 쿼리 (요구사항 12번)
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

    // ────────────────────────────────────────────────────────────────────
    // 뷰 사용 쿼리: hotel_avg_rating 뷰로 평균 별점 조회
    // ────────────────────────────────────────────────────────────────────

    /**
     * hotel_avg_rating 뷰를 사용하여 특정 호텔의 평균 리뷰 별점을 조회합니다.
     * (요구사항 8번 - 뷰를 사용하는 쿼리)
     *
     * create.sql에서 다음 뷰가 정의되어야 합니다:
     *   CREATE VIEW hotel_avg_rating AS
     *   SELECT hotel_id, ROUND(AVG(rating), 1) AS avg_rating FROM Review GROUP BY hotel_id;
     *
     * @param hotelId 조회할 호텔 ID
     * @return 평균 별점 (리뷰 없으면 0.0)
     */
    public double findAvgRatingByHotelId(int hotelId) {
        // hotel_avg_rating 뷰 사용 (요구사항 8번)
        String sql = "SELECT avg_rating FROM hotel_avg_rating WHERE hotel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hotelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) return rs.getDouble("avg_rating");
            }
        } catch (SQLException e) {
            System.err.println("[오류] 평균 별점 조회 실패: " + e.getMessage());
        }
        return 0.0;
    }

    // ────────────────────────────────────────────────────────────────────
    // 부속질의: 전체 평균 이상 별점을 가진 호텔 조회
    // ────────────────────────────────────────────────────────────────────

    /**
     * 전체 평균 별점 이상인 호텔 목록을 조회합니다.
     * 서브쿼리로 전체 평균을 계산하여 비교합니다. (요구사항 10번 - 부속질의)
     * @return 평균 이상 별점 호텔 리스트
     */
    public List<Hotel> findHotelsAboveAvgRating() {
        List<Hotel> list = new ArrayList<>();
        // 부속질의(서브쿼리) 사용 (요구사항 10번)
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

    // ────────────────────────────────────────────────────────────────────
    // 조인 쿼리: 호텔별 룸 목록 조회
    // ────────────────────────────────────────────────────────────────────

    /**
     * 특정 호텔의 전체 룸 목록을 조회합니다.
     * Hotel ↔ Room JOIN 사용 (요구사항 11번)
     * @param hotelId 조회할 호텔 ID
     * @return Room 리스트
     */
    public List<Room> findRoomsByHotelId(int hotelId) {
        List<Room> list = new ArrayList<>();
        // Hotel과 Room 테이블 JOIN (요구사항 11번 - 조인 쿼리)
        // idx_room_hotel_id(hotel_id) 인덱스 활용
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
            System.err.println("[오류] 룸 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    // ────────────────────────────────────────────────────────────────────
    // 조인 쿼리: 호텔별 리뷰 목록 조회
    // ────────────────────────────────────────────────────────────────────

    /**
     * 특정 호텔의 전체 리뷰 목록을 조회합니다.
     * Review ↔ Hotel JOIN 사용 (요구사항 11번)
     * @param hotelId 조회할 호텔 ID
     * @return Review 리스트
     */
    public List<Review> findReviewsByHotelId(int hotelId) {
        List<Review> list = new ArrayList<>();
        // Review와 Hotel 조인, idx_review_hotel_id(hotel_id) 인덱스 활용 (요구사항 7, 11번)
        String sql = "SELECT rv.review_id, rv.rating, rv.review_date, rv.review, rv.hotel_id, rv.customer_id "
                   + "FROM Review rv "
                   + "INNER JOIN Hotel h ON rv.hotel_id = h.hotel_id "
                   + "WHERE rv.hotel_id = ? "
                   + "ORDER BY rv.review_date DESC";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hotelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    LocalDate date = rs.getDate("review_date") != null
                            ? rs.getDate("review_date").toLocalDate() : null;
                    list.add(new Review(
                        rs.getInt("review_id"),
                        rs.getInt("rating"),
                        date,
                        rs.getString("review"),
                        rs.getInt("hotel_id"),
                        rs.getInt("customer_id")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[오류] 리뷰 목록 조회 실패: " + e.getMessage());
        }
        return list;
    }

    // ────────────────────────────────────────────────────────────────────
    // 조인 쿼리: 호텔 주변 관광지 조회
    // ────────────────────────────────────────────────────────────────────

    /**
     * 특정 호텔 주변 관광지 목록을 조회합니다.
     * Hotel_Attraction ↔ Attraction 3-way JOIN (요구사항 11번)
     * @param hotelId 조회할 호텔 ID
     * @return Attraction 리스트
     */
    public List<Attraction> findAttractionsByHotelId(int hotelId) {
        List<Attraction> list = new ArrayList<>();
        // Hotel_Attraction 중간 테이블을 통한 3-way JOIN (요구사항 11번)
        String sql = "SELECT a.attraction_id, a.attraction_name, a.a_description "
                   + "FROM Attraction a "
                   + "INNER JOIN Hotel_Attraction ha ON a.attraction_id = ha.attraction_id "
                   + "INNER JOIN Hotel h ON ha.hotel_id = h.hotel_id "
                   + "WHERE ha.hotel_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, hotelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Attraction(
                        rs.getInt("attraction_id"),
                        rs.getString("attraction_name"),
                        rs.getString("a_description")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[오류] 관광지 조회 실패: " + e.getMessage());
        }
        return list;
    }

    // ────────────────────────────────────────────────────────────────────
    // 내부 헬퍼 메서드
    // ────────────────────────────────────────────────────────────────────

    /**
     * ResultSet 한 행을 Hotel 객체로 변환합니다.
     * @param rs 현재 커서 위치의 ResultSet
     * @return Hotel 객체
     */
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
