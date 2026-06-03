package db_2026_team06.model;

import java.time.LocalDate;

/**
 * Reservation 테이블에 대응하는 DTO 클래스
 * 예약 정보를 담습니다.
 */
public class Reservation {
    private int       reservationId;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private LocalDate reservationDate;
    private int       guests;
    private int       roomNumber;
    private int       customerId;

    public Reservation() {}

    public Reservation(int reservationId, LocalDate checkIn, LocalDate checkOut,
                       LocalDate reservationDate, int guests, int roomNumber, int customerId) {
        this.reservationId   = reservationId;
        this.checkIn         = checkIn;
        this.checkOut        = checkOut;
        this.reservationDate = reservationDate;
        this.guests          = guests;
        this.roomNumber      = roomNumber;
        this.customerId      = customerId;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getReservationId()                  { return reservationId; }
    public void setReservationId(int v)            { this.reservationId = v; }

    public LocalDate getCheckIn()                  { return checkIn; }
    public void setCheckIn(LocalDate v)            { this.checkIn = v; }

    public LocalDate getCheckOut()                 { return checkOut; }
    public void setCheckOut(LocalDate v)           { this.checkOut = v; }

    public LocalDate getReservationDate()          { return reservationDate; }
    public void setReservationDate(LocalDate v)    { this.reservationDate = v; }

    public int getGuests()                         { return guests; }
    public void setGuests(int v)                   { this.guests = v; }

    public int getRoomNumber()                     { return roomNumber; }
    public void setRoomNumber(int v)               { this.roomNumber = v; }

    public int getCustomerId()                     { return customerId; }
    public void setCustomerId(int v)               { this.customerId = v; }
}
