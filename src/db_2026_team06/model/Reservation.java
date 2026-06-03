package db_2026_team06.model;

import java.time.LocalDate;

public class Reservation {
	//변수 정의
	private int reservation_id;
	private LocalDate check_in;
	private LocalDate check_out;
	private LocalDate reservation_date;
	private int guests;
	private int room_number;
	private int customer_id;
	
	//생성자
	public Reservation() {}
	public Reservation(LocalDate check_in, LocalDate check_out,
			LocalDate reservation_date, int guests, int room_number, int customer_id) {
		this.check_in = check_in;
		this.check_out = check_out;
		this.reservation_date = reservation_date;
		this.guests = guests;
		this.room_number = room_number;
		this.customer_id = customer_id;
	}
	
	//getter, setter
	public int getReservationId()          { return reservation_id; }
    public void setReservationId(int id)   { this.reservation_id = id; }

    public LocalDate getCheckIn()               { return check_in; }
    public void setCheckIn(LocalDate checkIn)   { this.check_in = checkIn; }

    public LocalDate getCheckOut()              { return check_out; }
    public void setCheckOut(LocalDate checkOut) { this.check_out = checkOut; }

    public LocalDate getReservationDate()                    { return reservation_date; }
    public void setReservationDate(LocalDate reservationDate){ this.reservation_date = reservationDate; }

    public int getGuests()             { return guests; }
    public void setGuests(int guests)  { this.guests = guests; }

    public int getRoomNumber()               { return room_number; }
    public void setRoomNumber(int roomNumber){ this.room_number = roomNumber; }

    public int getCustomerId()               { return customer_id; }
    public void setCustomerId(int customerId){ this.customer_id = customerId; }
}
