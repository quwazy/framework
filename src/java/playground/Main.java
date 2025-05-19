package playground;

public class Main {

    public static void main(String[] args) {
        Calculator calculator = new Calculator();

        // Test addition
        int sum = calculator.add(5, 3);
        System.out.println("Sum: " + sum);

        // Test subtraction
        int difference = calculator.subtract(10, 4);
        System.out.println("Difference: " + difference);
    }
}