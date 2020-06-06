package com.first75494.textrecognitionx;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.IOException;
import java.security.Permission;

public class MainActivity extends AppCompatActivity {

   private SurfaceView surfaceView;
   private TextView textView;
   private CameraSource cameraSource;
   private static final int PERMISSION=100;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        surfaceView=findViewById(R.id.camera);
        textView=findViewById(R.id.text);

        startCameraSource();
    }

    private void startCameraSource(){
        final TextRecognizer textRecognizer=new TextRecognizer.Builder(getApplicationContext()).build();

        if(!textRecognizer.isOperational()){
            Log.w("TAG","Dependencies not loaded yet");
        }else{
            cameraSource=new CameraSource.Builder(getApplicationContext(),textRecognizer)
                    .setFacing(CameraSource.CAMERA_FACING_BACK).setRequestedPreviewSize(1280,1024)
                    .setAutoFocusEnabled(true).setRequestedFps(2.0f).build();
            surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                   try{
                       if(ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED){
                           ActivityCompat.requestPermissions(MainActivity.this,new String[]{Manifest.permission.CAMERA},PERMISSION);
                           return;
                       }
                       cameraSource.start(surfaceView.getHolder());
                   }catch(IOException e){
                       e.printStackTrace();
                   }
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                   //Release source for camerasource

                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    cameraSource.stop();
                }
            });
            textRecognizer.setProcessor(new Detector.Processor<TextBlock>() {
                @Override
                public void release() {
                    //Detect all text from camera
                }

                @Override
                public void receiveDetections(Detector.Detections<TextBlock> detections) {
                    final SparseArray<TextBlock> items = detections.getDetectedItems();
                    if(items.size()!=0){
                        textView.post(new Runnable() {
                            @Override
                            public void run() {
                                StringBuilder stringBulider=new StringBuilder();
                                for(int i=0;i<items.size();i++){
                                    TextBlock item=items.valueAt(i);
                                    stringBulider.append(item.getValue());
                                    stringBulider.append("\n");
                                }
                                textView.setText(stringBulider.toString());
                            }
                        });
                    }
                }
            });
        }
    }
}

