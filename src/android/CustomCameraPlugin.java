package com.example.acedeno.customcamera;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CordovaInterface;


import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;


public class CustomCameraPlugin extends CordovaPlugin{


    private static final String CAMERA = "customCamera";
    private static final int GET_PICTURES_REQUEST = 1;


    public CustomCameraPlugin() {}

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if(action.equals(CAMERA)){
            Log.i("XXX", "pasa por camera");

            class Snapshot implements Runnable {
                private CallbackContext callback;
                private CustomCameraPlugin self;
                Snapshot(CallbackContext callbackContext, CustomCameraPlugin self){
                    this.callback = callbackContext;
                    this.self     = self;
                }
                public void run(){
                    Intent intent = new Intent(self.cordova.getActivity(), CustomCameraActivity.class);

                    if(this.self.cordova != null)
                        this.self.cordova.startActivityForResult((CordovaPlugin)this.self, intent, 1);

                    PluginResult r = new PluginResult(PluginResult.Status.OK);
                    r.setKeepCallback(true);
                    this.callback.sendPluginResult(r);
                }
            }

            this.cordova.getActivity().runOnUiThread(new Snapshot(callbackContext, this));

            return true;
        }

        return false;
    }

    private void init(CallbackContext callbackContext){

        
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);
        
        /*if (requestCode == GET_PICTURES_REQUEST && callback != null) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
                Bundle extras = intent.getExtras();
                //ArrayList<String> result = extras.getStringArrayList("result");

                PluginResult r = new PluginResult(PluginResult.Status.OK);
                r.setKeepCallback(true);
                callback.sendPluginResult(r);

            } else {

                PluginResult r = new PluginResult(PluginResult.Status.OK);
                r.setKeepCallback(true);
                callback.sendPluginResult(r);

            }
        }*/

        resultCode = Activity.RESULT_OK;
    }
}
