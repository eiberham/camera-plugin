package com.example.acedeno.customcamera;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;


public class CustomCameraPlugin extends CordovaPlugin{

    private static final String CAMERA = "customCamera";
    private static final String IMAGES = "images";
    private static final int GET_PICTURES_REQUEST = 1;
    private CallbackContext callback;
    private boolean running = false;
    private ArrayList<String> pagepath;


    public CustomCameraPlugin() {}

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals(CAMERA)){
            this.callback = callbackContext;
            class Snapshot implements Runnable {
                private CallbackContext callback;
                private CustomCameraPlugin self;
                Snapshot(CallbackContext callbackContext, CustomCameraPlugin self){
                    this.callback = callbackContext;
                    this.self     = self;
                }
                public void run(){
                    Intent intent = new Intent(self.cordova.getActivity(), CustomCameraActivity.class);
                    if(this.self.cordova != null){
                        this.self.cordova.startActivityForResult((CordovaPlugin)this.self, intent, GET_PICTURES_REQUEST);
                    }

                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                this.cordova.getActivity().runOnUiThread(new Snapshot(callbackContext, this));
            }else{
                if(!this.running){
                    this.cordova.getActivity().runOnUiThread(new Snapshot(callbackContext, this));
                    this.running = true;
                }
            }

            return true;
        } else if(action.equals(IMAGES)){
            JSONArray jsArr = args.getJSONArray(0);
            if (jsArr != null){
                for(int i = 0;  i < jsArr.length(); i ++){
                    this.pagepath.add(jsArr.getString(i));
                }

                ImagesManager im = new ImagesManager(this.pagepath);
                String pdfPath = im.createPdf();

                PluginResult r = new PluginResult(PluginResult.Status.OK, pdfPath);
                r.setKeepCallback(true);
                callbackContext.sendPluginResult(r);
            }
            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        
        if (requestCode == GET_PICTURES_REQUEST && callback != null) {
            this.running = false;
            if (resultCode == cordova.getActivity().RESULT_OK) {
                Bundle extras = intent.getExtras();
                String result = extras.getString("result");
                callback.success(result);

            } else {
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                r.setKeepCallback(true);
                callback.sendPluginResult(r);
            }
        }
    }
}
