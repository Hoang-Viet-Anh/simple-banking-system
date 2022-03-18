package banking;

import java.sql.SQLException;
import java.util.Scanner;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        // Scanners for choice
        Scanner scanner1 = new Scanner(System.in);
        Scanner scanner2 = new Scanner(System.in);
        String choose = "";
        //Database class for connection and to interact to db
        //Automatically creates database if not exists
        Database database = new Database(args);
        try {
            database.getConnection();
        } catch (SQLException exc) {
            System.out.println("Connection failed.");
            System.exit(0);
        }
        //Create table if not exists
        try {
            database.createTable();
        } catch (SQLException exc) {
            System.out.println("Failed to create table.");
            System.err.println(exc);
            System.exit(0);
        }
        //Variables for interaction
        String cardNumber;
        Account account;
        int balance;
        //Main simple banking system
        //Loop choice
        while(true) {
            System.out.println("1. Create an account");
            System.out.println("2. Log into account");
            System.out.println("0. Exit");
            //Scanning choice
            choose = scanner1.nextLine();

            switch (choose) {
                case "1":
                    try {
                        do {
                            //Creates Account class instance that generate
                            //card number according to Luhn algorithm and pin code
                            account = new Account();
                            //Getting card number
                            cardNumber = account.getCardNumber();
                        //Checking if card number is already exists
                        //If exists then generate another card number and pin code
                        } while(database.findNumber(cardNumber));
                        //After checking for unique card number then
                        //add card to database
                        database.addCard(cardNumber, account.getPinCode());
                        //Print info about card number and pin code to user
                        System.out.println("\nYour card has been created");
                        System.out.println("Your card number:");
                        System.out.println(cardNumber);
                        System.out.println("Your card PIN:");
                        System.out.println(account.getPinCode() + "\n");
                    } catch (SQLException exc) {
                        System.err.println(exc.toString());
                    }
                    break;

                case "2":
                    //Asking user for card number and pin code to log in to account
                    System.out.println("\nEnter your card number:");
                    cardNumber = scanner1.nextLine();
                    System.out.println("Enter your PIN:");
                    int pin = scanner2.nextInt();
                    String cardNumber2;
                    int amount;

                    try {
                        //Checking in database if card number and pin code match
                        if (database.logIntoAccount(cardNumber, pin)) {
                            System.out.println("\nYou have successfully logged in!");

                            //Loop inside account menu choice
                            logout:
                            while (true) {
                                System.out.println("\n1. Balance");
                                System.out.println("2. Add income");
                                System.out.println("3. Do transfer");
                                System.out.println("4. Close account");
                                System.out.println("5. Log out");
                                System.out.println("0. Exit");
                                choose = scanner1.nextLine();
                                balance = database.getBalance(cardNumber, pin);

                                switch (choose) {
                                    //Show user balance
                                    case "1":
                                        System.out.println("\nBalance: " + balance);
                                        break;
                                    //Top up account
                                    case "2":
                                        System.out.println("\nEnter income:");
                                        database.addBalance(cardNumber, scanner2.nextInt());
                                        System.out.println("Income was added!");
                                        break;
                                    //Transfer to another account
                                    case "3":
                                        System.out.println("\nTransfer");
                                        System.out.println("Enter card number:");
                                        cardNumber2 = scanner1.nextLine();
                                        //Checking card through Luhn algorithm
                                        if (!database.checkLuhnAlgorithm(cardNumber2)) {
                                            System.out.println("Probably you made a mistake in the card number.");
                                            System.out.println("Please try again!");
                                            break;
                                        } else {
                                            //Find card in database
                                            if (!database.findNumber(cardNumber2)) {
                                                System.out.println("Such a card does not exist.");
                                                break;
                                            } else {
                                                //Scanning amount of money to transfer
                                                System.out.println("Enter how much money you want to transfer:");
                                                amount = scanner2.nextInt();
                                                //Checking balance for transfer
                                                if (amount > database.getBalance(cardNumber, pin)) {
                                                    System.out.println("Not enough money!");
                                                    break;
                                                } else {
                                                    //Transfer money to another account
                                                    database.doTransfer(cardNumber, cardNumber2, amount);
                                                    System.out.println("Success!");
                                                }
                                            }
                                        }
                                        break;
                                    //Delete account
                                    case "4":
                                        database.deleteAccount(cardNumber, pin);
                                        System.out.println("\nThe account has been closed!\n");
                                        break logout;
                                    //Log out
                                    case "5":
                                        System.out.println("\nYou have successfully logged out!");
                                        break logout;
                                    //System exit
                                    case "0":
                                        System.out.println("\nBye!");
                                        database.closeConnection();
                                        System.exit(0);
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
                    break;

                //System exit
                case "0":
                    System.out.println("\nBye!");
                    try {
                        database.closeConnection();
                    } catch (SQLException exc) {
                        System.err.println(exc.toString());
                    }
                    System.exit(0);
                    break;
                default:
                    break;
            }
        }
    }
}
