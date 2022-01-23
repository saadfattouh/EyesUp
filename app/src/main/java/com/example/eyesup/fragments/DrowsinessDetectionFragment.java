package com.example.eyesup.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.CountDownTimer;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.Voice;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.eyesup.CameraPreview;
import com.example.eyesup.R;
import com.example.eyesup.helper.SharedPrefManager;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

public class DrowsinessDetectionFragment extends Fragment {

    public static final String TAG = "detectionFragment";
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 101;


    private Camera mCamera;
    private CameraPreview mPreview;

    boolean rightEye;
    boolean leftEye;
    private int eyesClosedCount = 0;

    boolean safeToTakeAPicture = false;


    Timer timer;

    //alarms related
    TextToSpeech t1;
    SpeechRecognizer speechRecognizer;

    SharedPrefManager prefManager;

    String contactNumber = null;

    boolean alarmType = false;
    String ttsMessage = null;

    //if alarm type is set to true
    MediaPlayer m1 = null;
    int warningSound;


    public DrowsinessDetectionFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        prefManager = SharedPrefManager.getInstance(requireContext());

        if(prefManager.isContactNumberSet()){
            contactNumber = prefManager.getContactNumber();
        }

        alarmType = prefManager.getAlarmType();

        if(alarmType){
            //alarm
            warningSound = R.raw.warning;
        }else {
            ttsMessage = prefManager.getTTSMessage();
        }



