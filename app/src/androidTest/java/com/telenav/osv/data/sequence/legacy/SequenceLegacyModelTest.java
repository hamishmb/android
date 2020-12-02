package com.telenav.osv.data.sequence.legacy;

import android.annotation.SuppressLint;

import com.telenav.osv.data.score.legacy.ScoreHistoryLegacyTest;
import com.telenav.osv.item.Polyline;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author horatiuf
 */
public abstract class SequenceLegacyModelTest {

    private static final String TAG = "Sequence";

    double mTotalLength;

    int mOriginalFrameCount;

    int mFrameCount;

    String mThumbLink = "";

    String mAddress = "";

    Polyline mPolyline;

    int mId = -1;

    Date mDate;

//    SKCoordinate mLocation = new SKCoordinate();

    boolean mHasObd = false;

    String mAppVersion = "";

    double value;

    @SuppressLint("UseSparseArrays")
    Map<Integer, ScoreHistoryLegacyTest> mScoreHistory = new HashMap<>();

    String mPlatform = "";

    String mPlatformVersion = "";

    String mCurrency = "";

    private String mServerStatus = "";

    private boolean mIsPublic;

    private int mSeekToFrame = 0;

    @Override
    public String toString() {
        return "Sequence (id " + mId + " images " + mFrameCount + " from " + mOriginalFrameCount + " and " + value + " Points" + ")";
    }

    public double getTotalLength() {
        return mTotalLength;
    }

    public void setTotalLength(double totalLength) {
        this.mTotalLength = totalLength;
    }

    public int getOriginalFrameCount() {
        return mOriginalFrameCount;
    }

    public int getFrameCount() {
        return mFrameCount;
    }

    public void setFrameCount(int count) {
        mFrameCount = count;
    }

    public String getThumbLink() {
        return mThumbLink;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public Polyline getPolyline() {
        return mPolyline;
    }

    public int getId() {
        return mId;
    }

    public Date getDate() {
        return mDate;
    }

    //TODO: Fix location test model
//    public SKCoordinate getLocation() {
//        return mLocation;
//    }
//
//    public void setLocation(SKCoordinate mLocation) {
//        this.mLocation = mLocation;
//    }

    public boolean hasObd() {
        return mHasObd;
    }

    public String getAppVersion() {
        return mAppVersion;
    }

    public double getValue() {
        return value;
    }

    public Map<Integer, ScoreHistoryLegacyTest> getScoreHistories() {
        return mScoreHistory;
    }

    public String getPlatform() {
        return mPlatform;
    }

    public String getPlatformVersion() {
        return mPlatformVersion;
    }

    public String getServerStatus() {
        return mServerStatus;
    }

    public void setServerStatus(String mServerStatus) {
        this.mServerStatus = mServerStatus;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public boolean isPublic() {
        return mIsPublic;
    }

    public void setPublic(boolean isPublic) {
        this.mIsPublic = isPublic;
    }

    public int getRequestedFrameIndex() {
        return mSeekToFrame;
    }

    public void setRequestedFrameIndex(int seekToFrame) {
        this.mSeekToFrame = seekToFrame;
    }

    public int getDistance() {
        return (int) mTotalLength;
    }

    public void setScoreHistory(Map<Integer, ScoreHistoryLegacyTest> scoreHistory) {
        this.mScoreHistory = scoreHistory;
    }

    public void setHasObd(boolean hasObd) {
        this.mHasObd = hasObd;
    }

    public abstract boolean isOnline();

    public int getScore() {
        return (int) getValue();
    }

    public void setScore(double score) {
        this.value = score;
    }

    public abstract boolean isSafe();
}
