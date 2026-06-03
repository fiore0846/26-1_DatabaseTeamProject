package db_2026_team06.dao;

import db_2026_team06.model.Review;
import db_2026_team06.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

public class ReviewDAO {

    // 특정 호텔의 리뷰 목록 조회 (JOIN 포함 병합)
    public List<Review> getReviewsByHotelId(int hotelId) {
        List<Review> reviewList = new ArrayList<>();
        String sql = """
                SELECT rv.review_id, rv.rating, rv.review_date, rv.review, rv.hotel_id, rv.customer_id
                FROM Review rv
                INNER JOIN Hotel h ON rv.hotel_id = h.hotel_id
                WHERE rv.hotel_id = ?
                ORDER BY rv.review_date DESC, rv.review_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Date sqlDate = rs.getDate("review_date");
                    Review review = new Review(
                            rs.getInt("review_id"),
                            rs.getInt("rating"),
                            sqlDate != null ? sqlDate.toLocalDate() : null,
                            rs.getString("review"),
                            rs.getInt("hotel_id"),
                            rs.getInt("customer_id")
                    );
                    reviewList.add(review);
                }
            }
        } catch (Exception e) {
            System.out.println("[오류] 리뷰 목록 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }
        return reviewList;
    }

    // 특정 호텔의 평균 리뷰 별점 조회 (View 사용)
    public double getAverageRatingByHotelId(int hotelId) {
        String sql = "SELECT avg_rating FROM hotel_avg_rating WHERE hotel_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, hotelId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble("avg_rating");
                }
            }
        } catch (Exception e) {
            System.out.println("[오류] 평균 리뷰 별점 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }
        return 0.0;
    }
}