package db_2026_team06.menu;

import java.util.Scanner;

public class ReservationMenu {

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

        sc.nextLine();

        System.out.println("\n===== 예약하기 =====");

        System.out.print("고객명 입력 : ");
        String name = sc.nextLine();

        System.out.print("전화번호 입력 : ");
        String phone = sc.nextLine();

        System.out.print("이메일 입력 : ");
        String email = sc.nextLine();

        System.out.print("객실 번호 입력 : ");
        int roomId = sc.nextInt();

        sc.nextLine();

        System.out.print("체크인 날짜 입력 (YYYY-MM-DD) : ");
        String checkIn = sc.nextLine();

        System.out.print("체크아웃 날짜 입력 (YYYY-MM-DD) : ");
        String checkOut = sc.nextLine();

        System.out.println("\n===== 입력 정보 =====");
        System.out.println("고객명 : " + name);
        System.out.println("전화번호 : " + phone);
        System.out.println("이메일 : " + email);
        System.out.println("객실번호 : " + roomId);
        System.out.println("체크인 : " + checkIn);
        System.out.println("체크아웃 : " + checkOut);
    }
    
    private void viewReservation() {

        System.out.println("\n===== 예약 조회 =====");

        System.out.print("고객 ID 입력 : ");
        int customerId = sc.nextInt();

        System.out.println(customerId + "번 고객 예약 조회");
    }
    
    private void cancelReservation() {

        System.out.println("\n===== 예약 취소 =====");

        System.out.print("예약번호 입력 : ");
        int reservationId = sc.nextInt();

        System.out.println(reservationId + "번 예약 취소");
    }
}