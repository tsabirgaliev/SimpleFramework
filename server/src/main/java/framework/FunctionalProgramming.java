package framework;

import java.util.Random;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

public class FunctionalProgramming {
    public static void main(String[] args) {
        IntConsumer println = System.out::println;

        System.out.println("Task 1");
        // print each integer in range [1..5]
        IntStream intStream1 = IntStream.range(1, 6);
        intStream1.forEach(println);

        System.out.println("Task 2");
        // print square of each integer in range [1..5]
        IntStream intStream2 = IntStream.range(1, 6);
        IntStream intStream3 = intStream2.map(i -> i * i);

        intStream3.forEach(println);

        System.out.println("Task 3");
        // print 10 random integers in range [0..10)
        new Random().ints(10, 0, 10).forEach(println);

        System.out.println("Task 4");
        // for each of 10 random integers in range [0..10)
        // print "even" or "odd"
        Function<Integer, ?> isEven = (Integer i) -> i % 2 == 0 ? "even" : "odd";

        new Random().ints(10, 0, 10).boxed()
            .map(isEven)
            .forEach(System.out::println);


        System.out.println("Task 5");
        // print factorials of integers in range [1..10)
        IntStream.range(1, 11).map(i ->
            IntStream.range(1,i+1)
                .reduce(1, (a,b) -> a*b)
        ).forEach(println);

        System.out.println("Task 6");
        // print factorials of 10 random integers in range [1..10)
        // this is left as exercise for you!
        Random random = new Random();
        for (int i = 1; i<=10; i++) {
            int x = random.nextInt(10);
            int f = 1;
            for (int j = 1; j <= x; j++) {
                f = f * j;
            }
            System.out.println(f);
        }
    }
}
