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
import android.widget.Toast;
import android.app.Activity;
import android.util.Log;
import android.content.Context;

import java.util.ArrayList;


public class CustomCameraPlugin extends CordovaPlugin{


    private static final String CAMERA = "customCamera";
    private static final int GET_PICTURES_REQUEST = 1;
    private CallbackContext callback;


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
                    intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                    if(this.self.cordova != null)
                        this.self.cordova.startActivityForResult((CordovaPlugin)this.self, intent, GET_PICTURES_REQUEST);

                }
            }

            this.cordova.getActivity().runOnUiThread(new Snapshot(callbackContext, this));

            PluginResult r = new PluginResult(PluginResult.Status.OK);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);

            return true;
        }

        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("XXX", "Pasa por activityResult");
        Log.i("XXX", "resultok: " + cordova.getActivity().RESULT_OK);
        Log.i("XXX", "resultCode" + resultCode);
        
        
        if (requestCode == GET_PICTURES_REQUEST && callback != null) {
            if (resultCode == cordova.getActivity().RESULT_OK) {
                Log.i("XXX", "Responde OK");
                Bundle extras = intent.getExtras();
                String result = extras.getString("result"); Log.i("XXX", "Responde success");
                callback.success(result);

            } else {
                Log.i("XXX", "Responde failure");
                PluginResult r = new PluginResult(PluginResult.Status.OK);
                r.setKeepCallback(true);
                callback.sendPluginResult(r);

            }
        }
        

        //resultCode = Activity.RESULT_OK;
    }
}
