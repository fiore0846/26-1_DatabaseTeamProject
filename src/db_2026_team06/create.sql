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

CREATE VIEW vRoomInfo AS
SELECT 
    r.room_number,
    r.type,
    r.price_per_night,
    r.capacity,
    r.hotel_id
FROM Room r;

-- 테스트용 데이터 추가
INSERT INTO Hotel VALUES (1, 'EWHA Hotel', 'EWHA', '02-1234-5678', 'EWHA');
INSERT INTO Room VALUES (101, 'standard', 50000, 2, 1);
INSERT INTO Room VALUES (102, 'standard', 60000, 3, 1);
INSERT INTO Room VALUES (201, 'deluxe', 100000, 2, 1);
INSERT INTO Customer (customer_id, name, email, phone) VALUES
(123, '홍길동', 'hong@example.com', '010-1234-5678');