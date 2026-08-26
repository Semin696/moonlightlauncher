package moonlight.api.math;

public class MathSystem {

    public static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static double easeBoth(float t) {
        return t < 0.5 ? 2 * t * t : 1 - Math.pow(-2 * t + 2, 2) / 2;
    }

    public static double easeIn(float t) {
        return t * t;
    }

    public static double easeOut(float t) {
        return 1 - Math.pow(1 - t, 2);
    }

}
