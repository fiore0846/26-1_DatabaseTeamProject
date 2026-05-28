package db_2026_team06.menu;

import db_2026_team06.model.Hotel;
import db_2026_team06.model.Room;
import db_2026_team06.model.Review;
import db_2026_team06.service.HotelService;

import java.util.List;
import java.util.Scanner;

public class HotelDetailMenu {
    private Scanner scanner = new Scanner(System.in);
    private HotelService hotelService = new HotelService();

    // 호텔 세부 정보 화면 실행
    public void showHotelDetailMenu(int hotelId) {
        // 처음 진입 시 호텔 기본 정보만 출력
        printHotelDetail(hotelId);

        while (true) {
            System.out.println("\n===== 다음 작업 선택 =====");
            System.out.println("1. 룸 정보 보기");
            System.out.println("2. 리뷰 보기");
            System.out.println("3. 예약하기");
            System.out.println("0. 뒤로가기");
            System.out.print("선택: ");

            int choice = scanner.nextInt();
            scanner.nextLine();

            if (choice == 1) {
                // 해당 호텔의 룸 목록 출력
                printRoomList(hotelId);
            } else if (choice == 2) {
                // 해당 호텔의 리뷰 목록 출력
                printReviewList(hotelId);
            } else if (choice == 3) {
                // 예약 화면으로 이동
                goToReservationMenu(hotelId);
            } else if (choice == 0) {
                // 이 메서드를 종료하면 이전 화면, 즉 호텔 탐색/지도 화면으로 돌아감
                System.out.println("호텔 탐색 화면으로 돌아갑니다.");
                return;
            } else {
                System.out.println("잘못된 입력입니다. 다시 선택해주세요.");
            }
        }
    }

    // 호텔 기본 정보 출력
    private void printHotelDetail(int hotelId) {
        Hotel hotel = hotelService.getHotelDetail(hotelId);

        System.out.println("\n===== 호텔 기본 정보 =====");

        if (hotel == null) {
            System.out.println("해당 호텔 정보를 찾을 수 없습니다.");
            return;
        }

        // 평점은 Hotel 테이블이 아니라 Review.rating 평균으로 계산
        double avgRating = hotelService.getAverageRatingByHotelId(hotelId);

        System.out.println("호텔명: " + hotel.getHotelName());
        System.out.println("위치: " + hotel.getLocation());
        System.out.println("전화번호: " + hotel.getContact());
        System.out.printf("평점: %.1f%n", avgRating););
        System.out.println("설명: " + hotel.getHDescription());
    }

    // 해당 호텔의 룸 목록 출력
    private void printRoomList(int hotelId) {
        List<Room> roomList = hotelService.getRoomsByHotelId(hotelId);

        System.out.println("\n===== 룸 정보 =====");

        if (roomList == null || roomList.isEmpty()) {
            System.out.println("등록된 룸 정보가 없습니다.");
            return;
        }

        for (Room room : roomList) {
            System.out.println("------------------------------");
            System.out.println("객실 번호: " + room.getRoomNumber());
            System.out.println("룸 유형: " + room.getType());
            System.out.println("1박당 가격: " + room.getPricePerNight() + "원");
            System.out.println("최대 숙박 인원: " + room.getCapacity() + "명");
        }

        System.out.println("------------------------------");
    }

    // 해당 호텔의 리뷰 목록 출력
    private void printReviewList(int hotelId) {
        List<Review> reviewList = hotelService.getReviewsByHotelId(hotelId);

        System.out.println("\n===== 리뷰 정보 =====");

        if (reviewList == null || reviewList.isEmpty()) {
            System.out.println("등록된 리뷰가 없습니다.");
            return;
        }

        for (Review review : reviewList) {
            System.out.println("------------------------------");
            System.out.println("리뷰 ID: " + review.getReviewId());
            System.out.println("리뷰 일자: " + review.getReviewDate());
            System.out.println("평점: " + review.getRating());
            System.out.println("리뷰 내용: " + review.getReview());
        }

        System.out.println("------------------------------");
    }

    // 예약 화면으로 이동
    private void goToReservationMenu(int hotelId) {
        System.out.println("예약 화면으로 이동합니다.");

        // 예약 메뉴가 완성되면 여기에 연결하면 됨
        // ReservationMenu reservationMenu = new ReservationMenu();
        // reservationMenu.showReservationMenu(hotelId);
    }
}