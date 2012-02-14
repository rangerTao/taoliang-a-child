package com.duole.player;

import java.io.File;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.transform.ErrorListener;
import javax.xml.transform.TransformerException;

import android.R.integer;
import android.app.AlertDialog;
import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TimePicker;

import com.duole.Duole;
import com.duole.R;
import com.duole.activity.PlayerBaseActivity;
import com.duole.pojos.adapter.MusicItemAdapter;
import com.duole.utils.Constants;
import com.duole.utils.DuoleSysConfigUtils;
import com.duole.utils.DuoleUtils;
import com.duole.utils.XmlUtils;

public class MusicPlayerActivity extends PlayerBaseActivity implements
		OnFocusChangeListener, OnItemSelectedListener, OnClickListener,
		OnItemClickListener, OnCompletionListener {

	MediaPlayer mp;
	MediaPlayer mp2;
	RelativeLayout llMain;
	String url = "";
	int index;
	String type = "";
	Gallery gallery;
	public MusicPlayerActivity appref;
	private AnimationSet manimationSet;

	int homeCount = 0;

	static ProgressBar pbCountDown;
	DisplayMetrics dm;

	public boolean clicked = false;
	public static boolean isTopOfStack = false;
	public static boolean forceClose = false;
	boolean isExitDialogOn = false;

	Button btnPlay;

	EditText etPasswd;
	TimePicker tPicker;
	TextView tvMinute;

	AlertDialog adPasswd;

	TextView tvTip;
	AudioManager am;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.SetFullScreen();
		setContentView(R.layout.musicplayer);
		appref = this;
		
		topOfStack();

		btnPlay = (Button) findViewById(R.id.btnPlay);

		setBackground();

		Intent intent = getIntent();

		gallery = (Gallery) findViewById(R.id.musicGallery);

		gallery.setOnFocusChangeListener(this);

		gallery.setOnItemSelectedListener(this);

		gallery.setAdapter(new MusicItemAdapter(Constants.MusicList));

		gallery.setCallbackDuringFling(false);

		gallery.setOnItemClickListener(this);
		
		index = Integer.parseInt(intent.getStringExtra("index")) - 1;
		type = intent.getStringExtra("type");
		
		gallery.setSelection(index);

		if (Constants.MusicList.size() > 0) {
			setMusicData(index);
		}

		btnPlay.setOnClickListener(this);

//		mp.setOnCompletionListener(this);

		registerReceiver();

		initProgressBar();

		initDragListener();

		// startRestCountDown();
	}

	private void startRestCountDown() {
		if (!Duole.appref.restCountDown.isRunning()) {
			Duole.appref.restCountDown.resume();
		}
	}

	private void initDragListener() {
		
		tvTip = (TextView) findViewById(R.id.volumeTip);
		tvTip.setTextColor(Color.BLACK);
		tvTip.setTextSize(18f);
		

		dm = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(dm);

		am = (AudioManager) getSystemService(AUDIO_SERVICE);

		btnPlay.setOnTouchListener(new OnTouchListener() {

			int lastX, lastY;
			int disX = 0;
			boolean isClick = false;

			int maxV = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
			int perDegree = dm.widthPixels / maxV;
			int tempX = 0;
			
			public boolean onTouch(View v, MotionEvent event) {
				// TODO Auto-generated method stub
				switch (event.getAction()) {
				case MotionEvent.ACTION_DOWN:

					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					disX = lastX;
					isClick = false;
					
					break;
				case MotionEvent.ACTION_MOVE:
					int dx = (int) event.getRawX() - lastX;
					int dy = 0;

					tempX += dx;
					if (tempX >= perDegree) {
						am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
								AudioManager.ADJUST_RAISE, 2);
						tempX = 0;
					}
					if (tempX <= -perDegree) {
						am.adjustStreamVolume(AudioManager.STREAM_MUSIC,
								AudioManager.ADJUST_LOWER, 2);
						tempX = 0;
					}

					int left = v.getLeft() + dx;
					int top = v.getTop() + dy;
					int right = v.getRight() + dx;
					int bottom = v.getBottom() + dy;

					if (left < 0) {
						left = 0;
						right = left + v.getWidth();
					}

					if (right > dm.widthPixels) {
						right = dm.widthPixels;
						left = right - v.getWidth();
					}

					if (top < 0) {
						top = 0;
						bottom = top + v.getHeight();
					}

					if (bottom > dm.widthPixels) {
						bottom = dm.widthPixels;
						top = bottom - v.getHeight();
					}
					v.layout(left, top, right, bottom);

					lastX = (int) event.getRawX();
					lastY = (int) event.getRawY();
					v.postInvalidate();
					break;
				case MotionEvent.ACTION_UP:
					disX = Math.abs(lastX - disX);
					if (disX > 5) {
						isClick = true;
					}
					Log.d("TAG", "action up");
					
					tvTip.setVisibility(View.INVISIBLE);
					break;

				default:
					break;
				}
				return isClick;
			}
		});
	}

	private void playTipSound() {
		try {
			if (Constants.SCREEN_ON
					&& Duole.appref.restCountDown.getRemainMills() > 10
					&& mp2 == null) {
				File file = new File(Constants.CacheDir
						+ Constants.TIPSTARTNAME);
				if (file.exists()) {

					mp2 = new MediaPlayer();

					mp2.setDataSource(this, Uri.fromFile(file));

					mp2.prepare();
					mp2.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {

						public void onPrepared(MediaPlayer mp) {
							mp2.start();
						}
					});
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	/**
	 * Init the progress of progress bar.
	 */
	private void initProgressBar() {
		Duole.appref.gameCountDown.pause();

		pbCountDown = (ProgressBar) findViewById(R.id.pbRestTime);

		pbCountDown.setBackgroundColor(Color.RED);

		Duole.appref.restCountDown.setPb(pbCountDown);

		pbCountDown.setMax(Duole.appref.restCountDown.getTotalTime());

		if (type != null && type.equals("rest")) {

			pbCountDown.setVisibility(View.VISIBLE);

			Duole.appref.restCountDown.resume();

			playTipSound();

		}

	}

	private void registerReceiver() {
		IntentFilter intentFilter = new IntentFilter("com.duole.restime.out");
		registerReceiver(timeOutReceiver, intentFilter);
	}

	public void setBackground() {
		llMain = (RelativeLayout) findViewById(R.id.llMusicPlayer);

		if (!Constants.bgRestUrl.equals("")) {
			File bg = new File(Constants.CacheDir
					+ Constants.bgRestUrl.substring(Constants.bgRestUrl
							.lastIndexOf("/")));
			if (bg.exists()) {

				Bitmap temBitmap = BitmapFactory.decodeFile(bg
						.getAbsolutePath());
				if (temBitmap != null) {
					llMain.setBackgroundDrawable(Drawable.createFromPath(bg
							.getAbsolutePath()));
				} else {
					llMain.setBackgroundResource(R.drawable.bg86);
					bg.delete();
				}

			} else {
				llMain.setBackgroundResource(R.drawable.musicbg);
				try {
					DuoleUtils.downloadPicSingle(new URL(Constants.Duole
							+ Constants.bgRestUrl), bg);
					llMain.setBackgroundDrawable(Drawable.createFromPath(bg
							.getAbsolutePath()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	// deal with Dupilate touch event ===============> start..
	@Override
	public boolean onTouchEvent(MotionEvent event) {

		if (event.getPointerCount() == 2 && Constants.SLEEP_TIME) {

			if ((event.getEventTime() - event.getDownTime()) > 10 * 1000) {
				dealWithTwoPointerClick(dm, event);
			}
		}

		return super.onTouchEvent(event);
	}

	private void dealWithTwoPointerClick(DisplayMetrics dm, MotionEvent event) {

		float rawXA = event.getX(0);
		float rawXB = event.getX(1);
		float rawYA = event.getY(0);
		float rawYB = event.getY(1);

		if (((rawXA < 120.0 || rawXA > dm.widthPixels - 120.0) && rawYA < 120.0)
				&& ((rawXB < 120.0 || rawXB > dm.widthPixels - 120.0) && rawYB < 120.0)
				&& !isExitDialogOn) {
			showExitClickDialog();
		}

	}

	private void showExitClickDialog() {

		isExitDialogOn = true;

		tvTip.setVisibility(View.INVISIBLE);

		etPasswd = new EditText(appref);
		etPasswd.setSingleLine(true);
		etPasswd.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD
				| InputType.TYPE_CLASS_TEXT);

		adPasswd = new AlertDialog.Builder(appref)
				.setTitle(R.string.input_password)
				.setView(etPasswd)
				.setPositiveButton(R.string.btnPositive, positiveClickListener)
				.setNegativeButton(R.string.btnNegative,
						new DialogInterface.OnClickListener() {

							public void onClick(DialogInterface dialog,
									int which) {
								isExitDialogOn = false;
							}
						}).create();

		adPasswd.show();
	}

	DialogInterface.OnClickListener positiveClickListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {

			String pass = etPasswd.getText().toString();
			String system_pass = XmlUtils.readNodeValue(
					Constants.SystemConfigFile, Constants.XML_PASSWORD);
			if (system_pass.equals("")) {
				system_pass = Constants.defaultPasswd;
			}
			if (pass.equals(system_pass)) {
				tPicker = new TimePicker(appref);
				tPicker.setCurrentHour(0);
				tPicker.setCurrentMinute(30);
				tPicker.setIs24HourView(true);
				
				View minutePicker = LayoutInflater.from(appref).inflate(R.layout.sleeptimexit, null);
				tvMinute = (TextView) minutePicker.findViewById(R.id.tvMinute);
				Button btnAdd = (Button) minutePicker.findViewById(R.id.btnMinuteAdd);
				Button btnDe = (Button) minutePicker.findViewById(R.id.btnMinuteDe);
				
				btnAdd.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						int min = Integer.parseInt(tvMinute.getText().toString());
						if(min < 56){
							tvMinute.setText((min + 5) + "");
						}
					}
				});
				
				btnDe.setOnClickListener(new OnClickListener() {
					
					public void onClick(View v) {
						int min = Integer.parseInt(tvMinute.getText().toString());
						if(min > 9){
							tvMinute.setText((min - 5) +"");
						}
					}
				});

				new AlertDialog.Builder(appref)
						.setView(minutePicker)
						.setTitle(R.string.sleep_delay_title)
						.setPositiveButton(R.string.btnPositive,
								delayClickListener)
						.setNegativeButton(R.string.btnNegative,
								new DialogInterface.OnClickListener() {

									public void onClick(DialogInterface dialog,
											int which) {
										isExitDialogOn = false;
									}
								}).create().show();
			} else {
				TextView wrPass = new TextView(appref);
				wrPass.setText(R.string.passwrod_wrong_full);
				new AlertDialog.Builder(appref).setView(wrPass)
						.setTitle(R.string.password_wrong)
						.setNegativeButton(R.string.btnClose, null).create()
						.show();
				etPasswd.setText("");

				isExitDialogOn = false;
			}
		}
	};

	DialogInterface.OnClickListener delayClickListener = new DialogInterface.OnClickListener() {

		public void onClick(DialogInterface dialog, int which) {
			int min = Integer.parseInt(tvMinute.getText().toString());
			long mills = min * 60 * 1000;
			
			Log.d("TAG", mills + " delay");
			
			mHandler.postDelayed(new Runnable() {

				public void run() {
					Constants.sleepTimeDelayed = false;
				}
			}, mills);

			Constants.sleepTimeDelayed = true;
			// Delay the sleep time.
			Constants.SLEEP_TIME = false;

			DuoleSysConfigUtils.enableWifi(appref);

			Duole.appref.sendBroadcast(new Intent("com.duole.restime.out"));

			Duole.appref.initCountDownTimer();
			Constants.musicPlayerIsRunning = false;

		}
	};

	// deal with Dupilate touch event ===============> end..

	public void setMusicData(int index) {
		String filename = Constants.MusicList.get(index).getUrl();

		if (filename.startsWith("http:")) {
		} else {
			filename = filename.substring(filename.lastIndexOf("/"));
		}

		if (filename.startsWith("http")) {
			url = filename;
		} else {
			url = Constants.CacheDir + Constants.RES_AUDIO + filename;
		}

		Log.d("TAG","set data source");
		mHandler.post(new Runnable() {
			
			public void run() {
				try {
//					mp.setDataSource(this, Uri.fromFile(new File(url)));
					
					mp = new MediaPlayer();
					mp.setOnErrorListener(mpError);
					
					mp.setDataSource(url);
					mp.prepare();
					
				} catch (Exception e) {
					e.printStackTrace();
				}
				
			}
		});
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_HOME:

			musicControl();
			if (homeCount < 3 && mp2 != null) {
				try {
					mp2.seekTo(0);
					mp2.start();
				} catch (Exception e) {
					e.printStackTrace();
				}

				homeCount++;
			}
			break;
		}
		return true;
	}

	@Override
	protected void onDestroy() {
		
		if(mp != null && mp.isPlaying()){
			mp.stop();
		}
		
		unregisterReceiver(timeOutReceiver);
		super.onDestroy();
	}

	public void onFocusChange(View view, boolean value) {

	}

	public void onItemSelected(AdapterView<?> arg0, View view, int position,
			long arg3) {
		index = position;
		if (position != Constants.MusicList.size()) {
			AnimationSet animationSet = new AnimationSet(true);
			if (manimationSet != null && manimationSet != animationSet) {
				ScaleAnimation scaleAnimation = new ScaleAnimation(1.0f, 0.85f,
						1.0f, 0.85f, Animation.RELATIVE_TO_SELF, 1f,
						Animation.RELATIVE_TO_SELF, 1f);
				manimationSet.addAnimation(scaleAnimation);
				manimationSet.setFillAfter(true);
				view.startAnimation(manimationSet);
			}
			ScaleAnimation scaleAnimation = new ScaleAnimation(1, 1.2f, 1,
					1.2f, Animation.RELATIVE_TO_SELF, 1f,
					Animation.RELATIVE_TO_SELF, 1f);
			scaleAnimation.setDuration(1000);
			animationSet.addAnimation(scaleAnimation);
			animationSet.setFillAfter(true);
			view.startAnimation(animationSet);
			manimationSet = animationSet;

		} else {
			if (null != manimationSet)
				manimationSet.setFillAfter(false);
		}

		this.mHandler.post(new Runnable() {

			public void run() {
				if(mp != null && mp.isPlaying()){
					mp.stop();
					mp.release();
				}

				mp = new MediaPlayer();
				
				mp.setOnErrorListener(mpError);

				setMusicData(index);

				if (clicked) {
					playMusic();
				}
			}

		});

	}

	public void onNothingSelected(AdapterView<?> arg0) {
	}

	BroadcastReceiver timeOutReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if (!Constants.SLEEP_TIME) {
				forceClose = true;
				Constants.ENTIME_OUT = false;
				appref.finish();
				
			}
		}

	};

	public void onClick(View v) {

		musicControl();
	}

	private void musicControl() {

		if (mp.isPlaying()) {
			pauseMusic();
		} else {
			playMusic();
		}

	}

	private void playMusic() {
		btnPlay.setBackgroundResource(R.drawable.pause);
		
		try{
			
			mHandler.post(new Runnable() {
				
				public void run() {
					mp.start();
				}
			});
			
			if (mp2 != null) {
				mp2.release();
			}
		}catch (Exception e) {
			// TODO: handle exception
		}
	}

	private void pauseMusic() {
		btnPlay.setBackgroundResource(R.drawable.play);
		mp.pause();
	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		clicked = true;
	}

	public void onCompletion(MediaPlayer arg0) {
		btnPlay.setBackgroundResource(R.drawable.play);
		mp.seekTo(0);
	}

	@Override
	protected void onPause() {
		Log.e("TAG", "music player on pause");
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);

		if (pm.isScreenOn()) {
			Constants.musicPlayerIsRunning = false;
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		topOfStack();
		super.onResume();
	}

	@Override
	protected void onStop() {
		
		notTopOfStack();
		
		if(!Constants.ENTIME_OUT && !isTopOfStack() && !forceClose){

			Intent intent1 = new Intent(Duole.appref,
					MusicPlayerActivity.class);
			intent1.putExtra("index", "1");
			Duole.appref.startActivity(intent1);
		}
		
		super.onStop();
	}

	public void topOfStack() {
		isTopOfStack = true;
	}

	public void notTopOfStack() {
		isTopOfStack = false;
	}

	public static boolean isTopOfStack() {
		return isTopOfStack;
	}
	
	OnErrorListener mpError = new OnErrorListener() {
		
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Log.e("TAG", "mp error.What : " + what);
//			mp.release();
			mp.reset();
			return false;
		}
	};
	
	public static void hidePBAndStopCountDown(){
		pbCountDown.setVisibility(View.GONE);
		Duole.appref.restCountDown.stop();
		Duole.appref.restCountDown.seek(0);
	}
}
