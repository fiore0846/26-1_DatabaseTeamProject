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
     * 특정 호텔 주변의 관광지 정보를 가져오는 메서드
     * (요구사항 11번 조인 충족을 위해 3-way JOIN 쿼리 병합)
     */
    public List<Attraction> getNearbyAttractions(int hotelId) {
        List<Attraction> attractionList = new ArrayList<>();

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
                    Attraction attraction = new Attraction();
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