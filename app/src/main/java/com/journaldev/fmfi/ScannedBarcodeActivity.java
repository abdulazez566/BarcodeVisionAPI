package com.journaldev.fmfi;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.PointF;
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


import com.journaldev.fmfi.setting.Global;
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

    float[] lastEvent = null;
    float d = 0f;
    float newRot = 0f;
    private boolean isZoomAndRotate;
    private boolean isOutSide;
    private static final int NONE = 0;
    private static final int DRAG = 1;
    private static final int ZOOM = 2;
    private int mode = NONE;
    private PointF start = new PointF();
    private PointF mid = new PointF();
    float oldDist = 1f;
    private float xCoOrdinate, yCoOrdinate;

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
//    com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView myimage_zoom;
    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    ImageView myimage;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_barcode);
        myimage = findViewById(R.id.myimage);
//        myimage_zoom=findViewById(R.id.myimage_zoom);

        initViews();
        mProgressDialog = new ProgressDialog(this,R.style.AppCompatAlertDialogStyle);
        mProgressDialog.setCancelable(false);
        //abd
//        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
        myimage.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                ImageView view = (ImageView) v;
                view.bringToFront();
                viewTransformation(view, event);
                return true;
            }
        });
    }
    //abd
   /* @Override
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
    }*/
   //abd
    private void viewTransformation(View view, MotionEvent event) {
        switch (event.getAction() & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                xCoOrdinate = view.getX() - event.getRawX();
                yCoOrdinate = view.getY() - event.getRawY();

                start.set(event.getX(), event.getY());
                isOutSide = false;
                mode = DRAG;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                oldDist = spacing(event);
                if (oldDist > 10f) {
                    midPoint(mid, event);
                    mode = ZOOM;
                }

                lastEvent = new float[4];
                lastEvent[0] = event.getX(0);
                lastEvent[1] = event.getX(1);
                lastEvent[2] = event.getY(0);
                lastEvent[3] = event.getY(1);
                d = rotation(event);
                break;
            case MotionEvent.ACTION_UP:
                isZoomAndRotate = false;
                if (mode == DRAG) {
                    float x = event.getX();
                    float y = event.getY();
                }
            case MotionEvent.ACTION_OUTSIDE:
                isOutSide = true;
                mode = NONE;
                lastEvent = null;
            case MotionEvent.ACTION_POINTER_UP:
                mode = NONE;
                lastEvent = null;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false;
                        view.animate().x(event.getRawX() + xCoOrdinate).y(event.getRawY() + yCoOrdinate).setDuration(0).start();
                    }
                    if (mode == ZOOM && event.getPointerCount() == 2) {
                        float newDist1 = spacing(event);
                        if (newDist1 > 10f) {
                            float scale = newDist1 / oldDist * view.getScaleX();
                            view.setScaleX(scale);
                            view.setScaleY(scale);
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event);
                            view.setRotation((float) (view.getRotation() + (newRot - d)));
                        }
                    }
                }
                break;
        }
    }

    private float rotation(MotionEvent event) {
        double delta_x = (event.getX(0) - event.getX(1));
        double delta_y = (event.getY(0) - event.getY(1));
        double radians = Math.atan2(delta_y, delta_x);
        return (float) Math.toDegrees(radians);
    }

    private float spacing(MotionEvent event) {
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (int) Math.sqrt(x * x + y * y);
    }

    private void midPoint(PointF point, MotionEvent event) {
        float x = event.getX(0) + event.getX(1);
        float y = event.getY(0) + event.getY(1);
        point.set(x / 2, y / 2);
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
//                    myimage_zoom.setVisibility(View.GONE);
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
//                                            myimage_zoom.setVisibility(View.VISIBLE);
                                            surfaceView.setVisibility(View.GONE);
                                            btnAction.setText("Scan Another Code");
                                            btnAction.setVisibility(View.VISIBLE);
                                            alreadyloaded = true;
                                    /*        Picasso.with(ScannedBarcodeActivity.this)  //Here, this is context.
                                                    .load("https://scontent-mrs2-2.xx.fbcdn.net/v/t1.0-9/p960x960/45496751_2757766011115690_1480048500455505920_o.jpg?_nc_cat=107&_nc_sid=85a577&_nc_ohc=LNXrbzPbkD4AX-Ohie9&_nc_ht=scontent-mrs2-2.xx&_nc_tp=6&oh=d79dafe2a1267efbce6bf998b3eaa818&oe=5E8E621F")  //Url of the image to load.
                                                    .into(myimage);*/
                                    /*        mProgressDialog.setMessage("Loading....");
                                            new Handler().postDelayed(new Runnable(){
                                                @Override
                                                public void run() {
                                                    mProgressDialog.show();
                                                }
                                            },1);*/
                                            Picasso.with(ScannedBarcodeActivity.this)  //Here, this is context.
                                                    .load(Global.ImageUrl +intentData+".PNG")  //Url of the image to load
                                                    .networkPolicy(NetworkPolicy.NO_CACHE)
                                                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                                                    .fit().centerInside()
                                                    .rotate(90)
                                                    .placeholder( R.drawable.loader)
                                                    .into(myimage);

//                                            Toast.makeText(ScannedBarcodeActivity.this, "http://h2817272.stratoserver.net/FmfiPs/Portals/0/Fmfi_Files/UserImage/"+intentData+".PNG", Toast.LENGTH_LONG).show();
/*
                                            mProgressDialog.dismiss();
*/

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
