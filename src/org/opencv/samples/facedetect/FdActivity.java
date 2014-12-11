package org.opencv.samples.facedetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.objdetect.CascadeClassifier;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.Surface;
import android.view.WindowManager;

public class FdActivity extends Activity implements CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";
    private static final Scalar    FACE_RECT_COLOR     = new Scalar(0, 255, 0, 255);
    private static final Scalar    HEAD_RECT_COLOR     = new Scalar(255, 255, 0, 255);
    //public static final int        JAVA_DETECTOR       = 0;
   // public static final int        NATIVE_DETECTOR     = 1;
    
    private MenuItem               mItemFace50;
    private MenuItem               mItemFace40;
    private MenuItem               mItemFace30;
    private MenuItem               mItemFace20;
  //  private MenuItem               mItemType;

    private Mat                    mRgba;
    private Mat                    mGray;
    private File                   mHogCascadeFile;
    private File                   mLbpCascadeFile;
   // private CascadeClassifier      mJavaDetector;
    private DetectionBasedTracker  mNativeFaceDetector;
    private DetectionBasedTracker  mNativeHeadDetector;

   // private int                    mDetectorType       = NATIVE_DETECTOR;
    //private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    private CameraBridgeViewBase   mOpenCvCameraView;
  
    
    private BaseLoaderCallback  mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("detection_based_tracker");

                    try {
                        // load cascade file from application resources
                        InputStream is = getResources().openRawResource(R.raw.hogcascade_9_998_3_1_150);
                        InputStream is2 = getResources().openRawResource(R.raw.lbpcascade_frontalface);
                        File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
                        mHogCascadeFile = new File(cascadeDir, "hogcascade_9_998_3_1_150.xml");
                        mLbpCascadeFile = new File(cascadeDir, "lbocascade_frontalface.xml");
                        FileOutputStream os = new FileOutputStream(mHogCascadeFile);
                        FileOutputStream os2 = new FileOutputStream(mLbpCascadeFile);
                        
                        byte[] buffer = new byte[4096];
                        int bytesRead;
                        while ((bytesRead = is.read(buffer)) != -1) {
                            os.write(buffer, 0, bytesRead);
                        }
                        is.close();
                        os.close();
                        
                        byte[] buffer2 = new byte[4096];
                        int bytesRead2;
                        while ((bytesRead2 = is2.read(buffer2)) != -1) {
                            os2.write(buffer2, 0, bytesRead2);
                        }
                        is2.close();
                        os2.close();
//45678921e23q
                        //mJavaDetector = new CascadeClassifier(mCascadeFile.getAbsolutePath());
                        //if (mJavaDetector.empty()) {
                        //    Log.e(TAG, "Failed to load cascade classifier");
                        //    mJavaDetector = null;
                        //} else
                        //    Log.i(TAG, "Loaded cascade classifier from " + mCascadeFile.getAbsolutePath());

                        mNativeHeadDetector = new DetectionBasedTracker(mHogCascadeFile.getAbsolutePath(), 0);
                        mNativeFaceDetector = new DetectionBasedTracker(mLbpCascadeFile.getAbsolutePath(), 0);

                        cascadeDir.delete();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Log.e(TAG, "Failed to load cascade. Exception thrown: " + e);
                    }

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    public FdActivity() {
        //mDetectorName = new String[2];
        //mDetectorName[JAVA_DETECTOR] = "Java";
        //mDetectorName[NATIVE_DETECTOR] = "Native (tracking)";

        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.face_detect_surface_view);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);//(CameraBridgeViewBase) findViewById(R.id.fd_activity_surface_view);
        
        //mOpenCvCameraView = (CameraBridgeViewBase) new JavaCameraView(this, -1);
        //setContentView(mOpenCvCameraView);
        
        mOpenCvCameraView.setCvCameraViewListener(this);
        
               
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
        mGray = new Mat();
        mRgba = new Mat();
    }

    public void onCameraViewStopped() {
        mGray.release();
        mRgba.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();
        mGray = inputFrame.gray();
//        Configuration config = getResources().getConfiguration();
//        Display display = ((WindowManager)mOpenCvCameraView.getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        //Mat dst = new Mat();
//        float[] R;
//        float[] I;
//        float[] values;
        //boolean success = SensorManager.getRotationMatrix(R, I, gravity, geomag);
//        if(display.getRotation() == Surface.ROTATION_0){
//        	//mRgba.t();
//        	//Point cp = new Point(mRgba.cols()/2,mRgba.rows()/2);
//        	//Mat rot = Imgproc.getRotationMatrix2D(cp,-90,1);
//        	mOpenCvCameraView.rot90();
//        	//Size sz = new Size(200,200);
//        	//mOpenCvCameraView.setResolution(100,100);
//        	//Imgproc.warpAffine(mRgba,dst,rot,new Size(mRgba.cols(),mRgba.rows()));
//        	//dst.copyTo(mRgba);
//        	Point p3 = new Point(50.0,50.0);
//            Point p4 = new Point(70.0,70.0);
//            Core.rectangle(mRgba, p3, p4, FACE_RECT_COLOR, 3);
//        }
        if (mAbsoluteFaceSize == 0) {
            int height = mGray.rows();
            if (Math.round(height * mRelativeFaceSize) > 0) {
                mAbsoluteFaceSize = Math.round(height * mRelativeFaceSize);
            }
            mNativeHeadDetector.setMinFaceSize(mAbsoluteFaceSize);
            mNativeFaceDetector.setMinFaceSize(mAbsoluteFaceSize);
        }

        MatOfRect faces = new MatOfRect();
        MatOfRect heads = new MatOfRect();
        //if (mDetectorType == JAVA_DETECTOR) {
        //    if (mJavaDetector != null)
        //        mJavaDetector.detectMultiScale(mGray, faces, 1.1, 2, 2, // TODO: objdetect.CV_HAAR_SCALE_IMAGE
        //                new Size(mAbsoluteFaceSize, 2*mAbsoluteFaceSize), new Size());
        //}
        //else 
        //if (mDetectorType == NATIVE_DETECTOR) {
        if (mNativeHeadDetector != null){
            mNativeHeadDetector.detect(mGray, heads);
        }
        else{
        	Log.e(TAG, "No Head Detector!");
        }
        if (mNativeFaceDetector != null){
            mNativeFaceDetector.detect(mGray, faces);
        }
        else{
        	Log.e(TAG, "No Face Detector!");
        }
            
       // }
     //   else {
    //        Log.e(TAG, "Detection method is not selected!");
     //   }

        Rect[] facesArray = faces.toArray();
        Rect[] headsArray = heads.toArray();
        for (int i = 0; i < facesArray.length; i++)
            Core.rectangle(mRgba, facesArray[i].tl(), facesArray[i].br(), FACE_RECT_COLOR, 3);
        for (int i = 0; i < headsArray.length; i++)
            Core.rectangle(mRgba, headsArray[i].tl(), headsArray[i].br(), HEAD_RECT_COLOR, 3);
        
       // Point p1 = new Point(0.0,0.0);
       // Point p2 = new Point(.0,10.0);
        //Core.rectangle(mRgba, p1, p2, FACE_RECT_COLOR, 3);
        Core.putText(mRgba, String.valueOf(headsArray.length), new Point(50.0,50.0),Core.FONT_HERSHEY_SIMPLEX,1.0,new Scalar(0,0,255,255),3);
        Core.putText(mRgba, String.valueOf(facesArray.length), new Point(100.0,50.0),Core.FONT_HERSHEY_SIMPLEX,1.0,new Scalar(255,0,0,255),3);

        return mRgba;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemFace50 = menu.add("Face size 50%");
        mItemFace40 = menu.add("Face size 40%");
        mItemFace30 = menu.add("Face size 30%");
        mItemFace20 = menu.add("Face size 20%");
        //mItemType   = menu.add(mDetectorName[mDetectorType]);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);
        if (item == mItemFace50)
            setMinFaceSize(0.5f);
        else if (item == mItemFace40)
            setMinFaceSize(0.4f);
        else if (item == mItemFace30)
            setMinFaceSize(0.3f);
        else if (item == mItemFace20)
            setMinFaceSize(0.2f);
//        else if (item == mItemType) {
//            int tmpDetectorType = (mDetectorType + 1) % mDetectorName.length;
//            item.setTitle(mDetectorName[tmpDetectorType]);
//            setDetectorType(tmpDetectorType);
//        }
        return true;
    }

    private void setMinFaceSize(float faceSize) {
        mRelativeFaceSize = faceSize;
        mAbsoluteFaceSize = 0;
    }

 //   private void setDetectorType(int type) {
 //      if (mDetectorType != type) {
 //           mDetectorType = type;
//
//            if (type == NATIVE_DETECTOR) {
//                Log.i(TAG, "Detection Based Tracker enabled");
//                mNativeDetector.start();
//            } else {
//                Log.i(TAG, "Cascade detector enabled");
//                mNativeDetector.stop();
//            }
//        }
//    }
//    @Override
//    public void onConfigurationChanged(Configuration newConfig) {
//        super.onConfigurationChanged(newConfig);
//
//        // Checks the orientation of the screen
//        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
//           
//        } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
//        		mRgba.t();
//        }
//    }
}
