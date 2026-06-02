package db_2026_team06.dao;
//SQL을 직접 실행하는 클래스. DB에서 데이터를 가져오거나 저장함.

import db_2026_team06.model.Reservation;
import db_2026_team06.model.Room;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

	public List<Room> showRoomInfo() throws Exception {
		String sql = "SELECT room_number, type, price_per_night, capacity, hotel_id FROM vRoomInfo";
		
		List<Room> rooms = new ArrayList<>();
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			
			while (rs.next()) {
				Room room = new Room(
                    rs.getInt("room_number"),
                    rs.getString("type"),
                    rs.getInt("price_per_night"),
                    rs.getInt("capacity"),
                    rs.getInt("hotel_id")
                    );
				rooms.add(room);
			}
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
		}
		return rooms;
	}
	
	public int checkInCustomer(String name, String phone, String email) throws Exception {
		String sql = "SELECT customer_id FROM Customer WHERE name = ? AND phone = ? AND email = ?";
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setString(3, email);
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
	            return rs.getInt(1);
			}
			return 0;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return 0;
		}
	}
	
	// 예약 가능 여부 확인 (날짜 겹치는 예약이 있는지 체크)
	public boolean checkAvailability(int roomId, LocalDate checkIn, LocalDate checkOut) throws Exception {
		String sql = "SELECT COUNT(*) FROM Reservation "
				+ "WHERE room_number = ? AND (check_in <= ? AND check_out >= ?)";
		
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
	
	public int getReservationId() throws Exception {
		String sql = "SELECT COUNT(*) FROM Reservation";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			rs.next();
			int reservationId = rs.getInt(1);
			return (reservationId+1);
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return -1;
		}
	}
	
	// 예약 생성 (INSERT)
	public boolean createReservation(Reservation reservation) throws Exception {
		String sql = "INSERT INTO Reservation (check_in, check_out, reservation_date, guests, room_number, customer_id) VALUES (?, ?, ?, ?, ?, ?)";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setDate(1, Date.valueOf(reservation.getCheckIn()));
			pstmt.setDate(2, Date.valueOf(reservation.getCheckOut()));
			pstmt.setDate(3, Date.valueOf(reservation.getReservationDate()));
			pstmt.setInt(4, reservation.getGuests());
			pstmt.setInt(5, reservation.getRoomNumber());
			pstmt.setInt(6, reservation.getCustomerId());
			
			return pstmt.executeUpdate() > 0; //executeUpdate(): INSERT / DELETE / UPDATE 관련 구문에서는 반영된 레코드의 건수를 반환
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return false;
		}
	}
	
	//예약 확인
	public boolean viewReservation(int customerId) throws Exception {
		String sql = "SELECT reservation_id, room_number, reservation_date, guests, check_in, check_out "
				+ "FROM Reservation WHERE customer_id = ?";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			
			pstmt.setInt(1, customerId);
			ResultSet rs = pstmt.executeQuery();
			
			System.out.println("예약 번호 | 객실 번호 | 예약 일자 | 일행 수 | 체크인 날짜 | 체크아웃 날짜");
			boolean hasReservation = false;
	        
	        while (rs.next()) {
	            hasReservation = true;
	            
	            int reservationId = rs.getInt("reservation_id");
	            int roomNumber = rs.getInt("room_number");
	            String reservationDate = rs.getString("reservation_date");
	            int guests = rs.getInt("guests");
	            String checkIn = rs.getString("check_in");
	            String checkOut = rs.getString("check_out");
	            
	            // 형식에 맞춰 출력
	            System.out.printf("%d, %d, %s, %d, %s, %s\n", reservationId, roomNumber, reservationDate, guests, checkIn, checkOut);
	        }
	        
	        if (!hasReservation) {
	            System.out.println("해당 고객의 예약 내역이 존재하지 않습니다.");
	        }
			return true;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return false;
		}
	}
	
	// 예약 취소 (DELETE)
	public boolean cancelReservation(int reservationId) throws Exception {
		String sql = "DELETE FROM Reservation WHERE reservation_id = ?";
		
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
