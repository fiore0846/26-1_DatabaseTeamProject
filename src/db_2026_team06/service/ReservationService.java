package db_2026_team06.service;

import db_2026_team06.dao.ReservationDAO;
import db_2026_team06.model.Reservation;
import db_2026_team06.model.Room;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {
	// ReservationDAO 객체 생성하여 해당 클래스의 함수 사용
	private ReservationDAO reservationDAO = new ReservationDAO();

	public List<Room> getRoomList() throws Exception {
		return reservationDAO.showRoomInfo(); // ReservationDAO의 showRoomInfo()에서 룸 객체 리스트 반환받아 ReservationMenu에서 출력하도록 연결
	}
	
	public boolean setReservation(String name, String phone, String email, int roomId, int guests, String checkIn, String checkOut) throws Exception {
		//예약 생성
		int customerId = getCustomerId(name, phone, email); // 고객 번호 변수로 저장
		LocalDate checkInDate = LocalDate.parse(checkIn);
		LocalDate checkOutDate = LocalDate.parse(checkOut);
		if (customerId != 0) {
			boolean result = createReservation(customerId, roomId, guests, checkInDate, checkOutDate); // 고객 있는지 확인 후 예약 생성
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
	
	public int getCustomerId(String name, String phone, String email) throws Exception {
		return reservationDAO.checkInCustomer(name, phone, email);
	}
	
	public boolean createReservation(int customerId, int roomNumber, int guests, LocalDate checkIn, LocalDate checkOut) throws Exception {
		// 날짜 유효성 검사
		if (!checkIn.isBefore(checkOut)) {
			System.out.println("[오류] 체크아웃 날짜는 체크인 날짜 이후여야 합니다.");
			return false;
		}
		
		Connection conn = DBConnection.getConnection();
		conn.setAutoCommit(false); //트랜잭션 시작
		try {
			// 예약 가능 여부 확인
			boolean available = reservationDAO.checkAvailability(roomNumber, checkIn, checkOut);
			if (!available) {
				System.out.println("[오류] 해당 날짜에 이미 예약이 있습니다.");
				conn.rollback(); // 에러 발생 시 롤백
				return false;
			}
			// 예약 생성
			LocalDate reservationDate = LocalDate.now();
			Reservation reservation = new Reservation(checkIn, checkOut, reservationDate, guests, roomNumber, customerId);
			boolean result = reservationDAO.createReservation(reservation);
			conn.commit(); // 정상적으로 예약 생성 시 커밋
			return result;
		} catch (Exception e) {
			conn.rollback(); // 에러 발생 시 롤백
	        throw e;
		} finally {
			conn.setAutoCommit(true); //트랜잭션 끝
		}
	}
	
	public boolean viewReservation(int customerId) throws Exception {
		return reservationDAO.viewReservation(customerId);
	}
	
	public boolean cancelReservation(int reservationId) throws Exception {
		return reservationDAO.cancelReservation(reservationId);
	}
}
