package util;

import java.util.Arrays;
import java.util.Scanner;

public class InputHelper {
    private final Scanner scanner;

    public InputHelper(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }

    public String readRequired(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isBlank()) {
                return value;
            }
            System.out.println("Input cannot be blank.");
        }
    }

    public String readOptional(String prompt, String currentValue) {
        String value = readLine(prompt + " [" + currentValue + "]: ");
        return value.isBlank() ? currentValue : value;
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= min && parsed <= max) {
                    return parsed;
                }
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    public int readIntMin(String prompt, int min) {
        while (true) {
            String value = readLine(prompt);
            try {
                int parsed = Integer.parseInt(value);
                if (parsed >= min) {
                    return parsed;
                }
                System.out.println("Enter a number greater than or equal to " + min + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid integer.");
            }
        }
    }

    public double readDouble(String prompt, double min, double max) {
        while (true) {
            String value = readLine(prompt);
            try {
                double parsed = Double.parseDouble(value);
                if (parsed >= min && parsed <= max) {
                    return parsed;
                }
                System.out.println("Enter a number from " + min + " to " + max + ".");
            } catch (NumberFormatException ex) {
                System.out.println("Enter a valid number.");
            }
        }
    }

    public boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (y/n): ").toLowerCase();
            if (value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("Enter y or n.");
        }
    }

    public <T extends Enum<T>> T readEnum(String prompt, Class<T> enumClass) {
        while (true) {
            String value = readLine(prompt + " " + Arrays.toString(enumClass.getEnumConstants()) + ": ");
            try {
                return Enum.valueOf(enumClass, value.trim().toUpperCase());
            } catch (IllegalArgumentException ex) {
                System.out.println("Invalid option.");
            }
        }
    }
}
