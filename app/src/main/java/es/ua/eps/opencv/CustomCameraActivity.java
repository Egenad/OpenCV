package es.ua.eps.opencv;

import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.Collections;
import java.util.List;

public class CustomCameraActivity extends CameraActivity implements CvCameraViewListener2 {

    private JavaCameraView mOpenCvCameraView;

    private Mat mBlur;
    private Mat mGray;
    private Mat mEdges;
    private Mat mResult;

    private Boolean stop;

    private static final String TAG = "OCVSample::Activity";

    private Button startStopButton;
    private SeekBar blurSeekBar;
    private SeekBar gradientSeekBar;
    private SeekBar angleSeekBar;

    private Mat gradX;
    private Mat gradY;
    private Mat magnitude;
    private Mat angle;

    private int kernelSize = 5;
    private int edgeGradient = 80;
    private int angleTH = 100;

    public CustomCameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        if (OpenCVLoader.initLocal()) {
            Log.i(TAG, "OpenCV loaded successfully");
        } else {
            Log.e(TAG, "OpenCV initialization failed!");
            (Toast.makeText(this, "OpenCV initialization failed!", Toast.LENGTH_LONG)).show();
            return;
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON + WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_custom_camera);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.camera_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        mBlur = new Mat();
        mGray = new Mat();
        mEdges = new Mat();
        mResult = new Mat();

        gradX = new Mat();
        gradY = new Mat();
        magnitude = new Mat();
        angle = new Mat();

        stop = false;

        startStopButton = findViewById(R.id.start_stop_button);
        startStopButton.setOnClickListener(v -> {
            stop = !stop;
            if(stop){
                startStopButton.setText(R.string.start_button);
            }else{
                startStopButton.setText(R.string.stop_button);
            }
        });

        blurSeekBar = findViewById(R.id.blur_seekbar);
        blurSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                kernelSize = progress;

                if(kernelSize == 0 || kernelSize % 2 == 0) kernelSize++;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        gradientSeekBar = findViewById(R.id.gradient_seekbar);
        gradientSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                edgeGradient = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        angleSeekBar = findViewById(R.id.angle_seekbar);
        angleSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                angleTH = progress;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

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
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        Mat rgba = inputFrame.rgba();

        if(!stop) { // Canny edge detection
            Imgproc.GaussianBlur(rgba, mBlur, new Size(kernelSize, kernelSize), 0, 0); // 5x5 Gaussian Blur
            Imgproc.cvtColor(mBlur, mGray, Imgproc.COLOR_RGBA2GRAY); // Image to gray scale

            // Gradient calculation
            Imgproc.Sobel(mGray, gradX, CvType.CV_32F, 1, 0); // X direction
            Imgproc.Sobel(mGray, gradY, CvType.CV_32F, 0, 1); // Y direction

            // Magnitude
            Core.magnitude(gradX, gradY, magnitude);

            // Gradient direction
            Core.phase(gradX, gradY, angle);

            Imgproc.Canny(mGray, mEdges, edgeGradient, angleTH); // Canny edge detection
            Imgproc.cvtColor(mEdges, mResult, Imgproc.COLOR_GRAY2RGBA); // Change color to detected edges
        }else{
            mResult = rgba;
        }

        return mResult;
    }
}