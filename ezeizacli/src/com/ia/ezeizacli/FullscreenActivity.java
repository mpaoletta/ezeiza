package com.ia.ezeizacli;

import java.io.IOException;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.ia.ezeizacli.util.SystemUiHider;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends Activity implements Barrel.BarrelAnimation {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 20000;

    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;

    /**
     * The flags to pass to {@link SystemUiHider#getInstance}.
     */
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;

    /**
     * The instance of the {@link SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    
    private Model model = new Model(new AnimationHandler());
    private ViewFlipper mTopViewFlipper = null;
    private ViewFlipper mBottomViewFlipper = null;
    
    private EditText editX = null;
    private EditText editY = null;
    private EditText editIp = null;
    
    
	private MediaPlayer player;

    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        try {
        	AssetFileDescriptor afd = getAssets().openFd("audio.wav");
        	player = new MediaPlayer();
        	player.setDataSource(afd.getFileDescriptor());
        	player.prepare();
        }
        catch(IOException e) {
        	System.out.println("Error cargando archivo de audio");
        	e.printStackTrace();
        }
        
        
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        mTopViewFlipper = (ViewFlipper) findViewById(R.id.viewflipperTop);
        mBottomViewFlipper = (ViewFlipper) findViewById(R.id.viewflipperBottom);
        editX = (EditText)findViewById(R.id.editX);
        editY = (EditText)findViewById(R.id.editY);
        editIp = (EditText)findViewById(R.id.editIP);
        
        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, mTopViewFlipper, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(boolean visible) {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                            // If the ViewPropertyAnimator API is available
                            // (Honeycomb MR2 and later), use it to animate the
                            // in-layout UI controls at the bottom of the
                            // screen.
                            if (mControlsHeight == 0) {
                                mControlsHeight = controlsView.getHeight();
                            }
                            if (mShortAnimTime == 0) {
                                mShortAnimTime = getResources().getInteger(
                                        android.R.integer.config_shortAnimTime);
                            }
                            controlsView.animate()
                                    .translationY(visible ? 0 : mControlsHeight)
                                    .setDuration(mShortAnimTime);
                        } else {
                            // If the ViewPropertyAnimator APIs aren't
                            // available, simply show or hide the in-layout UI
                            // controls.
                            controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                        }

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        
        OnClickListener acceptListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TOGGLE_ON_CLICK) {
                    mSystemUiHider.toggle();
                } else {
                    mSystemUiHider.show();
                }
                
                model.setIp(editIp.getText().toString());
                model.setX(Integer.valueOf(editX.getText().toString()));
                model.setY(Integer.valueOf(editY.getText().toString()));                
                
                model.loadConf();
                
                Barrel barrel = model.getBarrel(FullscreenActivity.this);

                fillViewFlipper(barrel.getTopBitmapList(), mTopViewFlipper);
                fillViewFlipper(barrel.getBottomBitmapList(), mBottomViewFlipper);
                
                model.startAnimation();
                
                hideKeyboard();
            }
        };
        
        // Set up the user interaction to manually show or hide the system UI.
        mTopViewFlipper.setOnClickListener(acceptListener);

        findViewById(R.id.accept_button).setOnClickListener(acceptListener);
        
        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        editX.setOnTouchListener(mDelayHideTouchListener);
        editX.setText(String.valueOf(model.getX()));
        editY.setOnTouchListener(mDelayHideTouchListener);
        editY.setText(String.valueOf(model.getY()));
        editIp.setOnTouchListener(mDelayHideTouchListener);
        editIp.setText(model.getIp());        
        
    }

    private void fillViewFlipper(List<Bitmap> bitmaps, ViewFlipper viewFlipper) {
        viewFlipper.removeAllViews();
        for(Bitmap image : bitmaps) {
        	ImageView img = new ImageView(viewFlipper.getContext());
        	img.setImageBitmap(image);
//        	*img.setTag(files[i].getPath())*
        	img.setScaleType(ImageView.ScaleType.FIT_XY);
        	viewFlipper.addView(img, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,ViewGroup.LayoutParams.FILL_PARENT));
        }    	
    }
    
    private void hideKeyboard() {   
        // Check if no view has focus:
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }    
    
    
    public static void hide_keyboard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if(view == null) {
            view = new View(activity);
        }
        inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }    

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }


    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
    
    class AnimationHandler extends Handler {

		@Override
		public void handleMessage(Message msg) {
			long ts = msg.getData().getLong("timestamp");
			long elapsed = System.currentTimeMillis() - ts;
			System.out.println("handleMessage " + msg.getData().getLong("id") + " ts: " + ts + ". Elapsed: " + elapsed);
			mTopViewFlipper.showNext();
			try {
				Thread.sleep(100l);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mBottomViewFlipper.showNext();
		}
		
		private void playAudioTransition() {
			 player.start();			
		}
    	
    }

	@Override
	public void showNext() {
		mTopViewFlipper.showNext();
		try {
			Thread.sleep(100l);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		mBottomViewFlipper.showNext();
	}

    
}
