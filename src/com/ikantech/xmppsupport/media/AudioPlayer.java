package com.ikantech.xmppsupport.media;

import java.io.File;
import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;

public class AudioPlayer {
	private MediaPlayer mMediaPlayer;
	private OnCompletionListener mCompletionListener;

	public synchronized void startPlaying(String filename) throws Exception {
		if (mMediaPlayer != null) {
			stopPlaying();
		}

		try {
			mMediaPlayer = new MediaPlayer();
			if (mCompletionListener != null) {
				mMediaPlayer.setOnCompletionListener(mCompletionListener);
			}
			File file = new File(filename);
			if (!file.exists()) {
				throw new Exception("err_unknown");
			}
			mMediaPlayer.setDataSource(filename);
			mMediaPlayer.prepare();
			mMediaPlayer.start();
		} catch (IOException e) {
			throw new Exception("err_unknown");
		}
	}

	public synchronized MediaPlayer getMediaPlayer() {
		return mMediaPlayer;
	}

	public synchronized void release() {
		stopPlaying();
	}

	public synchronized void setOnCompletionListener(
			OnCompletionListener listener) {
		mCompletionListener = listener;
	}

	public synchronized void stopPlaying() {
		if (mMediaPlayer != null) {
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}
}
