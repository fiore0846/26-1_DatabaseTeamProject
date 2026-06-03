package db_2026_team06.service;

import db_2026_team06.dao.HotelDAO;
import db_2026_team06.model.Attraction;
import db_2026_team06.model.Hotel;
import db_2026_team06.model.Room;
import db_2026_team06.model.Review;

import java.util.List;

/**
 * 호텔 탐색 화면과 관련된 비즈니스 로직을 처리하는 서비스 클래스
 * DAO를 통해 데이터를 가져오고, 화면(Menu)에 필요한 형태로 가공합니다.
 */
public class HotelService {

    private final HotelDAO hotelDAO;

    public HotelService() {
        this.hotelDAO = new HotelDAO();
    }

    // ────────────────────────────────────────────────────────────────────
    // 호텔 목록 관련
    // ────────────────────────────────────────────────────────────────────

    /**
     * 전체 호텔 목록을 반환합니다.
     * 지도 패널의 초기 마커 표시에 사용됩니다.
     * @return Hotel 리스트
     */
    public List<Hotel> getAllHotels() {
        return hotelDAO.findAllHotels();
    }

    /**
     * 사용자가 입력한 지역명으로 호텔을 검색합니다. (동적 쿼리)
     * 빈 문자열이면 전체 목록을 반환합니다.
     * @param location 사용자 입력 지역명
     * @return 검색 결과 Hotel 리스트
     */
    public List<Hotel> searchHotelsByLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return hotelDAO.findAllHotels();
        }
        return hotelDAO.searchHotelsByLocation(location.trim());
    }

    /**
     * 전체 평균 별점 이상의 우수 호텔 목록을 반환합니다. (부속질의 활용)
     * @return 우수 호텔 리스트
     */
    public List<Hotel> getTopRatedHotels() {
        return hotelDAO.findHotelsAboveAvgRating();
    }

    // ────────────────────────────────────────────────────────────────────
    // 호텔 세부 정보 관련
    // ────────────────────────────────────────────────────────────────────

    /**
     * 특정 호텔의 기본 정보를 반환합니다.
     * @param hotelId 조회할 호텔 ID
     * @return Hotel 객체, 없으면 null
     */
    public Hotel getHotelById(int hotelId) {
        return hotelDAO.findHotelById(hotelId);
    }

    /**
     * 특정 호텔의 평균 별점을 반환합니다. (뷰 활용)
     * 소수점 첫째 자리까지 반올림된 값입니다.
     * @param hotelId 조회할 호텔 ID
     * @return 평균 별점 (0.0 ~ 5.0)
     */
    public double getAvgRating(int hotelId) {
        return hotelDAO.findAvgRatingByHotelId(hotelId);
    }

    /**
     * 특정 호텔의 객실 목록을 반환합니다. (조인 쿼리 활용)
     * @param hotelId 조회할 호텔 ID
     * @return Room 리스트
     */
    public List<Room> getRoomsByHotelId(int hotelId) {
        return hotelDAO.findRoomsByHotelId(hotelId);
    }

    /**
     * 특정 호텔의 리뷰 목록을 반환합니다. (조인 쿼리 활용)
     * @param hotelId 조회할 호텔 ID
     * @return Review 리스트
     */
    public List<Review> getReviewsByHotelId(int hotelId) {
        return hotelDAO.findReviewsByHotelId(hotelId);
    }

    /**
     * 특정 호텔의 주변 관광지 목록을 반환합니다. (3-way 조인 활용)
     * @param hotelId 조회할 호텔 ID
     * @return Attraction 리스트
     */
    public List<Attraction> getAttractionsByHotelId(int hotelId) {
        return hotelDAO.findAttractionsByHotelId(hotelId);
    }

    /**
     * 별점(1~5)을 ★ 기호 문자열로 변환합니다.
     * 화면에서 별점을 시각적으로 표시할 때 사용합니다.
     * @param rating 정수 별점 (1~5)
     * @return "★★★☆☆" 형식의 문자열
     */
    public String formatStars(int rating) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            sb.append(i <= rating ? "★" : "☆");
        }
        return sb.toString();
    }
}