package db_2026_team06.dao;

import db_2026_team06.model.Reservation;
import db_2026_team06.model.Room;
import db_2026_team06.util.DBConnection;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
	// 룸 정보 출력하기 위해 룸 객체 리스트 생성
	public List<Room> showRoomInfo() throws Exception {
		String sql = "SELECT room_number, type, price_per_night, capacity, hotel_id FROM vRoomInfo";
		
		List<Room> rooms = new ArrayList<>();
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			ResultSet rs = pstmt.executeQuery();
			// rs 다음 객체 없을 때까지 룸 정보 검색하여 리스트에 추가
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
		// 고객 정보 있는지 확인하고 고객 번호(customer_id) 반환
		String sql = "SELECT customer_id FROM Customer WHERE name = ? AND phone = ? AND email = ?";
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, name);
			pstmt.setString(2, phone);
			pstmt.setString(3, email);
			
			ResultSet rs = pstmt.executeQuery();
			if (rs.next()) {
	            return rs.getInt(1); // SELECT문으로 조회한 customer_id 반환
			}
			return 0;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return 0;
		}
	}
	
	public boolean checkAvailability(int roomId, LocalDate checkIn, LocalDate checkOut) throws Exception {
		// 예약 가능 여부 확인 (날짜 겹치는 예약이 있는지 체크)
		String sql = "SELECT COUNT(*) FROM Reservation "
				+ "WHERE room_number = ? AND (check_in <= ? AND check_out >= ?)";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			// sql문에 입력값 넣어 동적으로 조회
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
	
	public boolean createReservation(Reservation reservation) throws Exception {
		// 예약 생성 (INSERT)
		String sql = "INSERT INTO Reservation (check_in, check_out, reservation_date, guests, room_number, customer_id) VALUES (?, ?, ?, ?, ?, ?)";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			// sql문에 입력값 넣어 동적으로 조회
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
	
	public boolean viewReservation(int customerId) throws Exception {
		//예약 확인
		String sql = "SELECT customerId, reservationId, customer_name, room_number, room_type, price_per_night, guests, check_in, check_out, reservation_date"
				+ " FROM vReservationDetail WHERE customerId = ?";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, customerId);
			ResultSet rs = pstmt.executeQuery();
			
			System.out.println("예약 번호 | 고객명 | 객실 번호 | 객실 유형 | 1박 당 가격 | 일행 수 | 체크인 날짜 | 체크아웃 날짜 | 예약일");
			boolean hasReservation = false;
	        
	        while (rs.next()) {
	        	// rs.next()가 true이면 예약 정보 있는 것
	            hasReservation = true;
	            
	            int reservationId = rs.getInt("reservationId");
	            String name = rs.getString("customer_name");
	            int roomNumber = rs.getInt("room_number");
	            String roomType = rs.getString("room_type");
	            int pricePerNight = rs.getInt("price_per_night");
	            int guests = rs.getInt("guests");
	            String checkIn = rs.getString("check_in");
	            String checkOut = rs.getString("check_out");
	            String reservationDate = rs.getString("reservation_date");
	            
	            // 형식에 맞춰 출력
	            System.out.printf("%d, %s, %d, %s, %d, %d, %s, %s, %s\n", reservationId, name, roomNumber, roomType, pricePerNight, guests, checkIn, checkOut, reservationDate);
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
	
	public boolean cancelReservation(int reservationId) throws Exception {
		// 예약 취소 (DELETE)
		String sql = "DELETE FROM Reservation WHERE reservation_id = ?";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			// sql문에 입력값 넣어 동적으로 조회
			pstmt.setInt(1, reservationId);
			return pstmt.executeUpdate() > 0;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return false;
		}
	}
}
