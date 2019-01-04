/*===============================================================================
Copyright (c) 2016 PTC Inc. All Rights Reserved.


Copyright (c) 2012-2014 Qualcomm Connected Experiences, Inc. All Rights Reserved.

Vuforia is a trademark of PTC Inc., registered in the United States and other 
countries.
===============================================================================*/

package com.vuforia.samples.VuforiaSamples.app.ObjectRecognition;

import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.vuforia.CameraDevice;
import com.vuforia.DataSet;
import com.vuforia.ObjectTracker;
import com.vuforia.State;
import com.vuforia.STORAGE_TYPE;
import com.vuforia.Trackable;
import com.vuforia.Tracker;
import com.vuforia.TrackerManager;
import com.vuforia.Vuforia;
import com.vuforia.samples.SampleApplication.SampleApplicationControl;
import com.vuforia.samples.SampleApplication.SampleApplicationException;
import com.vuforia.samples.SampleApplication.SampleApplicationSession;
import com.vuforia.samples.SampleApplication.utils.LoadingDialogHandler;
import com.vuforia.samples.SampleApplication.utils.SampleApplicationGLView;
import com.vuforia.samples.SampleApplication.utils.Texture;
import com.vuforia.samples.VuforiaSamples.R;


public class ObjectTargets extends Activity implements SampleApplicationControl
{
    private static final String LOGTAG = "ObjectRecognition";
    
    SampleApplicationSession vuforiaAppSession;
    
    private DataSet mCurrentDataset;
    
    // Our OpenGL view:
    private SampleApplicationGLView mGlView;
    
    // Our renderer:
    private ObjectTargetRenderer mRenderer;
    
    // The textures we will use for rendering:
    private Vector<Texture> mTextures;
    
    private GestureDetector mGestureDetector;
    
    private boolean mFlash = false;
    private boolean mExtendedTracking = false;
    
    private View mFlashOptionView;
    
    private RelativeLayout mUILayout;
    
    //private SampleAppMenu mSampleAppMenu;
    
    LoadingDialogHandler loadingDialogHandler = new LoadingDialogHandler(this);


    private View viewMenu;
    private View videoView;
    private TextView textView1;
    private ImageView imageView;
    private TextView textView2;
    private ListView listView;
    private VideoView video;
    private int ID;
    private MediaController mediaController;
    String[] values = new String[]{"Training", "Demo", "References"};
    // Alert Dialog used to display SDK errors
    private AlertDialog mErrorDialog;
    
    boolean mIsDroidDevice = false;
    
    
    // Called when the activity first starts or the user navigates back to an
    // activity.
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(LOGTAG, "onCreate");
        super.onCreate(savedInstanceState);
        
        vuforiaAppSession = new SampleApplicationSession(this);
        
        startLoadingAnimation();
        
        vuforiaAppSession
            .initAR(this, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        // Load any sample specific textures:
        mTextures = new Vector<Texture>();
        loadTextures();
        
        mGestureDetector = new GestureDetector(this, new GestureListener());
        
        mIsDroidDevice = android.os.Build.MODEL.toLowerCase().startsWith(
            "droid");

        final LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,LayoutParams.MATCH_PARENT);
        LayoutInflater inflater = getLayoutInflater();

        viewMenu = inflater.inflate(R.layout.menu, null);
        viewMenu.setVisibility(View.INVISIBLE);
        textView1  = (TextView) viewMenu.findViewById(R.id.text_type);
        textView2 = (TextView) viewMenu.findViewById(R.id.text_value);
        imageView = (ImageView) viewMenu.findViewById(R.id.instance_image);

