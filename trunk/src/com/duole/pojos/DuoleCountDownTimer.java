package com.duole.pojos;

import android.os.Handler;
import android.os.Message;

public abstract class DuoleCountDownTimer {
	
	private final long mCountdownInterval;  
    private long mTotalTime;  
    private long mRemainTime;  
      
    public DuoleCountDownTimer(long millisInFuture, long countDownInterval) {  
        mTotalTime = millisInFuture;  
        mCountdownInterval = countDownInterval;  
        mRemainTime = millisInFuture;  
    }  
    public final void seek(int value) {  
        synchronized (DuoleCountDownTimer.this) {  
            mRemainTime = ((100 - value) * mTotalTime) / 100;  
        }  
    }  
      
    public void setTotalTime(long total){
    
    	this.mTotalTime = total;
    	
    }
    
    
    public final void cancel() {  
        mHandler.removeMessages(MSG_RUN);  
        mHandler.removeMessages(MSG_PAUSE);  
    }  
    public final void resume() {  
        mHandler.removeMessages(MSG_PAUSE);  
        mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_RUN));  
    }  
    public final void pause() {  
        mHandler.removeMessages(MSG_RUN);  
        mHandler.sendMessageAtFrontOfQueue(mHandler.obtainMessage(MSG_PAUSE));  
    }  
      
    public synchronized final DuoleCountDownTimer start() {  
        if (mRemainTime <= 0) {  
            onFinish();  
            return this;  
        }  
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_RUN),  
                mCountdownInterval);  
        return this;  
    }  
    public abstract void onTick(long millisUntilFinished, int percent);  
      
    public abstract void onFinish();  
    private static final int MSG_RUN = 1;  
    private static final int MSG_PAUSE = 2;  
    private Handler mHandler = new Handler() {  
        @Override  
        public void handleMessage(Message msg) {  
            synchronized (DuoleCountDownTimer.this) {  
                if (msg.what == MSG_RUN) {  
                    mRemainTime = mRemainTime - mCountdownInterval;  
                    if (mRemainTime <= 0) {  
                        onFinish();  
                    } else if (mRemainTime < mCountdownInterval) {  
                        sendMessageDelayed(obtainMessage(MSG_RUN), mRemainTime);  
                    } else {  
                        onTick(mRemainTime, new Long(100  
                                * (mTotalTime - mRemainTime) / mTotalTime)  
                                .intValue());  
                      
                        sendMessageDelayed(obtainMessage(MSG_RUN),  
                                mCountdownInterval);  
                    }  
                } else if (msg.what == MSG_PAUSE) {  
                }  
            }  
        }  
    };  
	
}
