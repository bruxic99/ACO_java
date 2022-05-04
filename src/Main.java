import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        final double [] n = {0.1};
        final int[] x = {500};
        IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    System.out.println("Attempt #" + i);
                    AntColonyOptimization aco = null;
                    try {
                        System.out.println(x[0]);
                        aco = new AntColonyOptimization(n[0],x[0] );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Objects.requireNonNull(aco).solve();
                    n[0] +=0.09;
                    x[0] -=50;
                });
        }
    }
