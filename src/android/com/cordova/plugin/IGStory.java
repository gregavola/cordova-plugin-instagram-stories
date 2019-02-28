/**
 */
package com.cordova.plugin;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.apache.cordova.PluginResult.Status;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import android.content.pm.PackageManager;

import android.util.Log;

import java.net.URLConnection;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import android.net.Uri;
import android.content.Intent;
import android.app.Activity;
import  java.net.URL;
import java.io.File;
import java.io.IOException;
import android.support.v4.content.FileProvider;

public class IGStory extends CordovaPlugin {
  private static final String TAG = "IGStory";
  private CallbackContext callback = null;

  public void initialize(CordovaInterface cordova, CordovaWebView webView) {
    super.initialize(cordova, webView);

    Log.d(TAG, "Initializing IGStory");
  }

  public boolean execute(String action, JSONArray args, final CallbackContext callbackContext) throws JSONException {
    if (action.equals("shareToStory")) {

      PackageManager pm = this.cordova.getActivity().getBaseContext().getPackageManager();

      if (isPackageInstalled("com.instagram.android", pm)) {
        String backgroundImageUrl = args.getString(0);
        String stickerAssetUrl = args.getString(1);
        String attributionLinkUrl = args.getString(2);
        String backgroundTopColor = args.getString(3);
        String backgroundBottomColor = args.getString(4);

        Log.e(TAG, backgroundImageUrl);
        Log.e(TAG, stickerAssetUrl);
        Log.e(TAG, attributionLinkUrl);

        if (backgroundTopColor != "" && backgroundBottomColor != "") {

          try {
            File stickerImageFile = null;
            FileOutputStream os = null;

            File parentDir = this.webView.getContext().getExternalFilesDir(null);

            stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);

            downloadFile(stickerAssetUrl, stickerImageFile);

            Uri stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);

            Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
            intent.setType("image/jpeg");
            intent.putExtra("interactive_asset_uri", stickerUri);
            intent.putExtra("content_url", attributionLinkUrl);
            intent.putExtra("top_background_color", backgroundTopColor);
            intent.putExtra("bottom_background_color", backgroundBottomColor);

            // Instantiate activity and verify it will resolve implicit intent
            Activity activity = this.cordova.getActivity();
            activity.grantUriPermission(
                    "com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.startActivityForResult(intent, 0);
            callbackContext.success("shared");
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }

        } else {
          try {

            File backgroundImageFile = null;
            File stickerImageFile = null;
            FileOutputStream os = null;

            File parentDir = this.webView.getContext().getExternalFilesDir(null);

            backgroundImageFile = File.createTempFile("instagramBackground", ".png", parentDir);
            stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);

            downloadFile(backgroundImageUrl, backgroundImageFile);
            downloadFile(stickerAssetUrl, stickerImageFile);


            Uri backgroundUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,backgroundImageFile);
            Uri stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);

            // Instantiate implicit intent with ADD_TO_STORY action,
            // background asset, sticker asset, and attribution link
            Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(backgroundUri, "image/jpeg");
            intent.putExtra("interactive_asset_uri", stickerUri);
            intent.putExtra("content_url", attributionLinkUrl);

            // Instantiate activity and verify it will resolve implicit intent
            Activity activity = this.cordova.getActivity();
            activity.grantUriPermission(
                    "com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            activity.startActivityForResult(intent, 0);
            callbackContext.success("shared");
          } catch (Exception e) {
            callbackContext.error(e.getMessage());
          }
        }
      } else {
        callbackContext.error("ig not installed");
      }


    }

    return true;
  }

  private boolean isPackageInstalled(String packageName, PackageManager packageManager) {

    boolean found = true;

    try {

      packageManager.getPackageInfo(packageName, 0);
    } catch (PackageManager.NameNotFoundException e) {

      found = false;
    }

    return found;
  }


  private static void downloadFile(String url, File outputFile) {
    try {
      URL u = new URL(url);
      URLConnection conn = u.openConnection();
      int contentLength = conn.getContentLength();

      DataInputStream stream = new DataInputStream(u.openStream());

      byte[] buffer = new byte[contentLength];
      stream.readFully(buffer);
      stream.close();

      DataOutputStream fos = new DataOutputStream(new FileOutputStream(outputFile));
      fos.write(buffer);
      fos.flush();
      fos.close();
    } catch (FileNotFoundException e) {
      Log.e(TAG, e.getMessage());
    } catch (IOException e) {
      Log.e(TAG, e.getMessage());
    }

  }
}
