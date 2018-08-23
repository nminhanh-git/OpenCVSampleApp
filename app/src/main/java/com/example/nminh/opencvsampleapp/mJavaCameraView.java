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

    public mJavaCameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    private String mPictureFileName;
    private Bitmap imageBitmap;

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

        File picPath = new File(Environment.getExternalStorageDirectory().getPath() + "/DCIM/Camera", "OpenCV_" + mPictureFileName + ".jpeg");
        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(picPath);
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Toast.makeText(getContext(), "Taken", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(getContext(), "Can't take picture. Error: IOException", Toast.LENGTH_SHORT).show();
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
