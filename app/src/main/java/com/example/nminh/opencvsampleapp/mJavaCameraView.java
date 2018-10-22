package com.example.nminh.opencvsampleapp;

import org.opencv.android.JavaCameraView;

import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class mJavaCameraView extends JavaCameraView implements Camera.PictureCallback {

    private String mPictureFileName;
    private Bitmap imageBitmap;
    public mJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }



    public void takePicture(Bitmap bitmap, String fileName) {
        this.mPictureFileName = fileName;
        this.imageBitmap = bitmap;
        mCamera.setPreviewCallback(null);

        mCamera.takePicture(null, null, this);
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera camera) {
        mCamera.startPreview();
        mCamera.setPreviewCallback(this);

        savePicture(getContext(), imageBitmap, "OpenCV_" + mPictureFileName + ".jpeg");
    }

    public static void savePicture(Context context, Bitmap bitmap, String imageName){
        FileOutputStream fos = null;
        File picPath = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera", imageName);

        try {
            fos = new FileOutputStream(picPath);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(context, "Image saved to /DCIM/Camera", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(context, "Can't take picture. Error: IOException", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
