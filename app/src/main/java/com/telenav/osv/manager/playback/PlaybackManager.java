package com.telenav.osv.manager.playback;

import java.util.ArrayList;
import android.location.Location;
import android.view.View;
import android.widget.SeekBar;
import com.telenav.osv.activity.OSVActivity;
import com.telenav.osv.application.ApplicationPreferences;
import com.telenav.osv.data.frame.datasource.local.FrameLocalDataSource;
import com.telenav.osv.data.sequence.model.LocalSequence;
import com.telenav.osv.data.sequence.model.Sequence;
import com.telenav.osv.data.sequence.model.details.compression.SequenceDetailsCompressionJpeg;
import com.telenav.osv.data.video.datasource.VideoLocalDataSource;

/**
 * abstract playback manager
 * Created by Kalman on 27/07/16.
 */
public abstract class PlaybackManager {

    public static PlaybackManager get(OSVActivity activity, Sequence sequence, FrameLocalDataSource frameLocalDataSource, VideoLocalDataSource videoLocalDataSource, ApplicationPreferences appPrefs) {
        if (sequence.getType() == Sequence.SequenceTypes.LOCAL) {
            LocalSequence localSequence = (LocalSequence) sequence;
            return sequence.getCompressionDetails() instanceof SequenceDetailsCompressionJpeg ?
                    new SafePlaybackManager(activity, localSequence, frameLocalDataSource, appPrefs) :
                    new VideoPlayerManager(activity, localSequence, videoLocalDataSource);
        } else {
            return new OnlinePlaybackManager(activity, sequence, appPrefs);
        }
    }

    public abstract View getSurface();

    public abstract void setSurface(View surface);

    public abstract void prepare();

    public abstract void next();

    public abstract void previous();

    public abstract void play();

    public abstract void pause();

    public abstract void stop();

    public abstract void fastForward();

    public abstract void fastBackward();

    public abstract boolean isPlaying();

    public abstract void setSeekBar(SeekBar seekBar);

    public abstract int getLength();

    public abstract void destroy();

    public abstract void addPlaybackListener(PlaybackListener playbackListener);

    public abstract void removePlaybackListener(PlaybackListener playbackListener);

    public abstract boolean isSafe();

    public abstract Sequence getSequence();

    public abstract ArrayList<Location> getTrack();

    public abstract void onSizeChanged();

    public interface PlaybackListener {

        void onPlaying();

        void onPaused();

        void onStopped();

        void onPrepared();

        void onProgressChanged(int index);

        void onExit();

        default void onWaiting(boolean isWaitingMode) {}
    }
}
