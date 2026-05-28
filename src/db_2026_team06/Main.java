package db_2026_team06;

import db_2026_team06.menu.HotelDetailMenu;
import db_2026_team06.util.DBConnection;

public class Main {
    public static void main(String[] args) {
        HotelDetailMenu hotelDetailMenu = new HotelDetailMenu();

        // 테스트용 hotel_id
        hotelDetailMenu.showHotelDetailMenu(1);

        DBConnection.closeConnection();
    }
}