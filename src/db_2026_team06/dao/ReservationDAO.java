package db_2026_team06.dao;
//SQL을 직접 실행하는 클래스. DB에서 데이터를 가져오거나 저장함.

import DB2026Team06.model.Reservation;
import DB2026Team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
	
	// 예약 가능 여부 확인 (날짜 겹치는 예약이 있는지 체크)
	public boolean checkAvailability(int roomId, LocalDate checkIn, LocalDate checkOut) throws Exception {
		String sql = "CREATE VIEW vReservation AS SELECT type, price_per_night, capacity FROM Room "
				+ "WHERE room_number = (SELECT room_number FROM Reservation "
				+ "WHERE room_id = ? AND (check_in <= ? OR check_out >= ?))";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, roomId);
			pstmt.setDate(2, Date.valueOf(checkOut));
			pstmt.setDate(3, Date.valueOf(checkIn));
			
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			return rs.getInt(1) == 0; //0이면 예약 가능 -> true 반환
		} catch (SQLException e){
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return false;
		}
	}
	
	// 예약 생성 (INSERT)
	public boolean createReservation(Reservation reservation) throws Exception {
		String sql = "INSERT INTO Reservation (customer_id, room_number, check_in, check_out) VALUES (?, ?, ?, ?, ?)";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, reservation.getCustomerId());
			pstmt.setInt(2, reservation.getRoomNumber());
			pstmt.setDate(3, Date.valueOf(reservation.getCheckIn()));
			pstmt.setDate(4, Date.valueOf(reservation.getCheckOut()));
			
			return pstmt.executeUpdate() > 0; //executeUpdate(): INSERT / DELETE / UPDATE 관련 구문에서는 반영된 레코드의 건수를 반환
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return false;
		}
	}
	
	// 예약 취소 (UPDATE)
	public boolean cancelReservation(int reservationId) throws Exception {
		String sql = "UPDATE Reservation SET status = 'cancelled' WHERE reservation_id = ?";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, reservationId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return false;
		}
	}
}
