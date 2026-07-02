package birh.db;

import java.sql.*;

public class DatabaseHandler {
    // MySQL መረጃዎች - እንደ አንተ ኮምፒውተር Setup ቀይራቸው
    private static final String DB_NAME = "student_db";
    private static final String URL = "jdbc:mysql://localhost:3306/" + DB_NAME;
    private static final String USER = "root"; // MySQL username
    private static final String PASS = "";     // MySQL password (ባዶ ከሆነ "")

    public static Connection getConnection() throws SQLException {
        try {
            // ለ Connector/J 9.x ተገቢው ድራይቨር ስም
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASS);
        } catch (ClassNotFoundException e) {
            throw new SQLException("MySQL Connector Driver በትክክል! JAR ፋይሉ በትክክል መያያዙን አረጋግጥ።");
        }
    }

    public static void initializeDatabase() {
        // መጀመሪያ ያለ ዳታቤዝ ስም በመገናኘት ዳታቤዙ መኖሩን ማረጋገጥ
        String baseUri = "jdbc:mysql://localhost:3306/?useSSL=false&allowPublicKeyRetrieval=true";
        try (Connection conn = DriverManager.getConnection(baseUri, USER, PASS);
             Statement stmt = conn.createStatement()) {
            
            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            stmt.executeUpdate("USE " + DB_NAME);

            // 1. Modules Table
            stmt.execute("CREATE TABLE IF NOT EXISTS modules (" +
                    "module_code VARCHAR(20) PRIMARY KEY, " +
                    "module_name VARCHAR(100) NOT NULL)");

            // 2. Courses Table
            stmt.execute("CREATE TABLE IF NOT EXISTS courses (" +
                    "course_code VARCHAR(20) PRIMARY KEY, " +
                    "course_name VARCHAR(100) NOT NULL, " +
                    "credit_hours INT, " +
                    "lecture INT, " +
                    "lab INT, " +
                    "tutorial INT, " +
                    "module_code VARCHAR(20), " +
                    "prerequisite_code VARCHAR(20), " +
                    "FOREIGN KEY (module_code) REFERENCES modules(module_code), " +
                    "FOREIGN KEY (prerequisite_code) REFERENCES courses(course_code))");

            // 3. Students Table
            stmt.execute("CREATE TABLE IF NOT EXISTS students (" +
                    "student_id VARCHAR(20) PRIMARY KEY, " +
                    "full_name VARCHAR(100) NOT NULL, " +
                    "department VARCHAR(100))");

            // 4. Registrations Table
            stmt.execute("CREATE TABLE IF NOT EXISTS registrations (" +
                    "reg_id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "student_id VARCHAR(20), " +
                    "course_code VARCHAR(20), " +
                    "grade DOUBLE, " +
                    "year INT, " +
                    "semester INT, " +
                    "FOREIGN KEY (student_id) REFERENCES students(student_id), " +
                    "FOREIGN KEY (course_code) REFERENCES courses(course_code))");

            System.out.println("MySQL ዳታቤዝ በተሳካ ሁኔታ ተገናኝቷል።");
        } catch (SQLException e) {
            System.err.println("የዳታቤዝ ዝግጅት ስህተት፦ " + e.getMessage());
        }
    }
}
