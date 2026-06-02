package db_2026_team06.service;

import db_2026_team06.dao.ReservationDAO;
import db_2026_team06.model.Reservation;
import db_2026_team06.model.Room;

import java.time.LocalDate;
import java.util.List;

public class ReservationService {
	
	private ReservationDAO reservationDAO = new ReservationDAO();

	public List<Room> getRoomList() throws Exception {
		return reservationDAO.showRoomInfo();
	}
	
	public boolean setReservation(String name, String phone, String email, int roomId, int guests, String checkIn, String checkOut) throws Exception {
		int customerId = reservationDAO.checkInCustomer(name, phone, email);
		System.out.println(customerId); //확인 후 삭제
		LocalDate checkInDate = LocalDate.parse(checkIn);
		LocalDate checkOutDate = LocalDate.parse(checkOut);
		if (customerId != 0) {
			boolean result = createReservation(customerId, roomId, guests, checkInDate, checkOutDate);
			if (result) {
				System.out.println("예약이 완료되었습니다.");
			} else {
				System.out.println("예약에 실패하였습니다.");
			}
			return result;
		} else {
			System.out.println("고객 정보를 찾을 수 없습니다. 이름을 다시 확인해주세요.");
			return false;
		}
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
		int reservation_id = reservationDAO.getReservationId();
		Reservation reservation = new Reservation(reservation_id, checkIn, checkOut, reservationDate, guests, roomNumber, customerId);
		return reservationDAO.createReservation(reservation);
	}
	
	public boolean viewReservation(int customerId) throws Exception {
		return reservationDAO.viewReservation(customerId);
	}
	
	public boolean cancelReservation(int reservationId) throws Exception {
		return reservationDAO.cancelReservation(reservationId);
	}
}
