package db_2026_team06.model;

/**
 * Room 테이블에 대응하는 DTO 클래스
 * 호텔 객실의 유형, 가격, 수용 인원 정보를 담습니다.
 */
public class Room {
    private int roomNumber;
    private String type;
    private int pricePerNight;
    private int capacity;
    private int hotelId;

    public Room() {}

    public Room(int roomNumber, String type, int pricePerNight, int capacity, int hotelId) {
        this.roomNumber     = roomNumber;
        this.type           = type;
        this.pricePerNight  = pricePerNight;
        this.capacity       = capacity;
        this.hotelId        = hotelId;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getRoomNumber()              { return roomNumber; }
    public void setRoomNumber(int v)        { this.roomNumber = v; }

    public String getType()                 { return type; }
    public void setType(String v)           { this.type = v; }

    public int getPricePerNight()           { return pricePerNight; }
    public void setPricePerNight(int v)     { this.pricePerNight = v; }

    public int getCapacity()                { return capacity; }
    public void setCapacity(int v)          { this.capacity = v; }

    public int getHotelId()                 { return hotelId; }
    public void setHotelId(int v)           { this.hotelId = v; }
}
