package db_2026_team06.model;

/**
 * Hotel 테이블에 대응하는 DTO 클래스
 * 호텔의 기본 정보를 담습니다.
 */
public class Hotel {
    private int hotelId;
    private String hotelName;
    private String location;
    private String contact;
    private String hDescription;

    // 지도 패널 시각화를 위한 좌표 (DB 컬럼 없이 프로그램 내 고정값으로 관리)
    private int mapX;
    private int mapY;

    public Hotel() {}

    public Hotel(int hotelId, String hotelName, String location, String contact, String hDescription) {
        this.hotelId    = hotelId;
        this.hotelName  = hotelName;
        this.location   = location;
        this.contact    = contact;
        this.hDescription = hDescription;
    }

    // ── Getters & Setters ──────────────────────────────────────────────

    public int getHotelId()            { return hotelId; }
    public void setHotelId(int v)      { this.hotelId = v; }

    public String getHotelName()             { return hotelName; }
    public void setHotelName(String v)       { this.hotelName = v; }

    public String getLocation()              { return location; }
    public void setLocation(String v)        { this.location = v; }

    public String getContact()               { return contact; }
    public void setContact(String v)         { this.contact = v; }

    public String getHDescription()          { return hDescription; }
    public void setHDescription(String v)    { this.hDescription = v; }

    public int getMapX()                     { return mapX; }
    public void setMapX(int v)               { this.mapX = v; }

    public int getMapY()                     { return mapY; }
    public void setMapY(int v)               { this.mapY = v; }

    @Override
    public String toString() {
        return hotelName + " (" + location + ")";
    }
}
