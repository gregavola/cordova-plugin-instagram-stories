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
import java.io.ByteArrayOutputStream;

import android.util.Log;

import java.io.FileOutputStream;
import android.net.Uri;
import android.content.Intent;
import android.app.Activity;
import  java.net.URL;
import java.io.File;
import java.io.IOException;
import android.support.v4.content.FileProvider;
import java.io.InputStream;
import android.os.Build;

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

        if (!backgroundTopColor.isEmpty() && !backgroundBottomColor.isEmpty()) {
          Log.e(TAG, "TOP COLOR HERE");
          try {
            File parentDir = this.webView.getContext().getExternalFilesDir(null);
            File stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);
            Uri stickerUri = null;

            URL u = new URL(stickerAssetUrl);
            saveImage(u, stickerImageFile);

            String type = "image/*";

            Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
            intent.setType(type);
            intent.putExtra("content_url", attributionLinkUrl);
            intent.putExtra("top_background_color", backgroundTopColor);
            intent.putExtra("bottom_background_color", backgroundBottomColor);


            /*if (Build.VERSION.SDK_INT < 26) {
              // Handle the file uri with pre Oreo method
              stickerUri = Uri.fromFile(stickerImageFile);
              intent.putExtra("interactive_asset_uri", stickerUri);
            } else {*/
              // Handle the file URI using Android Oreo file provider
              FileProvider FileProvider = new FileProvider();
              stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);
              intent.putExtra("interactive_asset_uri", stickerUri);
            //}

            // Instantiate activity and verify it will resolve implicit intent
            Activity activity = this.cordova.getActivity();
            activity.grantUriPermission(
                    "com.instagram.android", stickerUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);

            //this.cordova.startActivityForResult(this, intent, 0);
            Log.e(TAG,Build.VERSION.SDK);
            activity.startActivityForResult(intent, 0);
            callbackContext.success("shared");
          } catch (Exception e) {
            Log.e(TAG, "We have an exception!");
            Log.e(TAG, e.getMessage());
            callbackContext.error(e.getMessage());
          }

        } else {
          try {
            Log.e(TAG, "WE HAVE A BACKGROUND");
            File parentDir = this.webView.getContext().getExternalFilesDir(null);
            File backgroundImageFile = File.createTempFile("instagramBackground", ".png", parentDir);
            File stickerImageFile = File.createTempFile("instagramSticker", ".png", parentDir);
            Uri stickerUri = null;
            Uri backgroundUri = null;

            URL stickerURL = new URL(stickerAssetUrl);
            saveImage(stickerURL, stickerImageFile);

            URL backgroundURL = new URL(backgroundImageUrl);
            saveImage(backgroundURL, backgroundImageFile);

            Log.e(TAG, backgroundImageFile.toString());

            //Uri backgroundUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,backgroundImageFile);
            //Uri stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);

            // Instantiate implicit intent with ADD_TO_STORY action,
            // background asset, sticker asset, and attribution link
            Intent intent = new Intent("com.instagram.share.ADD_TO_STORY");
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

           /* if (Build.VERSION.SDK_INT < 26) {
              // Handle the file uri with pre Oreo method
              stickerUri = Uri.fromFile(stickerImageFile);
              backgroundUri = Uri.fromFile(stickerImageFile);
              intent.setDataAndType(backgroundUri, "image/*");
              intent.putExtra("interactive_asset_uri", stickerUri);*/
            //} else {
              // Handle the file URI using Android Oreo file provider
              FileProvider FileProvider = new FileProvider();
              stickerUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,stickerImageFile);
              backgroundUri = FileProvider.getUriForFile(this.cordova.getActivity().getBaseContext(), this.cordova.getActivity().getBaseContext().getPackageName() + ".provider" ,backgroundImageFile);

              intent.setDataAndType(backgroundUri, "image/*");
              intent.putExtra("interactive_asset_uri", stickerUri);
           // }

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

  private byte[] downloadUrl(URL toDownload) {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

    try {
      byte[] chunk = new byte[4096];
      int bytesRead;
      InputStream stream = toDownload.openStream();

      while ((bytesRead = stream.read(chunk)) > 0) {
        outputStream.write(chunk, 0, bytesRead);
      }

    } catch (IOException e) {
      Log.e(TAG, "SAVE ERROR (IO): " + e.getMessage());
      return null;
    } catch (Exception e) {
      Log.e(TAG, "SAVE ERROR (REG): " + e.getMessage());
      return null;
    }

    return outputStream.toByteArray();
  }

  private void saveImage(URL pathUrl, File file) {
    FileOutputStream os = null;

    try {
      os = new FileOutputStream(file, true);
    } catch (Exception e) {
      e.printStackTrace();
    }

    try {
      os.write(downloadUrl(pathUrl));
      os.flush();
      os.close();
    } catch (IOException e) {
      // TODO Auto-generated catch block
      Log.e(TAG, "SAVE ERROR: " + e.getMessage());
    }
  }
}
