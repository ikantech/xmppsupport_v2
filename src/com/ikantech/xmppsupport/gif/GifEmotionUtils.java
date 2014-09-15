package com.ikantech.xmppsupport.gif;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.widget.EditText;
import android.widget.TextView;

import com.ikantech.support.util.YiDeviceUtils;
import com.ikantech.support.util.YiLog;

public class GifEmotionUtils {
	private final int FACE_TEXT_MAXLENGHT = 7;

	private Context mContext;
	private Map<TextView, List<GifTextDrawable>> mGifMap;

	private int[] mEmotions;
	private String[] mEmotionDescs;

	private Callback mCallback;

	private BitmapDrawable mFace;

	// 存储正文中表情符号的位置
	class FacePos {
		int s;// 表情文字起始
		int e;// 表情文字结束点
		int i;// 该表情文字表示的表情的序号

		public FacePos(int s, int e, int i) {
			this.s = s;
			this.e = e;
			this.i = i;
		}

	}

	public GifEmotionUtils(Context context, int[] emotions,
			String[] emotionDescs, int res) {
		mContext = context;
		mEmotions = emotions;
		mEmotionDescs = emotionDescs;

		mCallback = null;
		mGifMap = new HashMap<TextView, List<GifTextDrawable>>();

		mFace = new BitmapDrawable(mContext.getResources().openRawResource(res));

		float faceH = mFace.getIntrinsicHeight();
		float faceW = mFace.getIntrinsicWidth();

		faceH = YiDeviceUtils.dip2px(mContext, faceH);
		faceW = YiDeviceUtils.dip2px(mContext, faceW);

		mFace.setBounds(0, 0, (int) faceW, (int) faceH);
	}

	public void setSpannableText(final TextView tv, final String content, final Handler handler) {
		synchronized (mGifMap) {
			List<GifTextDrawable> list = mGifMap.get(tv);
			if (list != null) {
				for (GifTextDrawable gifTextDrawable : list) {
					gifTextDrawable.stop();
				}
				mGifMap.remove(tv);
			}
		}

		final int selection = tv.getSelectionStart();
		tv.setText("");
		if (content.indexOf('[') > -1 && content.indexOf(']') > -1) {

			// 用以存储需替换的表情的位置
			final List<FacePos> faceList = new ArrayList<FacePos>();
			// 查找表情的位置
			for (int i = 0; i < content.length(); i++) {
				if (content.charAt(i) == '[') {

					for (int k = i; k < i + FACE_TEXT_MAXLENGHT; k++) {
						if (content.charAt(k) == ']') {
							YiLog.getInstance().i("aa %s",
									content.substring(i, k + 1));
							for (int j = 0; j < mEmotionDescs.length; j++) {
								if (mEmotionDescs[j].equals(content.substring(
										i, k + 1))) {
									// 保存需替换的表情文字的位置
									FacePos fp = new FacePos(i, k, j);
									faceList.add(fp);
								}
							}
							break;
						}
					}
				}
			}

			// 如果无表情
			if (faceList.size() == 0) {
				tv.post(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						tv.setText(content);
						if(tv instanceof EditText) {
							((EditText)tv).setSelection(selection);
						}
					}
				});
			} else {
				SpannableString ss = new SpannableString(content);
				for (int j = 0; j < faceList.size(); j++) {
					ImageSpan span = new ImageSpan(mFace,
							ImageSpan.ALIGN_BOTTOM);
					// 替换一个表情
					ss.setSpan(span, faceList.get(j).s,
							faceList.get(j).e + 1,
							Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
				tv.setText(ss);
				if(tv instanceof EditText) {
					((EditText)tv).setSelection(selection);
				}
				
				new Thread(new Runnable() {
					@Override
					public void run() {
						// TODO Auto-generated method stub
						// 如果有表情
						SpannableString ss = new SpannableString(content);

						final List<GifTextDrawable> gifList = new ArrayList<GifTextDrawable>();
						for (int j = 0; j < faceList.size(); j++) {
							// 开始新动画

							GifTextDrawable textDrawable = new GifTextDrawable(
									tv);

							GifOpenHelper gifOpenHelper = new GifOpenHelper();
							int status = gifOpenHelper.read(mContext
									.getResources().openRawResource(
											mEmotions[faceList.get(j).i]));
							BitmapDrawable bd = null;
							if (status == GifOpenHelper.STATUS_OK) {
								bd = new BitmapDrawable(gifOpenHelper
										.getImage());
								textDrawable.addFrame(bd,
										gifOpenHelper.getDelay(0));
								for (int i = 1; i < gifOpenHelper
										.getFrameCount(); i++) {
									textDrawable.addFrame(new BitmapDrawable(
											gifOpenHelper.nextBitmap()),
											gifOpenHelper.getDelay(i));
								}
							} else {
								try {
									bd = new BitmapDrawable(
											mContext.getResources()
													.openRawResource(
															mEmotions[faceList
																	.get(j).i]));
									textDrawable.addFrame(bd, 1000);
								} catch (Exception e) {
									// TODO: handle exception
								}
							}

							float faceH = bd.getIntrinsicHeight();
							float faceW = bd.getIntrinsicWidth();

							// 再转回px
							faceH = YiDeviceUtils.dip2px(mContext, faceH);
							faceW = YiDeviceUtils.dip2px(mContext, faceW);

							textDrawable.setBounds(0, 0, (int) faceW,
									(int) faceH);
							textDrawable.setOneShot(false);

							ImageSpan span = new ImageSpan(textDrawable,
									ImageSpan.ALIGN_BOTTOM);
							// 替换一个表情
							ss.setSpan(span, faceList.get(j).s,
									faceList.get(j).e + 1,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

							gifList.add(textDrawable);
						}

						synchronized (mGifMap) {
							mGifMap.put(tv, gifList);
						}

						final SpannableString ssForPost = ss;
						// 显示新的已经替换表情的text
						handler.post(new Runnable() {
							public void run() {
								// TODO 自动生成的方法存根
								tv.setText(ssForPost);
								if(tv instanceof EditText) {
									((EditText)tv).setSelection(selection);
								}
								for (int i = 0; i < gifList.size(); i++) {
									gifList.get(i).start();
								}
							}
						});
						if (mCallback != null) {
							mCallback.onGifEmotionUpdate();
						}

					}
				}).start();
			}
		} else {
			tv.setText(content);
		}
	}

	public void destory() {
		synchronized (mGifMap) {
			Set<TextView> keys = mGifMap.keySet();
			for (TextView textView : keys) {
				List<GifTextDrawable> list = mGifMap.get(textView);
				if (list != null) {
					for (GifTextDrawable gifTextDrawable : list) {
						gifTextDrawable.stop();
					}
				}
			}
			mGifMap.clear();
		}
	}

	public void setCallback(Callback mCallback) {
		this.mCallback = mCallback;
	}

	public interface Callback {
		void onGifEmotionUpdate();
	}
}