        t1 = new TextToSpeech(requireContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status != TextToSpeech.ERROR) {
                    t1.setLanguage(Locale.US);
                }
            }
        });

    }

    public void playSound(boolean play) {
        if (play) {
            // This play function
            // takes five parameter
            // leftVolume, rightVolume,
            // priority, loop and rate.
            if(m1 == null) {
                m1 = MediaPlayer.create(requireContext(), warningSound);
                m1.setVolume(1.0f, 1.0f);
                m1.setLooping(true);
                m1.start();

            }else
                m1.start();
        }else{
            if(m1!= null)
                m1.pause();
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_drawiness_detection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }else {
            setUpCamera(view);


            safeToTakeAPicture = true;

            // And From your main() method or any other method
            timer = new Timer();
            timer.schedule(new tackPic(), 1000, 3000);
        }

    }

    class tackPic extends TimerTask {
        public void run() {

            if(safeToTakeAPicture){
                mCamera.takePicture(null, null, mPicture);
                safeToTakeAPicture = false;
            }
        }
    }

    public void setUpCamera(View view){
        mCamera = openFrontFacingCamera();

        // Create our Preview view and set it as the content of our activity.
        mPreview = new CameraPreview(getContext(), mCamera);
        FrameLayout preview = view.findViewById(R.id.camera_preview);
        preview.addView(mPreview);

        mCamera.setDisplayOrientation(90);
    }


    private Camera openFrontFacingCamera() {
        int cameraCount = 0;
        Camera cam = null;
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        cameraCount = Camera.getNumberOfCameras();
        for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
            Camera.getCameraInfo( camIdx, cameraInfo );
            if ( cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT  ) {
                try {
                    cam = Camera.open( camIdx );
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera failed to open: " + e.getLocalizedMessage());
                }
            }
        }

        return cam;
    }

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {

            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            bitmap = rotateBitmap(bitmap, -90);
            InputImage frame = InputImage.fromBitmap(bitmap, 0);

            // detect driver drawsiness and return the results
            detectFaces(frame);

            mCamera.startPreview();

            safeToTakeAPicture = true;
        }
    };

    public Bitmap rotateBitmap(Bitmap original, float degrees) {
        Matrix matrix = new Matrix();
        matrix.preRotate(degrees);
        Bitmap rotatedBitmap = Bitmap.createBitmap(original, 0, 0, original.getWidth(), original.getHeight(), matrix, true);
        original.recycle();
        return rotatedBitmap;
    }

    private void detectFaces(InputImage image) {
        // [START set_detector_options]
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();
        // [END set_detector_options]

        // [START get_detector]
        FaceDetector detector = FaceDetection.getClient(options);
        // Or use the default options:
        // FaceDetector detector = FaceDetection.getClient();
        // [END get_detector]

        // [START run_detector]
        Task<List<Face>> result =
                detector.process(image)
                        .addOnSuccessListener(
                                new OnSuccessListener<List<Face>>() {
                                    @Override
                                    public void onSuccess(List<Face> faces) {
                                        // Task completed successfully
                                        // [START_EXCLUDE]
                                        // [START get_face_info]
                                        for (Face face : faces) {
                                            Rect bounds = face.getBoundingBox();
                                            float rotY = face.getHeadEulerAngleY();  // Head is rotated to the right rotY degrees
                                            float rotZ = face.getHeadEulerAngleZ();  // Head is tilted sideways rotZ degrees

                                            // If landmark detection was enabled (mouth, ears, eyes, cheeks, and
                                            // nose available):

                                            // If classification was enabled:
                                            if (face.getSmilingProbability() != null) {
                                                float smileProb = face.getSmilingProbability();
                                            }

                                            if (face.getRightEyeOpenProbability() != null) {
                                                float rightEyeOpenProb = face.getRightEyeOpenProbability();
                                                if(rightEyeOpenProb < 0.4 && rightEyeOpenProb>-1){
                                                    rightEye = false;
                                                }else if(rightEyeOpenProb > 0.4){
                                                    rightEye = true;
                                                }
                                            }

                                            if (face.getLeftEyeOpenProbability() != null) {
                                                float leftEyeOpenProb = face.getLeftEyeOpenProbability();
                                                if(leftEyeOpenProb < 0.4 && leftEyeOpenProb>-1){
                                                    leftEye = false;
                                                }else if(leftEyeOpenProb > 0.4){
                                                    leftEye = true;
                                                }
                                            }

                                            if(!rightEye && !leftEye){
                                                addToClosed();
                                                playAlarm(eyesClosedCount);
                                            }else{
                                                resetClosedCount();
                                                playSound(false);
                                            }
                                        }
                                        // [END get_face_info]
                                    }
                                })
                        .addOnFailureListener(
                                new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Task failed with an exception
                                        // ...
                                        Toast.makeText(requireContext(), "you must have internet connection", Toast.LENGTH_SHORT).show();
                                    }
                                });
        // [END run_detector]
    }

    private void playAlarm(int eyesClosedCount) {
        switch (eyesClosedCount){
            case 5:
                String firstAlarm = "sir please focus !";
                t1.speak(firstAlarm, TextToSpeech.QUEUE_FLUSH, null);
                break;
            case 10:
                if(alarmType){

                    Context ctx = requireContext();
                    LayoutInflater factory = LayoutInflater.from(ctx);
                    final View view = factory.inflate(R.layout.confirmation_dialog, null);
                    final AlertDialog cancelSoundDialog = new AlertDialog.Builder(ctx).create();
                    cancelSoundDialog.setView(view);
                    cancelSoundDialog.setCanceledOnTouchOutside(false);

                    TextView yes = view.findViewById(R.id.yes_btn);
                    TextView no = view.findViewById(R.id.no_btn);

                    playSound(true);
                    CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                        }

                        public void onFinish() {
                            playSound(false);
                            cancelSoundDialog.dismiss();
                            t1.speak("sir I am listening please confirm that you are awake", TextToSpeech.QUEUE_FLUSH, null);
                            startListening();
                        }
                    }.start();

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    no.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            playSound(false);
                            countDownTimer.cancel();
                            resetClosedCount();
                            cancelSoundDialog.dismiss();
                        }
                    });
                    cancelSoundDialog.show();


                }else{
                    t1.speak(ttsMessage, TextToSpeech.QUEUE_FLUSH, null);
                    t1.speak("tell me you are awake", TextToSpeech.QUEUE_FLUSH, null);
                    startListening();
                }
                break;
            case 30:
                if(prefManager.isContactNumberSet()){
                    Context ctx = requireContext();
                    LayoutInflater factory = LayoutInflater.from(ctx);
                    final View view = factory.inflate(R.layout.confirmation_dialog, null);
                    final AlertDialog callConfirmationDialog = new AlertDialog.Builder(ctx).create();
                    callConfirmationDialog.setView(view);
                    callConfirmationDialog.setCanceledOnTouchOutside(false);

                    TextView message = view.findViewById(R.id.message);
                    TextView yes = view.findViewById(R.id.yes_btn);

                    CountDownTimer countDownTimer = new CountDownTimer(10000, 1000) {

                        public void onTick(long millisUntilFinished) {
                            message.setText("calling the emergency contact number in " + millisUntilFinished/1000+"\n\n"+"if you are seeing this message and can respond then click yes to cancel the call");
                        }

                        public void onFinish() {
                            callAFriend(contactNumber);
                        }
                    }.start();

                    yes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(countDownTimer != null){
                                countDownTimer.cancel();
                                resetClosedCount();
                            }
                            callConfirmationDialog.dismiss();
                        }
                    });
                    callConfirmationDialog.show();

                    break;
                }

        }

    }


    private void startListening(){


        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(requireContext());
        Intent speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {
            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle bundle) {
                ArrayList<String> data = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                String spoken = data.get(0);
                if(spoken != null)
                    if(spoken.toLowerCase().contains("stop") || spoken.toLowerCase().contains("i am awake")|| spoken.toLowerCase().contains("i am fine")){
                        //stop the alarm!
                        t1.stop();
                        t1.speak("ooh Now I'm relieved", TextToSpeech.QUEUE_FLUSH, null);
                        speechRecognizer.stopListening();
                        resetClosedCount();
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });

        speechRecognizer.startListening(speechRecognizerIntent);


    }


    /** Check if this device has a camera */
    private boolean checkCameraHardware(Context context) {
        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            // this device has a camera
            return true;
        } else {
            // no camera on this device
            return false;
        }
    }


    @Override
    public void onPause() {
        timer.cancel();
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onPause();
    }



    @Override
    public void onDestroy() {
        timer.cancel();
        mCamera.release();
        if(speechRecognizer!=null)
            speechRecognizer.destroy();
        if(t1 !=null){
            t1.stop();
            t1.shutdown();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {

        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){

        } else {
            setUpCamera(requireView());


            if(timer == null){
                safeToTakeAPicture = true;
                // And From your main() method or any other method
                timer = new Timer();
                timer.schedule(new tackPic(), 1000, 3000);
            }

        }


        super.onResume();
    }

    void addToClosed(){
        eyesClosedCount++;
    }

    private void resetClosedCount() {
        eyesClosedCount = 0;
    }


    //use it after 1 minute of no response from user
    private void callAFriend(String phoneNumber){
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_DENIED){
            ActivityCompat.requestPermissions(requireActivity(), new String[] {Manifest.permission.CALL_PHONE}, 102);
        }else {
            Uri callUri = Uri.parse("tel://"+phoneNumber);
            Intent callIntent = new Intent(Intent.ACTION_CALL, callUri);
            callIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            startActivity(callIntent);
        }
    }

}