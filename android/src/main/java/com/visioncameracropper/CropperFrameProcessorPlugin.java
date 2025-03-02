package com.visioncameracropper;
import android.graphics.Bitmap;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mrousavy.camera.core.FrameInvalidError;
import com.mrousavy.camera.frameprocessors.Frame;
import com.mrousavy.camera.frameprocessors.FrameProcessorPlugin;
import com.mrousavy.camera.frameprocessors.VisionCameraProxy;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CropperFrameProcessorPlugin extends FrameProcessorPlugin {
  static public Bitmap frameTaken;
  CropperFrameProcessorPlugin(@NonNull VisionCameraProxy proxy, @Nullable Map<String, Object> options) {super();}

  @Nullable
  @Override
  public Object callback(@NonNull Frame frame, @Nullable Map<String, Object> arguments) {
    Map<String, Object> result = new HashMap<String, Object>();
    try {
        // Get the bitmap from the frame
        Bitmap bm = BitmapUtils.getBitmap(frame);
        Log.d("CropperFrameProcessorPlugin", "Original Bitmap dimensions: " + bm.getWidth() + "x" + bm.getHeight());

        if (arguments != null && arguments.containsKey("cropRegion")) {
            Map<String, Object> cropRegion = (Map<String, Object>) arguments.get("cropRegion");

            // Get the crop region values (percent)
            double left = (double) cropRegion.get("left");
            double top = (double) cropRegion.get("top");
            double width = (double) cropRegion.get("width");
            double height = (double) cropRegion.get("height");
            
            // Convert percentages to pixel values
            //double left = (leftPercent / 100.0) * bm.getWidth();
            //double top = (topPercent / 100.0) * bm.getHeight();
            //double width = (widthPercent / 100.0) * bm.getWidth();
            //double height = (heightPercent / 100.0) * bm.getHeight();

            Log.d("CropperFrameProcessorPlugin", "Converted crop region (pixels): Left = " + left + "px, Top = " + top + "px, Width = " + width + "px, Height = " + height + "px");

            // Ensure the crop does not exceed the bounds of the bitmap
            if (left + width > bm.getWidth()) {
                width = bm.getWidth() - left;  // Adjust width to fit within the image
                Log.d("CropperFrameProcessorPlugin", "Adjusted width to fit within the image: " + width + "px");
            }
            if (top + height > bm.getHeight()) {
                height = bm.getHeight() - top;  // Adjust height to fit within the image
                Log.d("CropperFrameProcessorPlugin", "Adjusted height to fit within the image: " + height + "px");
            }

            // Crop the bitmap using the adjusted values
            bm = Bitmap.createBitmap(bm, (int) left, (int) top, (int) width, (int) height, null, false);
            bm = Bitmap.createScaledBitmap(bm, 112, 112, true);
            Log.d("CropperFrameProcessorPlugin", "Cropped bitmap dimensions: " + bm.getWidth() + "x" + bm.getHeight());
        }

        // Optionally, handle Base64 or file saving logic
        if (arguments != null && arguments.containsKey("includeImageBase64")) {
            boolean includeImageBase64 = (boolean) arguments.get("includeImageBase64");
            if (includeImageBase64) {
                result.put("base64", BitmapUtils.bitmap2Base64(bm));
                Log.d("CropperFrameProcessorPlugin", "Base64 encoded image included");
            }
        }

        if (arguments != null && arguments.containsKey("saveBitmap")) {
            boolean saveBitmap = (boolean) arguments.get("saveBitmap");
            if (saveBitmap) {
                frameTaken = bm;
                Log.d("CropperFrameProcessorPlugin", "Bitmap saved");
            }
        }

        if (arguments != null && arguments.containsKey("saveAsFile")) {
            boolean saveAsFile = (boolean) arguments.get("saveAsFile");
            if (saveAsFile) {
                File cacheDir = VisionCameraCropperModule.getContext().getCacheDir();
                String fileName = System.currentTimeMillis() + ".jpg";
                String path = BitmapUtils.saveImage(bm, cacheDir, fileName);
                result.put("path", path);
                Log.d("CropperFrameProcessorPlugin", "Bitmap saved to file: " + path);
            }
        }
    } catch (FrameInvalidError e) {
        Log.e("CropperFrameProcessorPlugin", "Frame invalid error: " + e.getMessage());
        throw new RuntimeException(e);
    }

    return result;
  }

  static public Bitmap getBitmap() {
    try {
      return frameTaken;
    } catch (Exception e) {
      return null;
    }
  }
}
