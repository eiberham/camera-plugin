package com.example.acedeno.customcamera;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
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
import android.os.Build;

import android.support.design.widget.FloatingActionButton;
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

import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.Document;

import org.json.JSONArray;

public class CustomCameraActivity extends Activity implements SurfaceHolder.Callback {

    static Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;
    LayoutInflater controlInflater = null;

    Button cancel;
    ImageButton snapshot;
    Button paginator;
    Button flash;
    LinearLayout progress;
    Context context;

    boolean flashed = false;

    private ArrayList<String> pagepath = new ArrayList<String>();

    private static final int NOSCONECTA_CAMERA_PERMISSION = 1;
    private static final int CAMERA_ID = 0;
    private static final int CAMERA_DEFAULT_WIDTH = 1280;
    private static final int CAMERA_DEFAULT_HEIGHT = 720;
    private static final String NOSCONECTA_FOLDERS = "/NosConecta/Photos/";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getResources().getIdentifier("main", "layout", getPackageName()));

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

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
        Log.i("XXX", "Terminó el onCreate :)");
    }

     @Override
    protected void onPause() {
        /*if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }*/
        Log.i("XXX", "Se puso en pausa");
        super.onPause();
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i("XXX", "Se puso en resumen :)");
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
                Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
                Bitmap bitmap = rotate(photo, info.orientation);

                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                        NOSCONECTA_FOLDERS);

                if(!dir.exists()) {
                    dir.mkdirs();
                }

                SecureRandom sRand = new SecureRandom();
                String filename = new BigInteger(130, sRand).toString(32) + ".jpg";
                String filepath = dir.getAbsolutePath() + filename;
                
                OutputStream stream = new FileOutputStream(filepath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                camera.stopPreview();

                pagepath.add(filepath);

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

    public void setPermissions(){
        Log.i("XXX", "pasa por SetPermissions");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityCompat.requestPermissions(CustomCameraActivity.this, new String[]{
                Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, NOSCONECTA_CAMERA_PERMISSION);
        }
    }

    public boolean hasPermissions(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Log.i("XXX", "Es mayor o igual a LOLIPOP");
            return (ActivityCompat.checkSelfPermission(CustomCameraActivity.this, Manifest.permission.CAMERA) +
                    ActivityCompat.checkSelfPermission(CustomCameraActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) ? false : true;
        } else {
            Log.i("XXX", "Es menor a LOLIPOP");
            return true;
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {

            if(requestCode == NOSCONECTA_CAMERA_PERMISSION) {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    try {
                        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                        camera.setDisplayOrientation(90);
                        Camera.Parameters parameters = camera.getParameters();
                        parameters.setPictureSize(CAMERA_DEFAULT_WIDTH, CAMERA_DEFAULT_HEIGHT);
                        camera.setParameters(parameters);
                        camera.setPreviewDisplay(surfaceHolder);
                    } catch (IOException e) {
                        Log.i("XXX", "Excepcion camara 1");
                        e.printStackTrace();
                    }
                    camera.startPreview();
                    previewing = true;

                } else {

                    Toast.makeText(getBaseContext(),"You must give permission in order to use the " +
                                    "camera",
                            Toast.LENGTH_LONG).show();
                }
                return;
            }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.i("XXX", "surface changed !!");
        if(previewing){
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null){
            try {
                camera.setDisplayOrientation(90);
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureSize(CAMERA_DEFAULT_WIDTH, CAMERA_DEFAULT_HEIGHT);
                camera.setParameters(parameters);
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
        Log.i("XXX", "Surface created!! ");
        progress = (LinearLayout)findViewById(getResources().getIdentifier("progressbar", "id", getPackageName()));

        cancel = (Button)findViewById(getResources().getIdentifier("Cancel", "id", getPackageName()));
        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(deletePictures(pagepath)){
                    Intent result = new Intent();
                    setResult(Activity.RESULT_CANCELED, result);
                    finish();
                }
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
                    String pdfpath = "file://";
                    try {
                        pdfpath += createPdf();
                        deletePictures(pagepath);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (DocumentException e) {
                        e.printStackTrace();
                    }

                    Log.i("XXX", "Respondo ok y devuelvo el path del PDF");
                    Log.i("XXX", "Activity.ResultOk: " + Activity.RESULT_OK);
                    Intent response = new Intent();
                    response.putExtra("result", pdfpath);
                    setResult(Activity.RESULT_OK, response);
                    finish();

                } else {
                    finish();
                    startActivity(getIntent());
                }
            }
        });

        flash = (Button)findViewById(getResources().getIdentifier("Flash", "id", getPackageName()));
        Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_flash", "mipmap", getPackageName()));
        int color = getResources().getColor(getResources().getIdentifier("gray", "color", getPackageName()));
        drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
        flash.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

        flash.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(!flashed){
                    flash.setTextColor(getResources().getColorStateList(getResources().getIdentifier("red", "color", getPackageName())));
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_flash", "mipmap", getPackageName()));
                    int color = getResources().getColor(getResources().getIdentifier("red", "color", getPackageName()));
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                    flash.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

                    camera.stopPreview();
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    camera.setParameters(parameters);
                    camera.startPreview();

                    flashed = true;
                } else {
                    flash.setTextColor(getResources().getColorStateList(getResources().getIdentifier("gray", "color", getPackageName())));
                    Drawable drawable = getResources().getDrawable(getResources().getIdentifier("ic_flash", "mipmap", getPackageName()));
                    int color = getResources().getColor(getResources().getIdentifier("gray", "color", getPackageName()));
                    drawable.setColorFilter(new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN));
                    flash.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);

                    camera.stopPreview();
                    Camera.Parameters parameters = camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                    camera.setParameters(parameters);
                    camera.startPreview();

                    flashed = false;
                }
            }
        });

        //TODO: Mover este bloque que actualiza las páginas a alguna función
        paginator.setText(pagepath.size() + " PÁGINAS");
        if(pagepath.size() > 0){
            paginator.setCompoundDrawablesWithIntrinsicBounds(0, 0, getResources().getIdentifier("ic_arrow_right", "drawable", getPackageName()), 0);
        }

        if(hasPermissions()){
            Log.i("XXX", "Tiene permisos");
            try{
                if(camera == null) {
                    try{
                        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    

                } else {
                    Log.i("XXX", "camara No es null ya esta seteada");
                }
            }catch (Exception e){
                Log.i("XXX", "Excepcion camara 2");
                e.printStackTrace();
            }
            
        }else{
            setPermissions();
        }
    }

    public boolean deletePictures(ArrayList<String> pictures){
        boolean ok = true;
        for(int i = 0; i <= pictures.size() -1; i++){
            File file = new File(pictures.get(i));
            boolean deleted = file.delete();
            if(!deleted){
                ok = false;
            }
        }
        return ok;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i("XXX", "Surface destroyed !!");
        if(camera != null){
            camera.stopPreview();
            camera.release();
            camera = null;

            previewing = false;
        }
    }

    private String createPdf() throws FileNotFoundException, DocumentException {
        SecureRandom sRand = new SecureRandom();
        String filename = new BigInteger(130, sRand).toString(32) + ".pdf";
        String target_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                NOSCONECTA_FOLDERS + filename;

        File myFile = new File(target_path);

        OutputStream output = new FileOutputStream(myFile);

        //Step 1
        Document document = new Document();

        //Step 2
        try {
            PdfWriter.getInstance(document, output);
        } catch (DocumentException e) {
            e.printStackTrace();
        }

        //Step 3
        document.open();
        

        for(int i = 0; i <= pagepath.size() -1; i ++){

            try {
                Image image = Image.getInstance(pagepath.get(i));
                image.setAlignment(Image.MIDDLE);
                image.scaleToFit((PageSize.A4.getWidth() -25), (PageSize.A4.getHeight()-25));
                document.add(image);
            } catch (IOException e) {
                e.printStackTrace();
            }
            document.newPage();
        }

        //Step 5: Close the document
        document.close();

        return target_path;
    }
}
