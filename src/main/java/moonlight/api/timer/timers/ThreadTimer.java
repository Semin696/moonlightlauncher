package moonlight.api.timer.timers;

import moonlight.api.timer.ITimer;

public class ThreadTimer implements ITimer {

    private long duration;
    private boolean isWorking;

    public ThreadTimer() {
        this.duration = 1000;
        this.isWorking = true;
    }

    @Override
    public void run(Runnable runnable) {
        if(!this.isWorking) return;

        new Thread(() -> {
            try {
                Thread.sleep(this.duration);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }).start();

        runnable.run();
        stop();
    }

    @Override
    public void stop() {
        this.isWorking = false;
    }

    public void setDuration(long second) {
        this.duration = (second * 1000);
    }

    public long getDuration() {
        return duration;
    }

}
