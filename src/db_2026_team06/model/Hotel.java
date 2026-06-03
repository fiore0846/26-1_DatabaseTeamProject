package db_2026_team06.model;

public class Hotel {
    private int hotelId;
    private String hotelName;
    private String location;
    private String contact;
    private String hDescription;

    // 기본 생성자
    public Hotel() {}

    // Getter & Setter
    public int getHotelId() { return hotelId; }
    public void setHotelId(int hotelId) { this.hotelId = hotelId; }

    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getHDescription() { return hDescription; }
    public void setHDescription(String hDescription) { this.hDescription = hDescription; }
}