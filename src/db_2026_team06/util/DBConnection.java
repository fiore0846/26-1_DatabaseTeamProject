package db_2026_team06.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    // 싱글톤 인스턴스 보관 변수
    private static Connection connection = null;

    // 데이터베이스 접속 정보 상수 정의
    private static final String URL = "jdbc:mysql://localhost:3306/DB2026Team06?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "DB2026Team06";
    private static final String PASSWORD = "DB2026Team06";

    // 외부에서 인스턴스를 생성하지 못하도록 생성자를 private으로 제한
    private DBConnection() {}

     //싱글톤 패턴으로 구현된 Connection 객체 반환 메서드
     //@return Connection 객체
     //@throws SQLException 데이터베이스 연결 실패 시 발생

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // MySQL JDBC 드라이버 로드
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (ClassNotFoundException e) {
                System.err.println("[오류] 데이터베이스 드라이버를 찾을 수 없습니다: " + e.getMessage());
                throw new SQLException(e);
            }
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("[시스템] 데이터베이스 연결이 안전하게 종료되었습니다.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}