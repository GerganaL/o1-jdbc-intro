package connectionToDatabase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.Properties;

public class Homework {

    public static final String CONNECTION_STRING = "jdbc:mysql://localhost:3306/";
    public static final String MINIONS_TABLE_NAME = "minions_db";
    private Connection connection;
    private BufferedReader reader;
    private static final String GET_VILLAIN_FROM_ID = "SELECT name FROM villains WHERE id = %d";

    public Homework() {
        this.reader = new BufferedReader(new InputStreamReader(System.in));
    }

    public void setConnection(String user, String password) throws SQLException {
        Properties properties = new Properties();
        properties.setProperty("user", user);
        properties.setProperty("password", password);
        connection = DriverManager.getConnection(CONNECTION_STRING + MINIONS_TABLE_NAME, properties);
    }

    public void getVillainsNamesEx2() throws SQLException {
        String query = "SELECT v.name, COUNT(mv.minion_id) AS 'count' " +
                "FROM villains AS v " +
                "JOIN minions_villains mv on v.id = mv.villain_id " +
                "GROUP BY v.id " +
                "HAVING `count` >15 " +
                "ORDER BY `count` DESC;";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();

        while (resultSet.next()) {
            //get by column name
            System.out.printf("%s %d%n", resultSet.getString("name"), resultSet.getInt("count"));
            //get by index
            System.out.printf("%s %d%n", resultSet.getString(1), resultSet.getInt(2));

        }
    }

