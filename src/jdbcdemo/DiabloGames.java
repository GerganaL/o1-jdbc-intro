package jdbcdemo;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.Properties;
import java.util.Scanner;

public class DiabloGames {
    public static void main(String[] args) {
        // 1. Read properties from external property file
        Properties properties = new Properties();
        String path = DiabloGames.class.getClassLoader().getResource("jdbc.properties").getPath();
        System.out.printf("Resource path: %s%n",path);

        try {
            properties.load(new FileInputStream(path));
        }catch (IOException e){
            e.printStackTrace();
        }

        //TODO: add meaningful deafults
        System.out.println(properties);
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter username (<Enter> for 'Alex'): ");
        String username = sc.nextLine().trim();
        username = username.length() > 0? username : "Alex";

        //2. try with resources - Conncetion , PrepareadStatement

        try (Connection con = DriverManager.getConnection(
                properties.getProperty("db.url")
                ,properties.getProperty("db.user")
                , properties.getProperty("db.password"));
            PreparedStatement ps = con.prepareStatement(properties.getProperty("sql.games"))){
            ps.setString(1,username);
            ResultSet rs = ps.executeQuery();

            // 3. Print data
            if(rs.next()) {
                do {
                    System.out.printf("| %10d | %-15.15s | %-15.15s | %10d |%n",
                            rs.getLong("id"),
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getInt("count")
                    );
                }while (rs.next());
            }else {
                System.out.printf("DB user with username '%s' not found.", username);
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
