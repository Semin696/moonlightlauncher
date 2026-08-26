package moonlight.api.timer.timers;

import moonlight.api.timer.ITimer;

public class SimpleTimer implements ITimer {

    private long start;
    private long duration;

    private boolean isWorking;

    @Override
    public void run() {
        this.isWorking = true;
        this.start = System.currentTimeMillis();
    }

    @Override
    public void stop() {
        this.isWorking = false;
    }

    public boolean hasFinished() {
        return this.isWorking && (System.currentTimeMillis() - this.start >= this.duration);
    }

    public SimpleTimer setDuration(long duration) {
        this.duration = duration;
        return this;
    }

}
