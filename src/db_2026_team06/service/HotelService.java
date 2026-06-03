package db_2026_team06.service;

import db_2026_team06.dao.HotelDAO;
import db_2026_team06.dao.RoomDAO;
import db_2026_team06.dao.ReviewDAO;
import db_2026_team06.dao.AttractionDAO;

import db_2026_team06.model.Hotel;
import db_2026_team06.model.Room;
import db_2026_team06.model.Review;
import db_2026_team06.model.Attraction;

import java.util.List;

public class HotelService {
    private HotelDAO hotelDAO = new HotelDAO();
    private RoomDAO roomDAO = new RoomDAO();
    private ReviewDAO reviewDAO = new ReviewDAO();
    private AttractionDAO attractionDAO = new AttractionDAO();

    public Hotel getHotelDetail(int hotelId) {
        return hotelDAO.getHotelDetail(hotelId);
    }

    public List<Room> getRoomsByHotelId(int hotelId) {
        return roomDAO.getRoomsByHotelId(hotelId);
    }

    public Room getRoomDetail(int roomNumber) {
        return roomDAO.getRoomDetail(roomNumber);
    }

    public List<Review> getReviewsByHotelId(int hotelId) {
        return reviewDAO.getReviewsByHotelId(hotelId);
    }

    public double getAverageRatingByHotelId(int hotelId) {
        return reviewDAO.getAverageRatingByHotelId(hotelId);
    }

    public List<Attraction> getNearbyAttractions(int hotelId) {return attractionDAO.getNearbyAttractions(hotelId); }
}