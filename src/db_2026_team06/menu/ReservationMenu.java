package db_2026_team06.menu;

import db_2026_team06.service.ReservationService;
import db_2026_team06.model.Room;

import java.util.List;
import java.util.Scanner;

public class ReservationMenu {
	
	private ReservationService reservationService = new ReservationService();

    private Scanner sc = new Scanner(System.in);

    public void showMenu() {

        while (true) {

            System.out.println("\n===== 예약 메뉴 =====");
            System.out.println("1. 예약하기");
            System.out.println("2. 예약 조회");
            System.out.println("3. 예약 취소");
            System.out.println("0. 뒤로가기");
            System.out.print("선택 : ");

            int choice = sc.nextInt();

            switch (choice) {

            case 1:
                reserveRoom();
                break;

            case 2:
                viewReservation();
                break;

            case 3:
                cancelReservation();
                break;

            case 0:
                System.out.println("예약 메뉴를 종료합니다.");
                return;

            default:
                System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private void reserveRoom() {
    	// 예약하기

        sc.nextLine();

        System.out.println("\n===== 예약하기 =====");

        System.out.print("고객명 입력 : ");
        String name = sc.nextLine().trim();

        System.out.print("전화번호 입력 : ");
        String phone = sc.nextLine().trim();

        System.out.print("이메일 입력 : ");
        String email = sc.nextLine().trim();

        System.out.print("체크인 날짜 입력 (YYYY-MM-DD) : ");
        String checkIn = sc.nextLine();

        System.out.print("체크아웃 날짜 입력 (YYYY-MM-DD) : ");
        String checkOut = sc.nextLine();
        
        // vRoomInfo 뷰를 통해 객실 목록 출력
        try {
        	List<Room> rooms = reservationService.getRoomList();
            if (rooms.isEmpty()) {
                System.out.println("[안내] 등록된 객실이 없습니다.");
                return;
            }
            System.out.println("\n─────────────────────────────────────────────────");
            System.out.printf("%-10s %-12s %-12s %-8s%n", "객실번호", "타입", "1박 요금(원)", "정원");
            System.out.println("─────────────────────────────────────────────────");
            for (Room room : rooms) {
                System.out.printf("%-10d %-12s %-16d %-8d%n",
                    room.getRoomNumber(),
                    room.getType(),
                    room.getPricePerNight(),
                    room.getCapacity());
            }
        } catch (Exception e) {
        	System.out.print(e);
        }
        
        System.out.print("일행 수 입력 : ");
        int guests = sc.nextInt();
        
        System.out.print("객실 번호 입력 : ");
        int roomId = sc.nextInt();

        sc.nextLine();
        // 예약 생성
        try {
        	reservationService.setReservation(name, phone, email, roomId, guests, checkIn, checkOut);
        	//예약 정보 출력
        	int customerId = reservationService.getCustomerId(name, phone, email); // ReservationService에 있는 함수 이용하여 고객 번호 반환받음.
        	System.out.println("\n===== 예약 정보 =====");
    		System.out.println("고객번호 : " + customerId);
            System.out.println("고객명 : " + name);
            System.out.println("전화번호 : " + phone);
            System.out.println("이메일 : " + email);
            System.out.println("일행 수 : " + guests);
            System.out.println("객실번호 : " + roomId);
            System.out.println("체크인 : " + checkIn);
            System.out.println("체크아웃 : " + checkOut);
        } catch (Exception e) {
        	System.out.print(e);
        }
        
    }
    
    private void viewReservation() {
    	// 예약 조회
        System.out.println("\n===== 예약 조회 =====");

        System.out.print("고객 ID 입력 : ");
        int customerId = sc.nextInt();

        System.out.println(customerId + "번 고객 예약 조회");
        // 고객 번호로 조회하여 예약 내역 출력
        try {
        	reservationService.viewReservation(customerId);
        } catch (Exception e) {
        	System.out.print(e);
        }
    }
    
    private void cancelReservation() {
    	// 예약 취소
        System.out.println("\n===== 예약 취소 =====");

        System.out.print("예약번호 입력 : ");
        int reservationId = sc.nextInt();
        // 예약 번호로 조회하여 예약 취소
        System.out.println(reservationId + "번 예약 취소");
        try {
        	reservationService.cancelReservation(reservationId);
        } catch (Exception e) {
        	System.out.print(e);
        }
    }
}