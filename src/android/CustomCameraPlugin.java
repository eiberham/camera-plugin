package com.example.acedeno.customcamera;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.apache.cordova.PluginResult;


import android.content.Intent;
import android.os.Bundle;

import java.util.ArrayList;

public class CustomCameraPlugin extends CordovaPlugin{


    private static final String CAMERA = "camera";
    private static final int GET_PICTURES_REQUEST = 1;
    CallbackContext callback;


    public CustomCameraPlugin() {}

    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        callback = callbackContext;
        if(action.equals(CAMERA)){
            Intent intent = new Intent(this.cordova.getActivity(), CustomCameraActivity.class);

            if(this.cordova != null)
                this.cordova.startActivityForResult((CordovaPlugin) this, intent, GET_PICTURES_REQUEST);

            PluginResult r = new PluginResult(PluginResult.Status.OK);
            r.setKeepCallback(true);
            callbackContext.sendPluginResult(r);

            return true;
        }

        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        // TODO Auto-generated method stub
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == GET_PICTURES_REQUEST && callback != null) {
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
        }
    }
}