        videoView = inflater.inflate(R.layout.video_layout,null);
        videoView.setVisibility(View.INVISIBLE);
        video = (VideoView) videoView.findViewById(R.id.video);
        LinearLayout menuLayout = (LinearLayout) viewMenu.findViewById(R.id.menu_layout);
        listView = (ListView) viewMenu.findViewById(R.id.options);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(ObjectTargets.this, R.layout.list_items_white, values);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(i==0)
                {
                    if(mediaController == null)
                    {
                        mediaController = new MediaController(ObjectTargets.this);
                        mediaController.setAnchorView(video);
                        mediaController.setMediaPlayer(video);
                        video.setMediaController(mediaController);
                    }
                    try {

                        String uriPath = setURIPath();
                        video.setVideoURI(Uri.parse(uriPath));
                    } catch (Exception e){

                        Log.e("Error", e.getMessage());
                        e.printStackTrace();
                    }
                    video.requestFocus();
                    video.seekTo(0);
                    hideMenu();
                    playVideo();
                    //Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    //startActivity(intent);
                }
                if(i==1)
                {
                    String path = setVideoPath();
                    //playVideo(path);
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    startActivity(intent);
                }
                if(i==2)
                {
                    String path = setWebPath();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
                    startActivity(intent);
                }
                Toast.makeText(ObjectTargets.this,"Clicked "+ i, Toast.LENGTH_SHORT).show();
            }
        });
        viewMenu.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction() == MotionEvent.ACTION_DOWN){
                    hideMenu();
                    return true;
                }
                return false;
            }
        });
        addContentView(viewMenu, layoutParams);
        addContentView(videoView,layoutParams);
    }

    private String setURIPath(){

        String uriPath="";
        switch (ID)
        {
            case 1:
                uriPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
                break;
            case 2:
                uriPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
                break;
            case 3:
                uriPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
                break;
            case 4:
                uriPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
                break;
            case 5:
                uriPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
                break;
            case -1:
                uriPath = "android.resource://" + getPackageName() + "/" + R.raw.videoplayback;
                break;
        }
        return uriPath;
    }

    private String setVideoPath(){

        String Path="";
        switch (ID)
        {
            case 1:
                Path = "https://www.youtube.com/watch?v=NlLJMv1Y7Hk";
                break;
            case 2:
                Path = "https://www.youtube.com/watch?v=NlLJMv1Y7Hk";
                break;
            case 3:
                Path = "https://www.youtube.com/watch?v=NlLJMv1Y7Hk";
                break;
            case 4:
                Path = "https://www.youtube.com/watch?v=NlLJMv1Y7Hk";
                break;
            case 5:
                Path = "https://www.youtube.com/watch?v=NlLJMv1Y7Hk";
                break;
            case -1:
                Path = "https://www.youtube.com/watch?v=NlLJMv1Y7Hk";
                break;
        }
        return Path;
    }

    private String setWebPath(){

        String Path="";
        switch (ID)
        {
            case 1:
                Path = "http://www.schneider-electric.co.in/en/";
                break;
            case 2:
                Path = "http://www.schneider-electric.co.in/en/";
                break;
            case 3:
                Path = "http://www.schneider-electric.co.in/en/";
                break;
            case 4:
                Path = "http://www.schneider-electric.co.in/en/";
                break;
            case 5:
                Path = "http://www.schneider-electric.co.in/en/";
                break;
            case -1:
                Path = "http://www.schneider-electric.co.in/en/";
                break;
        }
        return Path;
    }

    private void setValuesFromID() {

        switch (ID)
        {
            case 1:
                imageView.setImageResource(R.drawable.image1);
                textView1.setText("Image 1");
                break;
            case 2:
                imageView.setImageResource(R.drawable.schneider_electric);
                textView1.setText("Image 2");
                break;
            case 3:
                imageView.setImageResource(R.drawable.image2);
                textView1.setText("Image 3");
                break;
            case 4:
                imageView.setImageResource(R.drawable.image1);
                textView1.setText("Image 4");
                break;
            case 5:
                imageView.setImageResource(R.drawable.image2);
                textView1.setText("Image 5");
                break;
            case -1:
                imageView.setImageResource(R.drawable.card_info);
                textView1.setText("Image not found");
                break;
        }

    }

    private void playVideo()
    {

        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                video.bringToFront();
                video.setVisibility(View.VISIBLE);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                hideMenu();
                video.start();

            }
        });
    }


    // We want to load specific textures from the APK, which we will later use
    // for rendering.
    private void loadTextures()
    {
        mTextures.add(Texture.loadTextureFromApk(
            "ObjectRecognition/CubeWireframe.png", getAssets()));
    }
    
    
    // Process Single Tap event to trigger autofocus
    private class GestureListener extends
        GestureDetector.SimpleOnGestureListener
    {
        // Used to set autofocus one second after a manual focus is triggered
        private final Handler autofocusHandler = new Handler();
        
        
        @Override
        public boolean onDown(MotionEvent e)
        {
            return true;
        }
        
        
        @Override
        public boolean onSingleTapUp(MotionEvent e)
        {
            // Generates a Handler to trigger autofocus
            // after 1 second
            autofocusHandler.postDelayed(new Runnable()
            {
                public void run()
                {
                    boolean result = CameraDevice.getInstance().setFocusMode(
                        CameraDevice.FOCUS_MODE.FOCUS_MODE_TRIGGERAUTO);
                    
                    if (!result)
                        Log.e("SingleTapUp", "Unable to trigger focus");
                }
            }, 1000L);
            
            return true;
        }
    }
    
    
    // Called when the activity will start interacting with the user.
    @Override
    protected void onResume()
    {
        Log.d(LOGTAG, "onResume");
        super.onResume();
        
        // This is needed for some Droid devices to force portrait
        if (mIsDroidDevice)
        {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
        
        try
        {
            vuforiaAppSession.resumeAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Resume the GL view:
        if (mGlView != null)
        {
            mGlView.setVisibility(View.VISIBLE);
            mGlView.onResume();
        }
        
    }
    
    
    // Callback for configuration changes the activity handles itself
    @Override
    public void onConfigurationChanged(Configuration config)
    {
        Log.d(LOGTAG, "onConfigurationChanged");
        super.onConfigurationChanged(config);
        
        vuforiaAppSession.onConfigurationChanged();
    }
    
    
    // Called when the system is about to start resuming a previous activity.
    @Override
    protected void onPause()
    {
        Log.d(LOGTAG, "onPause");
        super.onPause();
        
        if (mGlView != null)
        {
            mGlView.setVisibility(View.INVISIBLE);
            mGlView.onPause();
        }
        
        // Turn off the flash
        if (mFlashOptionView != null && mFlash)
        {
            // OnCheckedChangeListener is called upon changing the checked state
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                ((Switch) mFlashOptionView).setChecked(false);
            } else
            {
                ((CheckBox) mFlashOptionView).setChecked(false);
            }
        }
        
        try
        {
            vuforiaAppSession.pauseAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
    }
    
    
    // The final call you receive before your activity is destroyed.
    @Override
    protected void onDestroy()
    {
        Log.d(LOGTAG, "onDestroy");
        super.onDestroy();
        
        try
        {
            vuforiaAppSession.stopAR();
        } catch (SampleApplicationException e)
        {
            Log.e(LOGTAG, e.getString());
        }
        
        // Unload texture:
        mTextures.clear();
        mTextures = null;
        
        System.gc();
    }

    @Override
    public void onBackPressed() {
        if(video.getVisibility() == View.VISIBLE)
        {
            video.suspend();
            video.setVisibility(View.INVISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            //showMenu();

        }
        else if(viewMenu.getVisibility()== View.VISIBLE)
        {
            hideMenu();
        }
        else
            super.onBackPressed();

    }

    private void hideVideo() {

        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(videoView.getVisibility() != View.VISIBLE){
                    return;
                }
                //Animation bottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
                //videoView.startAnimation(bottomDown);
                videoView.setVisibility(View.GONE);

            }
        });
    }

    // Initializes AR application components.
    private void initApplicationAR()
    {
        // Create OpenGL ES view:
        int depthSize = 16;
        int stencilSize = 0;
        boolean translucent = Vuforia.requiresAlpha();
        
        mGlView = new SampleApplicationGLView(this);
        mGlView.init(translucent, depthSize, stencilSize);
        
        mRenderer = new ObjectTargetRenderer(this, vuforiaAppSession);
        mRenderer.setTextures(mTextures);
        mGlView.setRenderer(mRenderer);
        
    }
    
    
    private void startLoadingAnimation()
    {
        LayoutInflater inflater = LayoutInflater.from(this);
        mUILayout = (RelativeLayout) inflater.inflate(R.layout.camera_overlay,
            null, false);
        
        mUILayout.setVisibility(View.VISIBLE);
        mUILayout.setBackgroundColor(Color.BLACK);
        
        // Gets a reference to the loading dialog
        loadingDialogHandler.mLoadingDialogContainer = mUILayout
            .findViewById(R.id.loading_indicator);
        
        // Shows the loading indicator at start
        loadingDialogHandler
            .sendEmptyMessage(LoadingDialogHandler.SHOW_LOADING_DIALOG);
        
        // Adds the inflated layout to the view
        addContentView(mUILayout, new LayoutParams(LayoutParams.MATCH_PARENT,
            LayoutParams.MATCH_PARENT));
        
    }

    void showMenu(int id){
        final Context context = this;
        ID = id;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(viewMenu.getVisibility() == View.VISIBLE)
                    return;
                Animation bottomUp = AnimationUtils.loadAnimation(context,
                        R.anim.bottom_up);
                textView2.setText("Select Options below: ");
                setValuesFromID();
                viewMenu.bringToFront();
                viewMenu.setVisibility(View.VISIBLE);
                viewMenu.startAnimation(bottomUp);
                //Toast.makeText(ObjectTargets.this,"Show Menu here",Toast.LENGTH_SHORT).show();
            }
        });
    }

    void hideMenu(){
        final Context context = this;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(viewMenu.getVisibility() != View.VISIBLE){
                    return;
                }
                Animation bottomDown = AnimationUtils.loadAnimation(context, R.anim.bottom_down);
                viewMenu.startAnimation(bottomDown);
                viewMenu.setVisibility(View.INVISIBLE);
            }
        });
    }

    // Methods to load and destroy tracking data.
    @Override
    public boolean doLoadTrackersData()
    {
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
            .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;
        
        if (mCurrentDataset == null)
            mCurrentDataset = objectTracker.createDataSet();
        
        if (mCurrentDataset == null)
            return false;
        
        if (!mCurrentDataset.load("ObjectRecognition/objTargetDB/sampledb_OT.xml",
            STORAGE_TYPE.STORAGE_APPRESOURCE))
            return false;
        
        if (!objectTracker.activateDataSet(mCurrentDataset))
            return false;
        
        int numTrackables = mCurrentDataset.getNumTrackables();
        for (int count = 0; count < numTrackables; count++)
        {
            Trackable trackable = mCurrentDataset.getTrackable(count);
            if(isExtendedTrackingActive())
            {
                trackable.startExtendedTracking();
            }
            
            String name = "Current Dataset : " + trackable.getName();
            trackable.setUserData(name);
            Log.d(LOGTAG, "UserData:Set the following user data "
                + (String) trackable.getUserData());
        }
        
        return true;
    }
    
    
    @Override
    public boolean doUnloadTrackersData()
    {
        // Indicate if the trackers were unloaded correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        ObjectTracker objectTracker = (ObjectTracker) tManager
            .getTracker(ObjectTracker.getClassType());
        if (objectTracker == null)
            return false;
        
        if (mCurrentDataset != null && mCurrentDataset.isActive())
        {
            if (objectTracker.getActiveDataSet(0).equals(mCurrentDataset)
                && !objectTracker.deactivateDataSet(mCurrentDataset))
            {
                result = false;
            } else if (!objectTracker.destroyDataSet(mCurrentDataset))
            {
                result = false;
            }
            
            mCurrentDataset = null;
        }
        
        return result;
    }
    
    
    @Override
    public void onInitARDone(SampleApplicationException exception)
    {
        
        if (exception == null)
        {
            initApplicationAR();

            mRenderer.setActive(true);

            // Now add the GL surface view. It is important
            // that the OpenGL ES surface view gets added
            // BEFORE the camera is started and video
            // background is configured.
            addContentView(mGlView, new LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT));
            
            // Sets the UILayout to be drawn in front of the camera
            mUILayout.bringToFront();
            
            // Sets the layout background to transparent
            mUILayout.setBackgroundColor(Color.TRANSPARENT);
            
            try
            {
                vuforiaAppSession.startAR(CameraDevice.CAMERA_DIRECTION.CAMERA_DIRECTION_DEFAULT);
            } catch (SampleApplicationException e)
            {
                Log.e(LOGTAG, e.getString());
            }
            
            boolean result = CameraDevice.getInstance().setFocusMode(
                CameraDevice.FOCUS_MODE.FOCUS_MODE_CONTINUOUSAUTO);
            
            if (result)
                Log.e(LOGTAG, "Unable to enable continuous autofocus");
            
//            mSampleAppMenu = new SampleAppMenu(this, this, "Object Reco",
//                mGlView, mUILayout, null);
//            setSampleAppMenuSettings();
//
        } else
        {
            Log.e(LOGTAG, exception.getString());
            if(exception.getCode() == SampleApplicationException.LOADING_TRACKERS_FAILURE)
            {
                showInitializationErrorMessage( 
                    getString(R.string.INIT_OBJECT_DATASET_NOT_FOUND_TITLE),
                    getString(R.string.INIT_OBJECT_DATASET_NOT_FOUND));

            }
            else
            {
                showInitializationErrorMessage( getString(R.string.INIT_ERROR),
                    exception.getString() );
            }
        }
    }
    
    
    // Shows initialization error messages as System dialogs
    public void showInitializationErrorMessage(String title, String message)
    {
        final String errorMessage = message;
        final String messageTitle = title;
        runOnUiThread(new Runnable()
        {
            public void run()
            {
                if (mErrorDialog != null)
                {
                    mErrorDialog.dismiss();
                }
                
                // Generates an Alert Dialog to show the error message
                AlertDialog.Builder builder = new AlertDialog.Builder(
                    ObjectTargets.this);
                builder
                    .setMessage(errorMessage)
                    .setTitle(messageTitle)
                    .setCancelable(false)
                    .setIcon(0)
                    .setPositiveButton(getString(R.string.button_OK),
                        new DialogInterface.OnClickListener()
                        {
                            public void onClick(DialogInterface dialog, int id)
                            {
                                finish();
                            }
                        });
                
                mErrorDialog = builder.create();
                mErrorDialog.show();
            }
        });
    }
    
    
    @Override
    public void onVuforiaUpdate(State state)
    {
    }
    
    
    @Override
    public boolean doInitTrackers()
    {
        // Indicate if the trackers were initialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        Tracker tracker;
        
        // Trying to initialize the image tracker
        tracker = tManager.initTracker(ObjectTracker.getClassType());
        if (tracker == null)
        {
            Log.e(
                LOGTAG,
                "Tracker not initialized. Tracker already initialized or the camera is already started");
            result = false;
        } else
        {
            Log.i(LOGTAG, "Tracker successfully initialized");
        }
        return result;
    }
    
    
    @Override
    public boolean doStartTrackers()
    {
        // Indicate if the trackers were started correctly
        boolean result = true;
        
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
            ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.start();
        
        return result;
    }
    
    
    @Override
    public boolean doStopTrackers()
    {
        // Indicate if the trackers were stopped correctly
        boolean result = true;
        
        Tracker objectTracker = TrackerManager.getInstance().getTracker(
            ObjectTracker.getClassType());
        if (objectTracker != null)
            objectTracker.stop();
        
        return result;
    }
    
    
    @Override
    public boolean doDeinitTrackers()
    {
        // Indicate if the trackers were deinitialized correctly
        boolean result = true;
        
        TrackerManager tManager = TrackerManager.getInstance();
        tManager.deinitTracker(ObjectTracker.getClassType());
        
        return result;
    }

    boolean isExtendedTrackingActive()
    {
        return mExtendedTracking;
    }
    
    final public static int CMD_BACK = -1;
    final public static int CMD_EXTENDED_TRACKING = 1;
    final public static int CMD_FLASH = 2;
    
    // This method sets the menu's settings
