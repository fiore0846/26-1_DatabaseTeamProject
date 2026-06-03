package db_2026_team06.dao;

import db_2026_team06.model.Attraction;
import db_2026_team06.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AttractionDAO {

    /**
     특정 호텔 주변의 관광지 정보를 가져오는 메서드
     */
    public List<Attraction> getNearbyAttractions(int hotelId) {
        List<Attraction> attractionList = new ArrayList<>();

        // 다대다 관계 처리를 위한 JOIN 쿼리
        String sql = "SELECT a.attraction_id, a.attraction_name, a.a_description "
                + "FROM Attraction a "
                + "JOIN Hotel_Attraction ha ON a.attraction_id = ha.attraction_id "
                + "WHERE ha.hotel_id = ?";

        // try-with-resources 적용
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            // ? 매개변수에 hotelId 세팅
            pstmt.setInt(1, hotelId);

            try (ResultSet rs = pstmt.executeQuery()) {
                // 커서를 다음 행으로 이동하며 데이터 추출
                while (rs.next()) {
                    Attraction attraction = new Attraction();
                    // ResultSet 컬럼명으로 데이터 매핑
                    attraction.setAttractionId(rs.getInt("attraction_id"));
                    attraction.setAttractionName(rs.getString("attraction_name"));
                    attraction.setADescription(rs.getString("a_description"));

                    attractionList.add(attraction);
                }
            }
        } catch (SQLException e) {
            System.err.println("관광지 정보 조회 중 문제가 발생했습니다.");
            e.printStackTrace();
        }

        return attractionList;
    }
}