package db_2026_team06.service;

import db_2026_team06.dao.ReservationDAO;
import db_2026_team06.model.Customer;
import db_2026_team06.model.Reservation;
import db_2026_team06.model.Room;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {
	// ReservationDAO 객체 생성하여 해당 클래스의 함수 사용
	private ReservationDAO reservationDAO = new ReservationDAO();

	public List<Room> getAvailableRooms(int targetHotelId, LocalDate checkIn, LocalDate checkOut, int guests) throws Exception {
		return reservationDAO.showRoomInfo(targetHotelId, checkIn, checkOut, guests);
	}
	
	public String validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null)        return "날짜를 입력해주세요.";
        if (!checkIn.isAfter(LocalDate.now().minusDays(1))) return "체크인은 오늘 이후여야 합니다.";
        if (!checkOut.isAfter(checkIn))                 return "체크아웃은 체크인 이후여야 합니다.";
        return null; // 유효
    }
	
	public int createReservation(Customer cs, int roomNumber, LocalDate checkIn, LocalDate checkOut, int guests) throws Exception {
		// 날짜 유효성 검사
		if (!checkIn.isBefore(checkOut)) {
			return -1;
		}
		
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false); //트랜잭션 시작
		try {
			// 예약 가능 여부 확인
			boolean available = reservationDAO.checkAvailability(roomNumber, checkIn, checkOut);
			if (!available) {
				conn.rollback(); // 에러 발생 시 롤백
				return -2;
			}
			// 예약 생성
			LocalDate reservationDate = LocalDate.now();
			Reservation reservation = new Reservation(0, checkIn, checkOut, reservationDate, guests, roomNumber, cs.getCustomerId());
			int result = reservationDAO.createReservation(reservation);
			conn.commit(); // 정상적으로 예약 생성 시 커밋
			return result;
		} catch (Exception e) {
			conn.rollback(); // 에러 발생 시 롤백
	        throw e;
		} finally {
			conn.setAutoCommit(true); //트랜잭션 끝
		}
	}
	
	public String[] getReservationDetail(int customerId) throws Exception {
		return reservationDAO.viewReservation(customerId);
	}
	
	public int calcTotalPrice(int pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        return (int)(nights * pricePerNight);
    }
	
	public boolean cancelReservation(int reservationId) throws Exception {
		return reservationDAO.cancelReservation(reservationId);
	}
}
