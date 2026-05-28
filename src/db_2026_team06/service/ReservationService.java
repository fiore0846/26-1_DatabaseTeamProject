package db_2026_team06.service;

import db_2026_team06.dao.ReservationDAO;
import db_2026_team06.model.Reservation;
import db_2026_team06.model.Room;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

public class ReservationService {
	
	private ReservationDAO reservationDAO = new ReservationDAO();

	public List<Room> getRoomList() throws Exception {
		return reservationDAO.showRoomInfo();
	}
	
	public boolean createReservation(int customerId, int roomNumber, int guests, LocalDate checkIn, LocalDate checkOut) throws Exception {
		// 1. 날짜 유효성 검사
		if (!checkIn.isBefore(checkOut)) {
			System.out.println("[오류] 체크아웃 날짜는 체크인 날짜 이후여야 합니다.");
			return false;
		}
		// 2. 예약 가능 여부 확인
		boolean available = reservationDAO.checkAvailability(roomNumber, checkIn, checkOut);
		if (!available) {
			System.out.println("[오류] 해당 날짜에 이미 예약이 있습니다.");
			return false;
		}
		// 3. 예약 생성
		LocalDate reservationDate = LocalDate.now();
		Random random = new Random();
		int reservation_id = random.nextInt(100);
		Reservation reservation = new Reservation(reservation_id, checkIn, checkOut, reservationDate, guests, roomNumber, customerId);
		return reservationDAO.createReservation(reservation);
	}
	
	public boolean cancelReservation(int reservationId) throws Exception {
		return reservationDAO.cancelReservation(reservationId);
	}
	
	public boolean checkAvailability(int roomId, LocalDate checkIn, LocalDate checkOut) throws Exception {
		return reservationDAO.checkAvailability(roomId, checkIn, checkOut);
	}
}
