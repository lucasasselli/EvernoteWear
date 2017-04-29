package com.lucasasselli.common;

import android.os.CountDownTimer;

/**
 * Created by Luca Sasselli on 14/02/2016.
 */
public class TimeoutTimer extends CountDownTimer {

    TimeoutListener timeoutListener;

    private final static int DEFAULT_INTERVALL = 1000;

    public TimeoutTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    public TimeoutTimer(long millisInFuture) {
        super(millisInFuture, DEFAULT_INTERVALL);
    }

    public void setTimeoutListener(TimeoutListener timeoutListener){
        this.timeoutListener = timeoutListener;
    }

    @Override
    public void onFinish() {
        timeoutListener.onTimeout();
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }

    public interface TimeoutListener{
        void onTimeout();
    }
}
