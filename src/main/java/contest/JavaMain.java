package contest;

import framework.*;

import java.util.stream.*;

public class JavaMain {
    public static void main(String[] args) {
        // challenge: sum of all numbers
        final int level = 0;
        for (int i = 1; i <= 2; i++) {
            final int sum = Stream.of(Framework.readInput(level, i).split(" "))
                .mapToInt(Integer::parseInt)
                .sum();

            System.out.println("Level " + level + ", Input " + i + " = " + sum);
            Framework.writeOutput(level, i, String.valueOf(sum));
        }
    }
}
