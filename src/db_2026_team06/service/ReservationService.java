package db_2026_team06.service;

import db_2026_team06.dao.ReservationDAO;
import db_2026_team06.model.Customer;
import db_2026_team06.model.Room;

import java.time.LocalDate;
import java.util.List;

/**
 * 예약 화면과 관련된 비즈니스 로직을 처리하는 서비스 클래스
 */
public class ReservationService {

    private final ReservationDAO reservationDAO;

    public ReservationService() {
        this.reservationDAO = new ReservationDAO();
    }

    /**
     * 예약 가능한 룸 목록을 반환합니다. (동적 쿼리 + 조인)
     */
    public List<Room> getAvailableRooms(int hotelId, LocalDate checkIn,
                                         LocalDate checkOut, int guests) {
        return reservationDAO.findAvailableRooms(hotelId, checkIn, checkOut, guests);
    }

    /**
     * 예약을 생성합니다. (트랜잭션)
     * @return 생성된 reservation_id, 실패 시 -1
     */
    public int createReservation(Customer customer, int roomId, LocalDate checkIn, LocalDate checkOut, int guests) {
        try {
            if (!reservationDAO.checkAvailability(roomId, checkIn, checkOut)) {
                return -2;
            }
            return reservationDAO.createReservation(customer, roomId, checkIn, checkOut, guests);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * 예약 상세 정보를 반환합니다. (뷰 사용)
     * @return [호텔명, 룸유형, 체크인, 체크아웃, 인원, 총가격, 고객명] 배열
     */
    public String[] getReservationDetail(int reservationId) {
        return reservationDAO.findReservationDetail(reservationId);
    }

    /**
     * 예약을 수정합니다. (Update)
     */
    public boolean updateReservation(int reservationId, LocalDate checkIn,
                                      LocalDate checkOut, int guests) {
        if (checkIn.isAfter(checkOut) || checkIn.isEqual(checkOut)) return false;
        return reservationDAO.updateReservation(reservationId, checkIn, checkOut, guests);
    }

    /**
     * 예약을 취소합니다. (Delete)
     */
    public boolean cancelReservation(int reservationId) {
        try {
            return reservationDAO.cancelReservation(reservationId);
        } catch (Exception e) {
            System.err.println("[오류] 예약 취소 중 문제 발생: " + e.getMessage());
            // 에러가 발생하면 취소 실패를 의미하는 false를 반환합니다.
            return false;
        }
    }

    /**
     * 날짜 유효성 검사
     */
    public String validateDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null || checkOut == null)        return "날짜를 입력해주세요.";
        if (!checkIn.isAfter(LocalDate.now().minusDays(1))) return "체크인은 오늘 이후여야 합니다.";
        if (!checkOut.isAfter(checkIn))                 return "체크아웃은 체크인 이후여야 합니다.";
        return null; // 유효
    }

    /**
     * 숙박 요금 계산
     */
    public int calcTotalPrice(int pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        long nights = checkOut.toEpochDay() - checkIn.toEpochDay();
        return (int)(nights * pricePerNight);
    }

    /**
     * 특정 고객의 예약 내역 목록을 반환합니다.
     */
    public List<String[]> getReservationsByCustomerId(int customerId) {
        return reservationDAO.findReservationsByCustomerId(customerId);
    }
}
