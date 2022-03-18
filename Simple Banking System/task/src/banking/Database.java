package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Arrays;

public class Database {

    private String url = "jdbc:sqlite:";
    private SQLiteDataSource dataSource;
    private Connection conn;
    private Statement statement;
    private ResultSet resultSet;
    private PreparedStatement preparedStatement;

    private final String ADD_BALANCE = "UPDATE card SET balance = balance + ? WHERE number = ?;";
    private final String SUBSTRACT_BALANCE = "UPDATE card SET balance = balance - ? WHERE number = ?;";
    private final String DELETE_ACCOUNT = "DELETE FROM card WHERE number = ? AND pin = ?;";
    private final String ADD_ACCOUNT = "INSERT INTO card (number, pin) VALUES (?, ?);";
    private final String GET_ACCOUNT = "SELECT * FROM card WHERE number = ?;";
    private final String LOG_IN_ACCOUNT = "SELECT * FROM card WHERE number = ? AND pin = ?;";

    //Set url to database
    public Database (String filename) {
        url += filename;
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
    }
    //Set url to database
    public Database (String[] args) {
        int index = Arrays.asList(args).indexOf("-fileName");
        url += args[++index];
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
    }
    //Connection to db
    public void getConnection() throws SQLException {
        conn = dataSource.getConnection();
        statement = conn.createStatement();
    }
    //Creating table if not exists
    public void createTable() throws SQLException {
        statement.execute("CREATE TABLE IF NOT EXISTS card (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "number VARCHAR(20)," +
                "pin VARCHAR(4)," +
                "balance INTEGER DEFAULT 0" +
                ");"
        );
    }
    //Add card data
    public void addCard(String number, int pin) throws SQLException {
        preparedStatement = conn.prepareStatement(ADD_ACCOUNT);
        preparedStatement.setString(1, number);
        preparedStatement.setInt(2, pin);
        preparedStatement.executeUpdate();
    }
    //Find specific card
    public boolean findNumber(String number) throws SQLException {
        preparedStatement = conn.prepareStatement(GET_ACCOUNT);
        preparedStatement.setString(1, number);
        resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }
    //Log into account
    public boolean logIntoAccount(String number, int pin) throws SQLException {
        preparedStatement = conn.prepareStatement(LOG_IN_ACCOUNT);
        preparedStatement.setString(1, number);
        preparedStatement.setInt(2, pin);
        resultSet = preparedStatement.executeQuery();
        return resultSet.next();
    }
    //Get account balance
    public int getBalance(String number, int pin) throws SQLException {
        preparedStatement = conn.prepareStatement(LOG_IN_ACCOUNT);
        preparedStatement.setString(1, number);
        preparedStatement.setInt(2, pin);
        resultSet = preparedStatement.executeQuery();
        resultSet.next();
        return resultSet.getInt("balance");
    }
    //Top up account balance
    public void addBalance(String number, int amount) throws SQLException {
        preparedStatement = conn.prepareStatement(ADD_BALANCE);
        preparedStatement.setInt(1, amount);
        preparedStatement.setString(2, number);
        preparedStatement.executeUpdate();
    }
    //Checking card number through Luhn algorithm
    public boolean checkLuhnAlgorithm(String number) {
            int nDigits = number.length();

            int nSum = 0;
            boolean isSecond = false;
            for (int i = nDigits - 1; i >= 0; i--)
            {

                int d = number.charAt(i) - '0';

                if (isSecond)
                    d = d * 2;

                nSum += d / 10;
                nSum += d % 10;

                isSecond = !isSecond;
            }
            return (nSum % 10 == 0);
    }
    //Transfer to another account
    public void doTransfer(String number1, String number2, int amount) throws SQLException {
        conn.setAutoCommit(false);
        Savepoint savepoint = conn.setSavepoint();

        preparedStatement = conn.prepareStatement(SUBSTRACT_BALANCE);
        preparedStatement.setInt(1, amount);
        preparedStatement.setString(2, number1);
        preparedStatement.executeUpdate();

        preparedStatement = conn.prepareStatement(ADD_BALANCE);
        preparedStatement.setInt(1, amount);
        preparedStatement.setString(2, number2);
        preparedStatement.executeUpdate();

        conn.commit();
        conn.setAutoCommit(true);
    }
    //Delete account from db
    public void deleteAccount(String number, int pin) throws SQLException {
        preparedStatement = conn.prepareStatement(DELETE_ACCOUNT);
        preparedStatement.setString(1, number);
        preparedStatement.setInt(2, pin);
        preparedStatement.executeUpdate();
    }
    //Close connection to db
    public void closeConnection() throws SQLException {
        conn.close();
        statement.close();
    }

}