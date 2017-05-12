package com.example.acedeno.customcamera;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

public class CustomCameraActivity extends Activity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;

    Button cancel;
    ImageButton snapshot;
    Button paginator;
    Button multipages;
    LinearLayout progress;

    boolean multipaged = false;

    private ArrayList<String> pagepath = new ArrayList<String>();

    private static final int NOSCONECTA_CAMERA_PERMISSION = 1;
    private static final int CAMERA_ID = 0;
    private static final String NOSCONECTA_FOLDERS = "/NosConecta/Photos/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("main", "layout", getPackageName()));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.i("XXX", "ONCREATEEEEE");
        getWindow().setFormat(PixelFormat.UNKNOWN);
        surfaceView = (SurfaceView)findViewById(getResources().getIdentifier("camerapreview", "id", getPackageName()));
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        controlInflater = LayoutInflater.from(getBaseContext());
        View viewControl = controlInflater.inflate(getResources().getIdentifier("custom", "layout", getPackageName()), null);

        LayoutParams layoutParamsControl = new LayoutParams(LayoutParams.FILL_PARENT,
                LayoutParams.FILL_PARENT);
        this.addContentView(viewControl, layoutParamsControl);
    }


    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            // TODO Do something when the shutter closes.

        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
            // TODO Do something with the image RAW data.
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera _camera) {
            // TODO Do something with the image JPEG data.
            BitmapFactory.Options bfo = new BitmapFactory.Options();
            bfo.inPreferredConfig = Bitmap.Config.RGB_565;
            Bitmap photo = BitmapFactory.decodeStream(new ByteArrayInputStream(data), null, bfo);
            try {
                Camera.CameraInfo info = new Camera.CameraInfo();
                Camera.getCameraInfo(CAMERA_ID, info);
                Bitmap bitmap = rotate(photo, info.orientation);

                SecureRandom sRand = new SecureRandom();
                String filename = new BigInteger(130, sRand).toString(32) + ".jpg";
                String filepath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        NOSCONECTA_FOLDERS + filename;
                
                OutputStream stream = new FileOutputStream(filepath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                camera.stopPreview();

                pagepath.add(filepath);

                if(multipaged){
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.GONE);

                            //TODO: Mover este bloque que actualiza las páginas a alguna función

                            paginator.setText(pagepath.size() + " PÁGINAS");
                            if(pagepath.size() > 0){
                                paginator.setCompoundDrawablesWithIntrinsicBounds(0, 0, getResources().getIdentifier("ic_arrow_right", "drawable", getPackageName()), 0);
                            }

                            camera.startPreview();
                        }
                    }, 2000);

                } else {
                    

                    Intent response = new Intent();
                    response.putExtra("result", pagepath);
                    setResult(Activity.RESULT_OK, response);
                    finish();

                    //startActivity(getIntent());
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    };

    public static Bitmap rotate(Bitmap bitmap, int degree) {
        int width  = bitmap.getWidth();
        int height = bitmap.getHeight();

        Matrix matrix = new Matrix();
        matrix.postRotate(degree);

        return Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
    }

    public String createDirectories(){
        String directory = Environment.getExternalStorageDirectory().getAbsolutePath() +
                NOSCONECTA_FOLDERS;
        File directories = new File(directory);
        if(!directories.exists()){
            if(directories.mkdirs()){
                return directory;
            } else {
                return null;
            }
        }

        return directory;
    }

    public void setPermissions(){
        ActivityCompat.requestPermissions(CustomCameraActivity.this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, NOSCONECTA_CAMERA_PERMISSION);
    }

    public boolean hasPermissions(){

        return (ActivityCompat.checkSelfPermission(CustomCameraActivity.this, Manifest.permission.CAMERA) +
                ActivityCompat.checkSelfPermission(CustomCameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) ? false : true;
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

            Log.i("XXX", "Pasa x aqui");
            if(requestCode == NOSCONECTA_CAMERA_PERMISSION) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    Log.i("XXX", "tmb Pasa x aqui");

                    camera = Camera.open(CAMERA_ID);
                    try {
                        camera.setDisplayOrientation(90);
                        camera.setPreviewDisplay(surfaceHolder);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    camera.startPreview();
                    previewing = true;

                } else {

                    
                }
                return;
            }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("XXX", "Surface changed event");
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();

                previewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }else{
            Log.i("XXX", "camera es null");
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.i("XXX", "Surface created");

        progress = (LinearLayout)findViewById(getResources().getIdentifier("progressbar", "id", getPackageName()));

        cancel = (Button)findViewById(getResources().getIdentifier("Cancel", "id", getPackageName()));
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent result = new Intent();
                setResult(Activity.RESULT_CANCELED, result);
                finish();
                //System.exit(0);
            }
        });

        snapshot = (ImageButton)findViewById(getResources().getIdentifier("Snapshot", "id", getPackageName()));
        snapshot.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
            }
        });


        paginator = (Button)findViewById(getResources().getIdentifier("Paginator", "id", getPackageName()));

        paginator.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(pagepath.size() > 0){
                    

                    Intent response = new Intent();
                    response.putStringArrayListExtra("result", pagepath);
                    setResult(Activity.RESULT_OK, response);
                    finish();

                } else {
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        multipages = (Button)findViewById(getResources().getIdentifier("Multipages", "id", getPackageName()));
        Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_multipages", "mipmap", getPackageName()));
        int color = getResources().getColor(getResources().getIdentifier("gray", "color", getPackageName()));
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        multipages.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        multipages.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!multipaged){
                    multipages.setTextColor(getResources().getColorStateList(getResources().getIdentifier("red", "color", getPackageName())));
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_multipages", "mipmap", getPackageName()));
                    int color = getResources().getColor(getResources().getIdentifier("red", "color", getPackageName()));
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                    multipages.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    multipaged = true;
                } else {
                    multipages.setTextColor(getResources().getColorStateList(getResources().getIdentifier("gray", "color", getPackageName())));
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_multipages", "mipmap", getPackageName()));
                    int color = getResources().getColor(getResources().getIdentifier("gray", "color", getPackageName()));
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                    multipages.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
                    multipaged = false;
                }
            }
        });

        //TODO: Mover este bloque que actualiza las páginas a alguna función
        paginator.setText(pagepath.size() + " PÁGINAS");
        if(pagepath.size() > 0){
            paginator.setCompoundDrawablesWithIntrinsicBounds(0, 0, getResources().getIdentifier("ic_arrow_right", "drawable", getPackageName()), 0);
        }

        if(hasPermissions()){
            camera = Camera.open(CAMERA_ID);
        }else{
            setPermissions();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("XXX", "surface destroyed");
        camera.stopPreview();
        camera.release();
        camera = null;

        previewing = false;
    }
}
