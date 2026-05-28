package DB2026Team06.model;

public class Room {
	private int room_number;
	private String type;
	private int price_per_night;
	private int capacity;
	private int hotel_id;
	
	public Room() {}
	public Room(int room_number, String type,
			int price_per_night, int capacity, int hotel_id) {
		this.room_number = room_number;
		this.type = type;
		this.price_per_night = price_per_night;
		this.capacity = capacity;
		this.hotel_id = hotel_id;
	}
	
	// getter, setter
	public int getRoomNumber()                  { return room_number; }
	public void setRoomNumber(int roomNumber)   { this.room_number = roomNumber; }
	
	public String getType()                 { return type; }
	public void setType(String type)    { this.type = type; }
	
	public int getPricePerNight()                    { return price_per_night; }
	public void setPricePerNight(int pricePerNight)  { this.price_per_night = pricePerNight; }
	
	public int getCapacity()               { return capacity; }
	public void setCapacity(int capacity)  { this.capacity = capacity; }
	
	public int getHotelId()              { return hotel_id; }
	public void setHotelId(int hotelId)  { this.hotel_id = hotelId; }
}