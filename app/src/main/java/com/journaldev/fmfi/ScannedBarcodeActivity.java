package com.journaldev.fmfi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.SparseArray;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

import java.io.IOException;

import android.view.MotionEvent;
import android.view.ScaleGestureDetector;


public class ScannedBarcodeActivity extends AppCompatActivity {


    SurfaceView surfaceView;
    TextView txtBarcodeValue;
    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private static final int REQUEST_CAMERA_PERMISSION = 201;
    Button btnAction;
    String intentData = "";
    boolean alreadyloaded= false;
    boolean isEmail = false;
    public ProgressDialog mProgressDialog;

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    ImageView myimage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        myimage = findViewById(R.id.myimage);
        initViews();
        mProgressDialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProgressDialog.setCancelable(false);
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());

    }
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return mScaleGestureDetector.onTouchEvent(event);
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {

        // when a scale gesture is detected, use it to resize the image
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
            mScaleFactor *= scaleGestureDetector.getScaleFactor();
            myimage.setScaleX(mScaleFactor);
            myimage.setScaleY(mScaleFactor);
            return true;
        }
    }
    private void initViews() {
        txtBarcodeValue = findViewById(R.id.txtBarcodeValue);
        surfaceView = findViewById(R.id.surfaceView);
        btnAction = findViewById(R.id.btnAction);

        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (alreadyloaded)
                {
                    myimage.setVisibility(View.GONE);
                    surfaceView.setVisibility(View.VISIBLE);
                    alreadyloaded =false;
                }
               /* if (intentData.length() > 0) {
                    if (isEmail)
                        startActivity(new Intent(ScannedBarcodeActivity.this, EmailActivity.class).putExtra("email_address", intentData));
                    else {
                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(intentData)));
                    }
                }*/


            }
        });
    }

    private void initialiseDetectorsAndSources() {

        Toast.makeText(getApplicationContext(), "Barcode scanner started", Toast.LENGTH_SHORT).show();

        barcodeDetector = new BarcodeDetector.Builder(this)
                .setBarcodeFormats(Barcode.ALL_FORMATS)
                .build();

        cameraSource = new CameraSource.Builder(this, barcodeDetector)
                .setRequestedPreviewSize(1920, 1080)
                .setAutoFocusEnabled(true) //you should add this feature
                .build();

        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                try {
                    if (ActivityCompat.checkSelfPermission(ScannedBarcodeActivity.this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(surfaceView.getHolder());
                    } else {
                        ActivityCompat.requestPermissions(ScannedBarcodeActivity.this, new
                                String[]{Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }


            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                cameraSource.stop();
            }
        });

        if (!alreadyloaded) {
            barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
                @Override
                public void release() {
//                    Toast.makeText(getApplicationContext(), "To prevent memory leaks barcode scanner has been stopped", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void receiveDetections(Detector.Detections<Barcode> detections) {
                    final SparseArray<Barcode> barcodes = detections.getDetectedItems();

                    if (barcodes.size() != 0) {


                        txtBarcodeValue.post(new Runnable() {

                            @Override
                            public void run() {

                                if (barcodes.valueAt(0).email != null) {
                                    txtBarcodeValue.removeCallbacks(null);
                                    intentData = barcodes.valueAt(0).email.address;
                                    txtBarcodeValue.setText(intentData);
                                    isEmail = true;
                                    btnAction.setText("ADD CONTENT TO THE MAIL");
                                } else {
                                    isEmail = false;
                                    btnAction.setText("LAUNCH URL");
                                    intentData = barcodes.valueAt(0).displayValue;
                                    txtBarcodeValue.setText(intentData);

                                    if (!intentData.equals(null)) {
                                        try {
                                            myimage.setVisibility(View.VISIBLE);
                                            surfaceView.setVisibility(View.GONE);
                                            btnAction.setText("Scan Another Code");
                                            btnAction.setVisibility(View.VISIBLE);
                                            alreadyloaded = true;
                                    /*        Picasso.with(ScannedBarcodeActivity.this)  //Here, this is context.
                                                    .load("https://scontent-mrs2-2.xx.fbcdn.net/v/t1.0-9/p960x960/45496751_2757766011115690_1480048500455505920_o.jpg?_nc_cat=107&_nc_sid=85a577&_nc_ohc=LNXrbzPbkD4AX-Ohie9&_nc_ht=scontent-mrs2-2.xx&_nc_tp=6&oh=d79dafe2a1267efbce6bf998b3eaa818&oe=5E8E621F")  //Url of the image to load.
                                                    .into(myimage);*/
                                            mProgressDialog.setMessage("Loading....");
                                            new Handler().postDelayed(new Runnable(){
                                                @Override
                                                public void run() {
                                                    mProgressDialog.show();
                                                }
                                            },100);
                                            Picasso.with(ScannedBarcodeActivity.this)  //Here, this is context.
                                                    .load("http://h2817272.stratoserver.net/FmfiPs/Portals/0/Fmfi_Files/UserImage/"+intentData+".PNG")  //Url of the image to load
                                                    .into(myimage);
//                                            Toast.makeText(ScannedBarcodeActivity.this, "http://h2817272.stratoserver.net/FmfiPs/Portals/0/Fmfi_Files/UserImage/"+intentData+".PNG", Toast.LENGTH_LONG).show();
                                            mProgressDialog.dismiss();

                                        } catch (Exception exc) {
                                            Toast.makeText(ScannedBarcodeActivity.this, "Error in image URL", Toast.LENGTH_LONG).show();
                                        }
                                    }
                                    else {
                                        Toast.makeText(ScannedBarcodeActivity.this, "the QR code is incorrect", Toast.LENGTH_LONG).show();

                                    }

                                }
                            }
                        });

                    }

                }
            });
        }
    }


    @Override
    protected void onPause() {
        super.onPause();
        cameraSource.release();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initialiseDetectorsAndSources();


    }
}
