package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;
import java.util.Arrays;
import java.util.Scanner;

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
        try {
            getConnection();
        } catch (SQLException exc) {
            System.out.println("Connection failed.");
            System.exit(0);
        }
        //Create table if not exists
        try {
            createTable();
        } catch (SQLException exc) {
            System.out.println("Failed to create table.");
            System.err.println(exc);
            System.exit(0);
        }
    }
    //Set url to database
    public Database (String[] args) {
        int index = Arrays.asList(args).indexOf("-fileName");
        url += args[++index];
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        try {
            getConnection();
        } catch (SQLException exc) {
            System.out.println("Connection failed.");
            System.exit(0);
        }
        //Create table if not exists
        try {
            createTable();
        } catch (SQLException exc) {
            System.out.println("Failed to create table.");
            System.err.println(exc);
            System.exit(0);
        }
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
        System.out.println("\nThe account has been closed!\n");
    }
    //Close connection to db
    public void closeConnection() throws SQLException {
        conn.close();
        statement.close();
    }

    public void printMenu() {
        System.out.println("1. Create an account");
        System.out.println("2. Log into account");
        System.out.println("0. Exit");
    }

    public void printCreateChoice() {
        Account account;
        String cardNumber;
        try {
            do {
                //Creates Account class instance that generate
                //card number according to Luhn algorithm and pin code
                account = new Account();
                //Getting card number
                cardNumber = account.getCardNumber();
                //Checking if card number is already exists
                //If exists then generate another card number and pin code
            } while(findNumber(cardNumber));
            //After checking for unique card number then
            //add card to database
            addCard(cardNumber, account.getPinCode());
            //Print info about card number and pin code to user
            System.out.println("\nYour card has been created");
            System.out.println("Your card number:");
            System.out.println(cardNumber);
            System.out.println("Your card PIN:");
            System.out.println(account.getPinCode() + "\n");
        } catch (SQLException exc) {
            System.err.println(exc.toString());
        }
    }

    public void printMenuLIA() {
        System.out.println("\n1. Balance");
        System.out.println("2. Add income");
        System.out.println("3. Do transfer");
        System.out.println("4. Close account");
        System.out.println("5. Log out");
        System.out.println("0. Exit");
    }

    public void printAddIncomeChoice(String cardNumber, Scanner scanner2) {
        System.out.println("\nEnter income:");
        try {
            addBalance(cardNumber, scanner2.nextInt());
        } catch (SQLException ignored) {}
        System.out.println("Income was added!");
    }

    public void printTransferChoice(String cardNumber, int pin, Scanner scanner1, Scanner scanner2) {
        String cardNumber2;
        int amount;

        System.out.println("\nTransfer");
        System.out.println("Enter card number:");
        cardNumber2 = scanner1.nextLine();

        //Checking card through Luhn algorithm
        if (!checkLuhnAlgorithm(cardNumber2)) {
            System.out.println("Probably you made a mistake in the card number.");
            System.out.println("Please try again!");
        } else {
            //Find card in database
            try {
                if (!findNumber(cardNumber2)) {
                    System.out.println("Such a card does not exist.");

                } else {
                    //Scanning amount of money to transfer
                    System.out.println("Enter how much money you want to transfer:");
                    amount = scanner2.nextInt();
                    //Checking balance for transfer
                    if (amount > getBalance(cardNumber, pin)) {
                        System.out.println("Not enough money!");
                    } else {
                        //Transfer money to another account
                        doTransfer(cardNumber, cardNumber2, amount);
                        System.out.println("Success!");
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public void printExitChoice() {
        System.out.println("\nBye!");
        try {
            closeConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        System.exit(0);
    }

    public void printLogIntoAccountChoice(Scanner scanner1, Scanner scanner2) {
        String cardNumber, cardNumber2, choose;
        int amount, pin, balance;

        System.out.println("\nEnter your card number:");
        cardNumber = scanner1.nextLine();
        System.out.println("Enter your PIN:");
        pin = scanner2.nextInt();

        try {
            //Checking in database if card number and pin code match
            if (logIntoAccount(cardNumber, pin)) {
                System.out.println("\nYou have successfully logged in!");

                //Loop inside account menu choice
                logout:
                while (true) {
                    printMenuLIA();
                    choose = scanner1.nextLine();
                    balance = getBalance(cardNumber, pin);

                    switch (choose) {
                        //Show user balance
                        case "1":
                            System.out.println("\nBalance: " + balance);
                            break;
                        //Top up account
                        case "2":
                            printAddIncomeChoice(cardNumber, scanner2);
                            break;
                        //Transfer to another account
                        case "3":
                            printTransferChoice(cardNumber, pin, scanner1, scanner2);
                            break;
                        //Delete account
                        case "4":
                            deleteAccount(cardNumber, pin);
                            break logout;
                        //Log out
                        case "5":
                            System.out.println("\nYou have successfully logged out!");
                            break logout;
                        //System exit
                        case "0":
                            printExitChoice();
                            break;
                        default:
                            break;
                    }
                }
            } else {
                //If card number and pin mismatch
                System.out.println("\nWrong card number or PIN!\n");
            }
        } catch (SQLException exc) {
            System.err.println(exc.toString());
        }
    }
}