//    private void setSampleAppMenuSettings()
//    {
//        SampleAppMenuGroup group;
//
//        group = mSampleAppMenu.addGroup("", false);
//        group.addTextItem(getString(R.string.menu_back), -1);
//
//        group = mSampleAppMenu.addGroup("", true);
//        group.addSelectionItem(getString(R.string.menu_extended_tracking),
//            CMD_EXTENDED_TRACKING, false);
//        mFlashOptionView = group.addSelectionItem(
//            getString(R.string.menu_flash), CMD_FLASH, false);
//
//        mSampleAppMenu.attachMenu();
//    }
//
//
//    @Override
//    public boolean menuProcess(int command)
//    {
//
//        boolean result = true;
//
//        switch (command)
//        {
//            case CMD_BACK:
//                finish();
//                break;
//
//            case CMD_FLASH:
//                result = CameraDevice.getInstance().setFlashTorchMode(!mFlash);
//
//                if (result)
//                {
//                    mFlash = !mFlash;
//                } else
//                {
//                    showToast(getString(mFlash ? R.string.menu_flash_error_off
//                        : R.string.menu_flash_error_on));
//                    Log.e(LOGTAG,
//                        getString(mFlash ? R.string.menu_flash_error_off
//                            : R.string.menu_flash_error_on));
//                }
//                break;
//
//            case CMD_EXTENDED_TRACKING:
//                for (int tIdx = 0; tIdx < mCurrentDataset.getNumTrackables(); tIdx++)
//                {
//                    Trackable trackable = mCurrentDataset.getTrackable(tIdx);
//
//                    if (!mExtendedTracking)
//                    {
//                        if (!trackable.startExtendedTracking())
//                        {
//                            Log.e(LOGTAG,
//                                "Failed to start extended tracking target");
//                            result = false;
//                        } else
//                        {
//                            Log.d(LOGTAG,
//                                "Successfully started extended tracking target");
//                        }
//                    } else
//                    {
//                        if (!trackable.stopExtendedTracking())
//                        {
//                            Log.e(LOGTAG,
//                                "Failed to stop extended tracking target");
//                            result = false;
//                        } else
//                        {
//                            Log.d(LOGTAG,
//                                "Successfully started extended tracking target");
//                        }
//                    }
//                }
//
//                if (result)
//                    mExtendedTracking = !mExtendedTracking;
//
//                break;
//
//        }
//
//        return result;
//    }
    
    
    private void showToast(String text)
    {
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }
}
