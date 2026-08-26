package moonlight.api.timer;

public class TimerSystem {

    public static int second(int tick) {
        int divide = tick / 20;
        return divide % 60;
    }

    public static int minute(int tick) {
        int divide = tick / 20;
        return divide / 60;
    }

}
