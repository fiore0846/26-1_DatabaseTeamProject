-- 사용자 생성
CREATE USER 'DB2026Team06'@'localhost' IDENTIFIED BY 'DB2026Team06';
GRANT ALL PRIVILEGES ON DB2026Team06.* TO 'DB2026Team06'@'localhost';
FLUSH PRIVILEGES;

-- 데이터베이스 생성 및 선택
DROP DATABASE IF EXISTS DB2026Team06;
CREATE DATABASE DB2026Team06;
USE DB2026Team06;

-- 1. Hotel 테이블 생성
CREATE TABLE Hotel (
    hotel_id INT PRIMARY KEY AUTO_INCREMENT,
    hotel_name VARCHAR(255) NOT NULL,
    location VARCHAR(255) NOT NULL,
    contact VARCHAR(255) NOT NULL,
    h_description TEXT
);

-- 2. Room 테이블 생성 (Hotel 참조)
CREATE TABLE Room (
    room_number INT PRIMARY KEY,
    type VARCHAR(255) NOT NULL,
    price_per_night INT NOT NULL,
    capacity INT NOT NULL,
    hotel_id INT NOT NULL,
    FOREIGN KEY (hotel_id) REFERENCES Hotel(hotel_id) ON DELETE CASCADE
);

-- 3. Customer 테이블 생성
CREATE TABLE Customer (
    customer_id INT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone VARCHAR(255)
);

-- 4. Reservation 테이블 생성 (Room, Customer 참조)
CREATE TABLE Reservation (
    reservation_id INT PRIMARY KEY AUTO_INCREMENT,
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    reservation_date DATE NOT NULL,
    guests INT NOT NULL,
    room_number INT NOT NULL,
    customer_id INT NOT NULL,
    FOREIGN KEY (room_number) REFERENCES Room(room_number) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- 5. Attraction 테이블 생성
CREATE TABLE Attraction (
    attraction_id INT PRIMARY KEY AUTO_INCREMENT,
    attraction_name VARCHAR(255) NOT NULL,
    a_description TEXT
);

-- 6. Hotel_Attraction 매핑 테이블 생성
CREATE TABLE Hotel_Attraction (
    hotel_id INT NOT NULL,
    attraction_id INT NOT NULL,
    PRIMARY KEY (hotel_id, attraction_id),
    FOREIGN KEY (hotel_id) REFERENCES Hotel(hotel_id) ON DELETE CASCADE,
    FOREIGN KEY (attraction_id) REFERENCES Attraction(attraction_id) ON DELETE CASCADE
);

-- 7. Review 테이블 생성 (Hotel, Customer 참조)
CREATE TABLE Review (
    review_id INT PRIMARY KEY AUTO_INCREMENT,
    rating INT NOT NULL,
    review_date DATE,
    review VARCHAR(255),
    hotel_id INT NOT NULL,
    customer_id INT NOT NULL,
    FOREIGN KEY (hotel_id) REFERENCES Hotel(hotel_id) ON DELETE CASCADE,
    FOREIGN KEY (customer_id) REFERENCES Customer(customer_id) ON DELETE CASCADE
);

-- 룸 정보 출력하는 뷰 생성
CREATE VIEW vRoomInfo AS
SELECT 
    r.room_number,
    r.type,
    r.price_per_night,
    r.capacity,
    r.hotel_id
FROM Room r;

-- 예약 정보 출력하는 뷰 생성
CREATE VIEW vReservationDetail AS
SELECT 
    r.customer_id AS customerId,
    r.reservation_id AS reservationId,
    c.name AS customer_name,
    r.room_number,
    rm.type AS room_type,
    rm.price_per_night,
    r.guests,
    r.check_in,
    r.check_out,
    r.reservation_date
FROM Reservation r
JOIN Customer c ON r.customer_id = c.customer_id -- 예약 정보와 고객 정보 조인
JOIN Room rm ON r.room_number = rm.room_number; -- 예약 정보와 룸 정보 조인

-- checkAvailability(): room_number + 날짜 범위로 조회
CREATE INDEX idx_reservation_room_date ON Reservation(room_number, check_in, check_out);

-- viewReservation(): customer_id로 조회
CREATE INDEX idx_reservation_customer ON Reservation(customer_id);

-- checkInCustomer(): name + phone + email로 조회
CREATE INDEX idx_customer_lookup ON Customer(name, phone, email);

-- 초기 데이터
INSERT INTO Hotel VALUES
(1, '서울 스카이 호텔', '서울 강남구', '02-1111-1111', '도심 비즈니스 호텔'),
(2, '부산 오션 호텔', '부산 해운대구', '051-2222-2222', '해변 전망 호텔'),
(3, '제주 리조트', '제주시 애월읍', '064-3333-3333', '휴양형 리조트');

INSERT INTO Room VALUES
(101, 'Standard', 120000, 2, 1),
(102, 'Deluxe', 180000, 3, 1),
(103, 'Suite', 280000, 4, 1),

(201, 'Standard', 130000, 2, 2),
(202, 'Deluxe', 200000, 3, 2),
(203, 'Suite', 320000, 4, 2),

(301, 'Standard', 140000, 2, 3),
(302, 'Family', 260000, 5, 3);

INSERT INTO Customer VALUES
(1, '김민준', 'minjun@gmail.com', '010-1111-1111'),
(2, '박서연', 'seoyeon@gmail.com', '010-2222-2222'),
(3, '이도윤', 'doyoon@gmail.com', '010-3333-3333'),
(4, '최지우', 'jiwoo@gmail.com', '010-4444-4444'),
(5, '정하은', 'haeun@gmail.com', '010-5555-5555'),
(6, '한지민', 'jimin@gmail.com', '010-6666-6666'),
(7, '윤서준', 'seojun@gmail.com', '010-7777-7777'),
(8, '강민서', 'minseo@gmail.com', '010-8888-8888');

INSERT INTO Reservation VALUES
(1,'2026-06-10','2026-06-12','2026-05-29',2,101,1),
(2,'2026-06-15','2026-06-18','2026-05-28',3,102,2),
(3,'2026-06-20','2026-06-22','2026-05-25',2,201,3),
(4,'2026-07-01','2026-07-03','2026-05-30',4,203,4),
(5,'2026-07-05','2026-07-07','2026-05-27',2,301,5),
(6,'2026-07-10','2026-07-13','2026-05-26',5,302,6),
(7,'2026-08-01','2026-08-03','2026-05-20',2,103,7),
(8,'2026-08-15','2026-08-18','2026-05-18',3,202,8);

INSERT INTO Attraction VALUES
(1, '경복궁', '서울 대표 관광지'),
(2, '해운대 해수욕장', '부산 대표 해변'),
(3, '성산일출봉', '제주 대표 관광지'),
(4, '남산타워', '서울 야경 명소');

INSERT INTO Hotel_Attraction VALUES
(1,1),
(1,4),
(2,2),
(3,3);

INSERT INTO Review VALUES
(1,5,'2026-05-10','객실이 매우 깨끗했습니다.',1,1),
(2,4,'2026-05-12','위치가 좋아요.',2,2),
(3,5,'2026-05-15','바다 전망이 훌륭합니다.',2,3),
(4,4,'2026-05-18','조용해서 좋았습니다.',3,4),
(5,5,'2026-05-20','가족 여행에 추천합니다.',3,5);