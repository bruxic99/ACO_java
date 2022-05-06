import java.io.FileNotFoundException;
import java.util.Objects;
import java.util.stream.IntStream;

public class Main {

    public static void main(String[] args) {
        final double [] evaporation = {0.1};
        final int[] q = {500};
        IntStream.rangeClosed(1, 10)
                .forEach(i -> {
                    System.out.println("Attempt #" + i);
                    AntColonyOptimization aco = null;
                    try {
                        aco = new AntColonyOptimization(evaporation[0],q[0], "1.txt" );
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    Objects.requireNonNull(aco).solve();
                    evaporation[0] +=0.09;
                    q[0] -=50;
                });
        }
    }
