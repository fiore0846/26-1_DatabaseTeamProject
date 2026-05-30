package db_2026_team06.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static Connection connection = null;
    private static final String URL = "jdbc:mysql://localhost:3306/DB2026Team06?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "DB2026Team06";
    private static final String PASSWORD = "DB2026Team06";

    private DBConnection() {}

    public static synchronized Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
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