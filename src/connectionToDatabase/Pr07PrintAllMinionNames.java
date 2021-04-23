package connectionToDatabase;

import com.mysql.cj.Constants;

import java.sql.*;
import java.util.Properties;

public class Pr07PrintAllMinionNames {
    private static final String minionNames = "SELECT m.name FROM `minions` AS m";
    public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    public static final String MINIONS_TABLE_NAME = "minions_db";

    public static void main(String[] args) {
        Properties properties = new Properties();
        String user = "root";
        String password = "password";
        properties.setProperty("user",user);
        properties.setProperty("password",password);

        try (Connection connection = DriverManager.getConnection(CONNECTION_STRING + MINIONS_TABLE_NAME, properties);
             PreparedStatement minionsStatement = connection.prepareStatement(
                     minionNames, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet minions = minionsStatement.executeQuery()) {

            int minionsCount = 0;
            while (minions.next()) {
                minionsCount++;
            }

            minions.beforeFirst();

            int firstIndex = 1;
            int lastIndex = minionsCount;

            for (int i = 1; i < minionsCount + 1; i++) {
                if (i % 2 != 0) {
                    minions.absolute(firstIndex);
                    firstIndex++;
                } else {
                    minions.absolute(lastIndex);
                    lastIndex--;
                }

                System.out.println(minions.getString("name"));
                minions.next();
            }

        } catch (SQLException se) {
            se.printStackTrace();
        }
    }
}
