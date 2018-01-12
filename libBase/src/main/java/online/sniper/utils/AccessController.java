package online.sniper.utils;

/**
 * Created by wangpeihe on 2017/11/2.
 */
public class AccessController {
    public static final int STATUS_IDLE = 0;
    public static final int STATUS_LOCK = 1;

    private final long mInterval;
    private final boolean isLockMode;
    private long mLastTime = 0;
    private int mStatus = STATUS_IDLE;

    public AccessController() {
        this(1000, false);
    }

    public AccessController(boolean isLockMode) {
        this(1000, isLockMode);
    }

    public AccessController(long interval, boolean isLockMode) {
        mInterval = interval;
        this.isLockMode = isLockMode;
    }

    public long getmInterval() {
        return mInterval;
    }

    public boolean isLockMode() {
        return isLockMode;
    }

    public synchronized boolean canAccess() {
        if (isLockMode) {
            if (mStatus == STATUS_IDLE) {
                return System.currentTimeMillis() - mLastTime >= mInterval;
            } else {
                return false;
            }
        } else {
            return System.currentTimeMillis() - mLastTime >= mInterval;
        }
    }

    public synchronized void access() {
        if (!isLockMode) {
            mLastTime = System.currentTimeMillis();
        }
    }

    public synchronized boolean judgeAndAccess() {
        boolean canAccess = canAccess();
        if (canAccess) {
            access();
        }
        return canAccess;
    }

    public synchronized void lockAccess() {
        if (isLockMode) {
            mStatus = STATUS_LOCK;
            mLastTime = System.currentTimeMillis();
        }
    }

    public synchronized void unlockAccess() {
        if (isLockMode) {
            mStatus = STATUS_IDLE;
            mLastTime = System.currentTimeMillis();
        }
    }

    public synchronized boolean isLocked() {
        if (isLockMode) {
            return mStatus == STATUS_LOCK;
        } else {
            return false;
        }
    }

    public synchronized boolean judgeAndLock() {
        boolean canAccess = canAccess();
        if (canAccess) {
            lockAccess();
        }
        return canAccess;
    }
}
