package banking;

import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class Account {

    private int[] cardNumber;
    private int pinCode;
    //Generating card number and pin according to Luhn algorithm
    public Account() {
        cardNumber = new int[16];
        Random random = new Random();
        String bin = "4 0 0 0 0 0";
        Scanner scanner = new Scanner(bin);
        int luhnSum = 0;
        for (int i = 0; i < 15; i++) {
            if(i < 6) {
                cardNumber[i] = scanner.nextInt();
            } else {
                cardNumber[i] = random.nextInt(10);
            }
            if (i % 2 == 0 && cardNumber[i] * 2 > 9) {
                luhnSum += cardNumber[i] * 2 - 9;
            } else if (i % 2 == 0 ) {
                luhnSum += cardNumber[i] * 2;
            } else {
                luhnSum += cardNumber[i];
            }
        }
        if(luhnSum % 10 == 0) {
            cardNumber[15] = 0;
        } else {
            cardNumber[15] = 10 - (luhnSum % 10);
        }
        pinCode = random.nextInt(9000) + 1000;
    }

    //Getters
    public String getCardNumber() {
        return Arrays.toString(cardNumber).replaceAll("[,\\[\\]\\ ]", "");
    }

    public int getPinCode() {
        return pinCode;
    }
}
