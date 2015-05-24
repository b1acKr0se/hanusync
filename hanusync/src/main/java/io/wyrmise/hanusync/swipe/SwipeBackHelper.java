package io.wyrmise.hanusync.swipe;

import android.app.Activity;
import android.content.Context;
import android.os.Vibrator;
import me.imid.swipebacklayout.lib.SwipeBackLayout.SwipeListener;
import me.imid.swipebacklayout.lib.app.SwipeBackActivityHelper;

/**
 * Created by Demise on 5/24/2015.
 */

public class SwipeBackHelper extends SwipeBackActivityHelper {
    private static final int VIBRATE_DURATION = 20;
    private SwipeListener mSwipeListener;
    private boolean mEnabledVibrator;
    SwipeListener mOnSwipe = new SwipeListener() {

        @Override
        public void onScrollStateChange(int state, float scrollPercent) {
            if(mSwipeListener != null)
                mSwipeListener.onScrollStateChange(state, scrollPercent);
        }

        @Override
        public void onScrollOverThreshold() {
            vibrate(VIBRATE_DURATION);
            if(mSwipeListener != null)
                mSwipeListener.onScrollOverThreshold();
        }

        @Override
        public void onEdgeTouch(int edgeFlag) {
            vibrate(VIBRATE_DURATION);
            if(mSwipeListener != null)
                mSwipeListener.onEdgeTouch(edgeFlag);
        }
    };
    private Vibrator mVibrator;

    public SwipeBackHelper(Activity activity) {
        this(activity, false);
    }

    public SwipeBackHelper(Activity activity, boolean enabledVibrator){
        super(activity);
        mEnabledVibrator = enabledVibrator;
        try {
            mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
        } catch (Exception e) {
        }
    }
    @Override
    public void onPostCreate() {
        super.onPostCreate();
        getSwipeBackLayout().addSwipeListener(mOnSwipe);
        getSwipeBackLayout().setScrollThresHold(0.4f);
    }
    public void setVibratorEnabled(boolean enabledVibrator){
        mEnabledVibrator = enabledVibrator;
    }

    private void vibrate(int duration) {
        if(mVibrator != null && mEnabledVibrator){
            long[] pattern = { 0, duration };
            mVibrator.vibrate(pattern, -1);
        }
    }

    public void setSwipeListener(SwipeListener l) {
        mSwipeListener = l;
    }

}