package com.ikantech.xmppsupport.media;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaRecorder;
import android.os.Environment;

import com.ikantech.support.util.YiBase64;
import com.ikantech.support.util.YiFileUtils;
import com.ikantech.support.util.YiLog;

public class AudioRecorder {
	public static String mStorePath = "yiim/";

	private static final String RECORDER_FILE = "record.ik";
	private MediaRecorder mMediaRecorder;

	public synchronized void startRecorder() throws Exception {
		if (mMediaRecorder == null) {
			try {
				mMediaRecorder = new MediaRecorder();
				mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mMediaRecorder
						.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				File file = Environment.getExternalStorageDirectory();
				if (!file.exists()) {
					throw new Exception("err_no_store_device");
				}
				file = new File(YiFileUtils.getStorePath() + mStorePath);
				if (!file.exists()) {
					file.mkdirs();
				}
				file = new File(YiFileUtils.getStorePath() + mStorePath
						+ RECORDER_FILE);

				if (!file.exists()) {
					file.createNewFile();
				}
				YiLog.getInstance()
						.i("record file: %s", file.getAbsoluteFile());
				mMediaRecorder.setOutputFile(YiFileUtils.getStorePath()
						+ mStorePath + RECORDER_FILE);
				mMediaRecorder
						.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
			} catch (Exception e) {
				throw new Exception("err_unknown");
			}
		}
		try {
			mMediaRecorder.prepare();
		} catch (IOException e) {
			throw new Exception("err_unknown");
		}
		mMediaRecorder.start();
	}

	public synchronized String getRecordedResource() throws Exception {
		File file = new File(YiFileUtils.getStorePath() + mStorePath
				+ RECORDER_FILE);
		if (file.exists()) {
			BufferedInputStream bis = null;
			try {
				bis = new BufferedInputStream(new FileInputStream(file));
				int bytes = (int) file.length();
				byte[] buffer = new byte[bytes];
				int readBytes = bis.read(buffer);
				if (readBytes != buffer.length) {
					throw new IOException("Entire file not read");
				}
				return new String(YiBase64.encode(buffer), "ASCII");
			} catch (Exception ex) {
				throw new Exception("err_unknown");
			} finally {
				if (bis != null) {
					try {
						bis.close();
						bis = null;
					} catch (Exception e) {
					}
				}
			}
		}
		throw new Exception("err_unknown");
	}

	public synchronized void release() throws Exception {
		stopRecording();
	}

	public synchronized MediaRecorder getMediaRecorder() {
		return mMediaRecorder;
	}

	public String getAudioFilePath() {
		return YiFileUtils.getStorePath() + mStorePath + RECORDER_FILE;
	}

	public synchronized void stopRecording() throws Exception {
		if (mMediaRecorder != null) {
			try {
				mMediaRecorder.stop();
				mMediaRecorder.release();
				mMediaRecorder = null;
			} catch (Exception e) {
				throw new Exception("err_unknown");
			}
		}
	}
}
