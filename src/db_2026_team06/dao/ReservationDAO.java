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
	public List<Room> showRoomInfo(int targetHotelId, LocalDate checkIn, LocalDate checkOut, int guests) throws Exception {
		String sql = "SELECT room_number, type, price_per_night, capacity, hotel_id FROM vRoomInfo WHERE hotel_id = ? AND capacity >= ?";
		
		List<Room> rooms = new ArrayList<>();
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, targetHotelId);		
			pstmt.setInt(2, guests);
			
			ResultSet rs = pstmt.executeQuery();
			// rs 다음 객체 없을 때까지 룸 정보 검색하여 리스트에 추가
			while (rs.next()) {
				int room_number = rs.getInt("room_number");
				if (checkAvailability(room_number, checkIn, checkOut)) {
					Room room = new Room(
		                    room_number,
		                    rs.getString("type"),
		                    rs.getInt("price_per_night"),
		                    rs.getInt("capacity"),
		                    rs.getInt("hotel_id")
		                    );
					rooms.add(room);
				}
			}
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
		}
		return rooms;
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
	
	public int createReservation(Reservation reservation) throws Exception {
		// 예약 생성 (INSERT)
		String sql1 = "INSERT INTO Reservation (check_in, check_out, reservation_date, guests, room_number, customer_id) VALUES (?, ?, ?, ?, ?, ?)";
		String sql2 = "SELECT reservation_id FROM Reservation WHERE customer_id = ? AND room_number = ? AND reservation_date = ?";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt1 = conn.prepareStatement(sql1);
			// sql문에 입력값 넣어 동적으로 조회
			int customerId = reservation.getCustomerId();
			int roomNumber = reservation.getRoomNumber();
			Date reservationDate = Date.valueOf(reservation.getReservationDate());
			pstmt1.setDate(1, Date.valueOf(reservation.getCheckIn()));
			pstmt1.setDate(2, Date.valueOf(reservation.getCheckOut()));
			pstmt1.setDate(3, reservationDate);
			pstmt1.setInt(4, reservation.getGuests());
			pstmt1.setInt(5, roomNumber);
			pstmt1.setInt(6, customerId);
			
			PreparedStatement pstmt2 = conn.prepareStatement(sql2);
			pstmt2.setInt(1, customerId);
			pstmt2.setInt(2, roomNumber);
			pstmt2.setDate(3, reservationDate);
			ResultSet rs = pstmt2.executeQuery();
			int reservationId = rs.getInt(0);
			return reservationId;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return -1;
		}
	}
	
	public String[] viewReservation(int customerId) throws Exception {
		//예약 확인
		String sql = "SELECT customerId, reservationId, customer_name, room_number, room_type, price_per_night, guests, check_in, check_out, reservation_date"
				+ " FROM vReservationDetail WHERE customerId = ?";
		
		try {
			Connection conn = DBConnection.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, customerId);
			ResultSet rs = pstmt.executeQuery();
			
			boolean hasReservation = false;
	        
	        while (rs.next()) {
	        	// rs.next()가 true이면 예약 정보 있는 것
	            hasReservation = true;
	            
	            
	            // 형식에 맞춰 출력
	            return new String[]{
                        rs.getString("hotel_name"), // 수정 필요
                        rs.getString("room_type"),
                        rs.getDate  ("check_in").toString(),
                        rs.getDate  ("check_out").toString(),
                        String.valueOf(rs.getInt("guests")),
                        String.format("%,d원", rs.getInt("total_price")), // 수정 필요
                        rs.getString("customer_name")
                    };
	        }
	        
	        if (!hasReservation) {
	            System.out.println("해당 고객의 예약 내역이 존재하지 않습니다.");
	        }
			return null;
		} catch (SQLException e) {
			System.out.println("DB연결 실패하거나, SQL문이 틀렸습니다.");
			System.out.print("사유 : " + e.getMessage());
			return null;
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
