package banking;

import java.util.Scanner;

public class BankingSystem {
    public void startBankingSystem(String[] args) {
        // Scanners for choice
        Scanner scanner1 = new Scanner(System.in);
        Scanner scanner2 = new Scanner(System.in);
        String choose = "";
        //Database class for connection and to interact to db
        //Automatically creates database if not exists
        Database database = new Database(args);
        //Variables for interaction
        String cardNumber;
        Account account;
        int balance;
        //Main simple banking system
        //Loop choice
        while(true) {
            database.printMenu();
            //Scanning choice
            choose = scanner1.nextLine();

            switch (choose) {
                case "1":
                    //Create bank account
                    database.printCreateChoice();
                    break;
                case "2":
                    //Asking user for card number and pin code to log in to account
                    database.printLogIntoAccountChoice(scanner1, scanner2);
                    break;
                //System exit
                case "0":
                    database.printExitChoice();
                    break;
                default:
                    break;
            }
        }
    }
}