    public void getMinionsNamesEx3() throws IOException, SQLException {
        System.out.println("Enter villain id: ");
        int villainId = Integer.parseInt(reader.readLine());

        String villainName = getEntityNameById(villainId, "villains");

        if (villainName == null) {
            System.out.printf("No villain with ID %d exists in the database", villainId);
        } else {
            System.out.printf("Villain: %s%n", villainName);
            String query = "SELECT m.name, m.age FROM minions AS m\n" +
                    "JOIN minions_villains mv on m.id = mv.minion_id\n" +
                    "WHERE mv.villain_id = ?";

            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, villainId);

            ResultSet resultSet = statement.executeQuery();

            int counter = 1;
            while (resultSet.next()) {
                System.out.printf("%d. %s %d%n", counter++, resultSet.getString("name"), resultSet.getInt("age"));
            }

        }
    }

    private String getEntityNameById(int entityId, String tableName) throws SQLException {
        String query = String.format("SELECT name FROM %s WHERE id = ?", tableName);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, entityId);

        ResultSet resultSet = statement.executeQuery();

        return resultSet.next() ? resultSet.getString("name") : null;
    }

    public void addMinionEx4() throws IOException, SQLException {
        System.out.print("Enter Minions Info: name, age, town name:");
        String[] minionInfo = reader.readLine().split("\\s+");

        System.out.print("Enter Villain Info: name: ");
        String[] villainInfo = reader.readLine().split("\\s+");

        String villainName = villainInfo[1];

        String minionName = minionInfo[1];
        int age = Integer.parseInt(minionInfo[2]);
        String townName = minionInfo[3];

        int townID = getEntityIdByName(townName, "towns");
        if (townID < 0) {
            insertEntityInTowns(townName);
            townID = getEntityIdByName(townName, "towns");
            System.out.printf("Town %s was added to the database.%n", townName);
        }

        insertEntityInMinions(minionName, age, townID);
        int villainId = getEntityIdByName(villainName, "villains");

        if (villainId < 0) {
            insertEntityInVillains(villainName);
            villainId = getEntityIdByName(villainName, "villains");
            System.out.printf("Villain %s was added to the database%n", villainName);
        }

        int minionId = getEntityIdByName(minionName, "minions");
        if (!insertEntityInMinionsVillains(minionId, villainId)) {
            System.out.printf("Successfully added %s to be minion of %s%n", minionName, villainName);
        }

    }

    private void insertEntityInMinions(String minionName, int age, int townID) throws SQLException {
        String query = "INSERT INTO minions(name, age, town_id) value(?, ?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, minionName);
        preparedStatement.setInt(2, age);
        preparedStatement.setInt(3, townID);
        preparedStatement.execute();
    }

    private boolean insertEntityInMinionsVillains(int minionId, int villainId) throws SQLException {
        String query = "INSERT INTO minions_villains(minion_id, villain_id) value(?, ?)";
        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setInt(1, minionId);
        preparedStatement.setInt(2, villainId);
        return preparedStatement.execute();
    }

    private void insertEntityInVillains(String villainName) throws SQLException {
        String query = "INSERT INTO villains(name, evilness_factor) value(?,?)";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, villainName);
        preparedStatement.setString(2, "evil");
        preparedStatement.execute();
    }

    private void insertEntityInTowns(String townName) throws SQLException {
        String query = "INSERT INTO towns(name) value(?)";

        PreparedStatement preparedStatement = connection.prepareStatement(query);

        preparedStatement.setString(1, townName);
        preparedStatement.execute();
    }

    private int getEntityIdByName(String entityName, String tableName) throws SQLException {
        String query = String.format("SELECT id FROM %s WHERE name = ?", tableName);
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, entityName);

        ResultSet resultSet = statement.executeQuery();

        return resultSet.next() ? resultSet.getInt(1) : -1;
    }

    public void changeTownNameCasingEx5() throws IOException, SQLException {
        System.out.println("Enter country name: ");
        String countryName = reader.readLine();

        String query = "UPDATE towns SET name = UPPER(name) WHERE country = ?";
        String printQuery = "SELECT name FROM towns\n" +
                "WHERE country = ?";


        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, countryName);
        int townsAffected = statement.executeUpdate();
        System.out.println(String.format("%d town names were affected", townsAffected));

        statement = connection.prepareStatement(printQuery);
        statement.setString(1,countryName);
        ResultSet resultSet = statement.executeQuery();
        while (resultSet.next()) {
            System.out.printf("%s ", resultSet.getString("name"));
        }

    }



    public void increaseAgeWithStoreProcedureEx9() throws IOException, SQLException {
        System.out.println("Enter minion id: ");
        int minionId = Integer.parseInt(reader.readLine());

        String query = "CALL usp_get_older(?)";

        CallableStatement callableStatement = connection.prepareCall(query);
        callableStatement.setInt(1, minionId);

        callableStatement.execute();
    }

    public void removeVillianEx6() throws IOException, SQLException {
        System.out.printf("Enter Villain id: ");
        int villainId = Integer.parseInt(reader.readLine());

        String villainName = getEntityNameById(villainId, "villains");
        if(villainName == null){
            System.out.println("No such villainName was found");
        }else{
           deleteEntityFromVillains(villainId, villainName);
        }
    }



    private void deleteEntityFromVillains(int villainId, String villainName) throws SQLException {
        String query = "SELECT COUNT(mv.minion_id) AS cnt             \n" +
                "FROM `villains` AS v JOIN `minions_villains` AS mv \n" +
                "ON v.id = mv.villain_id WHERE v.id = ?";
        String query2 = "DELETE FROM minions_villains WHERE villain_id = ?";
        String query3 = "DELETE FROM villains where id = ?";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setInt(1, villainId);
        statement.execute();
        statement = connection.prepareStatement(query2);
        statement.setInt(1,villainId);
        statement.execute();
        statement = connection.prepareStatement(query3);
        statement.setInt(1,villainId);
        statement.execute();
        System.out.println(String.format("%d minions released"));
    System.out.println(String.format("%s was deleted", villainName));

    }

    public void printMinionsEx7() throws SQLException {
        String query = "SELECT m.name FROM `minions` AS m";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        ResultSet minions = preparedStatement.executeQuery();

        int minionsCount = 0;
        while (minions.next()) {
            minionsCount++;
        }


        int firstIndex = 1;
        int lastIndex = minionsCount;




        for (int i = 1; i < minionsCount + 1; i++) {
            if (i % 2 != 0) {
                minions.getString(firstIndex);
                firstIndex++;
            } else {
                System.out.println(minions.getString("name"));
                lastIndex--;
            }

            System.out.println(minions.getString("name"));
            minions.next();
        }
    }
}
