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

	//특정 호텔의 리뷰 목록 조회
    public List<Review> getReviewsByHotelId(int hotelId) {
        List<Review> reviewList = new ArrayList<>();

        String sql = """
                SELECT
                    review_id,
                    rating,
                    review_date,
                    review,
                    hotel_id,
                    customer_id
                FROM Review
                WHERE hotel_id = ?
                ORDER BY review_date DESC, review_id DESC
                """;

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, hotelId);

            rs = pstmt.executeQuery();

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

        } catch (Exception e) {
            System.out.println("[오류] 리뷰 목록 조회 중 문제가 발생했습니다.");
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }

            } catch (Exception e) {
                System.out.println("[오류] 리뷰 조회 자원 해제 중 문제가 발생했습니다.");
                e.printStackTrace();
            }
        }

        return reviewList;
    }

    //특정 호텔의 평균 리뷰 별점 조회
    public double getAverageRatingByHotelId(int hotelId) {
        String sql = """
                SELECT
                    ROUND(COALESCE(AVG(rating), 0), 1) AS avg_rating
                FROM Review
                WHERE hotel_id = ?
                """;

        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            Connection conn = DBConnection.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, hotelId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getDouble("avg_rating");
            }

        } catch (Exception e) {
            System.out.println("[오류] 평균 리뷰 별점 조회 중 문제가 발생했습니다.");
            e.printStackTrace();

        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }

                if (pstmt != null) {
                    pstmt.close();
                }

            } catch (Exception e) {
                System.out.println("[오류] 평균 평점 조회 자원 해제 중 문제가 발생했습니다.");
                e.printStackTrace();
            }
        }

        return 0.0;
    }
}