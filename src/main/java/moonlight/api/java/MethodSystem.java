package moonlight.api.java;

public class MethodSystem {

    public static long executionTime(Runnable code) {
        long startTime = System.nanoTime();
        code.run();
        long endTime = System.nanoTime();

        return (endTime - startTime) / 1_000_000;
    }

}
