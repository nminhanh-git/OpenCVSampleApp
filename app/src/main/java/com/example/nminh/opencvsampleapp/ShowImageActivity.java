package com.example.nminh.opencvsampleapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;

public class ShowImageActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageView mImgViewEdited;
    private Bitmap mBitmapOriginal;
    private Bitmap mBitmapCompressed;
    private Bitmap mBitmapEdited;
    private Boolean shouldShowSave = false;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);

        mImgViewEdited = findViewById(R.id.edited_image_view);
        Intent i = getIntent();
        imagePath = i.getStringExtra("image path");
        showImage(imagePath);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = new MenuInflater(this);
        inflater.inflate(R.menu.show_image_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opt_cancel:
                finish();
                break;
            case R.id.opt_save:
                String imageName = "OpenCV_" + MainActivity.getCurrentDate() + ".jpeg";
                mJavaCameraView.savePicture(this, mBitmapEdited, imageName);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void showImage(String imagePath) {
        try {
            mBitmapOriginal = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(imagePath)));
            ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();

            mBitmapOriginal.compress(Bitmap.CompressFormat.JPEG, 80, outPutStream);
            byte[] byteArray = outPutStream.toByteArray();
            mBitmapCompressed = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }

        mBitmapEdited = mBitmapCompressed.copy(mBitmapCompressed.getConfig(), true);
        mImgViewEdited.setImageBitmap(mBitmapEdited);
    }
}
