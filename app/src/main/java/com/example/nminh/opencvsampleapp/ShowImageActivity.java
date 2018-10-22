package com.example.nminh.opencvsampleapp;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Date;

public class ShowImageActivity extends AppCompatActivity implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    public static final int ADJUST_BRIGHTNESS = 8;
    public static final int ADJUST_CONTRAST = 9;
    public static final int ADJUST_GAUSSIAN_BLUR = 10;
    public static final int IMAGE_ROTATION = 11;
    public static final int ADJUST_NEGATIVE = 12;


    private ImageView mImgViewEdited;
    private LinearLayout mLayoutBtn;
    private Button mBtnCancel;
    private Button mBtnSave;
    private LinearLayout mLayoutSeekbar;
    private LinearLayout mLayoutBlurSeekbar;
    private SeekBar mSeekbarBrightness;
    private SeekBar mSeekbarContrast;
    private SeekBar mSeekbarBlur;


    private Bitmap mBitmapOriginal;
    private Bitmap mBitmapEdited;

    private String imagePath;
    private Mat mMatIntermediate;
    private Mat mSepiaKernel;
    private Size mSize0;
    private Mat mMat0;
    private MatOfInt mChannels[];
    private MatOfInt mHistSize;
    private int mHistSizeNum = 25;
    private MatOfFloat mRanges;
    private Scalar mColorsRGB[];
    private Scalar mColorsHue[];
    private Scalar mWhite;
    private Point mP1;
    private Point mP2;
    private float mBuff[];
    private int alpha;
    private int beta;
    private int kSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        getSupportActionBar().setTitle("Edit Image");

        mImgViewEdited = findViewById(R.id.edited_image_view);
        Intent i = getIntent();
        imagePath = i.getStringExtra("image path");

        mLayoutBtn = findViewById(R.id.btn_layout);
        mBtnCancel = findViewById(R.id.cancel_btn);
        mBtnCancel.setOnClickListener(this);
        mBtnSave = findViewById(R.id.save_btn);
        mBtnSave.setOnClickListener(this);

        mLayoutSeekbar = findViewById(R.id.seekbar_layout);
        mSeekbarBrightness = findViewById(R.id.brightness_seekbar);
        mSeekbarBrightness.setOnSeekBarChangeListener(this);
        mSeekbarContrast = findViewById(R.id.contrast_seekbar);
        mSeekbarContrast.setOnSeekBarChangeListener(this);

        mLayoutBlurSeekbar = findViewById(R.id.blur_layout);
        mSeekbarBlur = findViewById(R.id.blur_seekbar);
        mSeekbarBlur.setOnSeekBarChangeListener(this);

        initVariable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showImage(imagePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            imagePath = imageUri.toString();

            Intent i = new Intent(this, ShowImageActivity.class);
            i.putExtra("image path", imagePath);
            startActivity(i);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.show_image_option_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.opt_another_image:
                Intent getImageIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(getImageIntent, 0);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_rgba:
                TurnOnFilter(MainActivity.VIEW_MODE_RGBA);
                mLayoutBtn.setVisibility(View.GONE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_histograms:
                TurnOnFilter(MainActivity.VIEW_MODE_HIST);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_canny:
                TurnOnFilter(MainActivity.VIEW_MODE_CANNY);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_sepia:
                TurnOnFilter(MainActivity.VIEW_MODE_SEPIA);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_zoom:
                TurnOnFilter(MainActivity.VIEW_MODE_ZOOM);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_sobel:
                TurnOnFilter(MainActivity.VIEW_MODE_SOBEL);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_pixelize:
                TurnOnFilter(MainActivity.VIEW_MODE_PIXELIZE);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                mLayoutBlurSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_posterize:
                TurnOnFilter(MainActivity.VIEW_MODE_POSTERIZE);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                mLayoutBlurSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_brightness:
                TurnOnFilter(MainActivity.VIEW_MODE_RGBA);
                mLayoutSeekbar.setVisibility(View.VISIBLE);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutBlurSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_blur:
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                mLayoutBlurSeekbar.setVisibility(View.VISIBLE);
                break;
            case R.id.opt_rotation:
                TurnOnFilter(IMAGE_ROTATION);
                mLayoutBtn.setVisibility(View.VISIBLE);
                mLayoutSeekbar.setVisibility(View.GONE);
                break;
            case R.id.opt_negative:
                TurnOnFilter(ADJUST_NEGATIVE);
                mLayoutBtn.setVisibility(View.VISIBLE);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void TurnOnFilter(int filterMode) {
        mBitmapEdited = mBitmapOriginal.copy(mBitmapOriginal.getConfig(), true);
        Mat inputMat = new Mat();
        inputMat = BitmapToMat(mBitmapEdited);
        Size inputMatSize = inputMat.size();
        Mat resultMat;
        switch (filterMode) {
            case MainActivity.VIEW_MODE_RGBA:
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;

            case MainActivity.VIEW_MODE_HIST:
                Mat hist = new Mat();
                int thickness = (int) (inputMatSize.width / (mHistSizeNum + 10) / 5);
                if (thickness > 5) thickness = 5;
                int offset = (int) ((inputMatSize.width - (5 * mHistSizeNum + 4 * 10) * thickness) / 2);
                // RGB
                for (int c = 0; c < 3; c++) {
                    Imgproc.calcHist(Arrays.asList(inputMat), mChannels[c], mMat0, hist, mHistSize, mRanges);
                    Imgproc.calcHist(Arrays.asList(inputMat), mChannels[c], mMat0, hist, mHistSize, mRanges);
                    Core.normalize(hist, hist, inputMatSize.height / 2, 0, Core.NORM_INF);
                    hist.get(0, 0, mBuff);
                    for (int h = 0; h < mHistSizeNum; h++) {
                        mP1.x = mP2.x = offset + (c * (mHistSizeNum + 10) + h) * thickness;
                        mP1.y = inputMatSize.height - 1;
                        mP2.y = mP1.y - 2 - (int) mBuff[h];
                        Imgproc.line(inputMat, mP1, mP2, mColorsRGB[c], thickness);
                    }
                }
                // Value and Hue
                Imgproc.cvtColor(inputMat, mMatIntermediate, Imgproc.COLOR_RGB2HSV_FULL);
                // Value
                Imgproc.calcHist(Arrays.asList(mMatIntermediate), mChannels[2], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, inputMatSize.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (3 * (mHistSizeNum + 10) + h) * thickness;
                    mP1.y = inputMatSize.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(inputMat, mP1, mP2, mWhite, thickness);
                }
                // Hue
                Imgproc.calcHist(Arrays.asList(mMatIntermediate), mChannels[0], mMat0, hist, mHistSize, mRanges);
                Core.normalize(hist, hist, inputMatSize.height / 2, 0, Core.NORM_INF);
                hist.get(0, 0, mBuff);
                for (int h = 0; h < mHistSizeNum; h++) {
                    mP1.x = mP2.x = offset + (4 * (mHistSizeNum + 10) + h) * thickness;
                    mP1.y = inputMatSize.height - 1;
                    mP2.y = mP1.y - 2 - (int) mBuff[h];
                    Imgproc.line(inputMat, mP1, mP2, mColorsHue[h], thickness);
                }
                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;


            case MainActivity.VIEW_MODE_CANNY:
                Imgproc.Canny(inputMat, mMatIntermediate, 80, 90);
                Imgproc.cvtColor(mMatIntermediate, inputMat, Imgproc.COLOR_GRAY2BGRA);
                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                inputMat.release();
                break;

            case MainActivity.VIEW_MODE_SEPIA:
                Core.transform(inputMat, inputMat, mSepiaKernel);
                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                inputMat.release();
                break;

            case MainActivity.VIEW_MODE_SOBEL:
                Imgproc.cvtColor(inputMat, inputMat, Imgproc.COLOR_BGRA2GRAY);
                Imgproc.Sobel(inputMat, mMatIntermediate, CvType.CV_8U, 1, 1);
                Core.convertScaleAbs(mMatIntermediate, mMatIntermediate, 10, 0);
                Imgproc.cvtColor(mMatIntermediate, inputMat, Imgproc.COLOR_GRAY2BGRA, 4);
                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                inputMat.release();
                break;

            case MainActivity.VIEW_MODE_PIXELIZE:
                Imgproc.resize(inputMat, mMatIntermediate, mSize0, 0.1, 0.1, Imgproc.INTER_NEAREST);
                Imgproc.resize(mMatIntermediate, inputMat, inputMat.size(), 0., 0., Imgproc.INTER_NEAREST);
                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                inputMat.release();
                break;

            case MainActivity.VIEW_MODE_POSTERIZE:
                Imgproc.Canny(inputMat, mMatIntermediate, 80, 90);
                inputMat.setTo(new Scalar(0, 0, 0, 255), mMatIntermediate);
                Core.convertScaleAbs(inputMat, mMatIntermediate, 1. / 16, 0);
                Core.convertScaleAbs(mMatIntermediate, inputMat, 16, 0);
                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;

            case ADJUST_BRIGHTNESS:
                resultMat = new Mat(inputMat.rows(), inputMat.cols(), inputMat.type());
                inputMat.convertTo(resultMat, -1, alpha, beta);
                mBitmapEdited = MatToBitmap(resultMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;

            case ADJUST_CONTRAST:
                resultMat = new Mat(inputMat.rows(), inputMat.cols(), inputMat.type());
                inputMat.convertTo(resultMat, -1, alpha, beta);
                mBitmapEdited = MatToBitmap(resultMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;

            case ADJUST_GAUSSIAN_BLUR:
                resultMat = new Mat(inputMat.rows(), inputMat.cols(), inputMat.type());
                Imgproc.GaussianBlur(inputMat, resultMat, new Size(kSize, kSize), 0);
                mBitmapEdited = MatToBitmap(resultMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;
            case IMAGE_ROTATION:
                resultMat = new Mat(inputMat.rows(), inputMat.cols(), inputMat.type());
                inputMat = BitmapToMat(((BitmapDrawable) mImgViewEdited.getDrawable()).getBitmap());
                Core.rotate(inputMat, resultMat, Core.ROTATE_90_COUNTERCLOCKWISE);
                mBitmapEdited = MatToBitmap(resultMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;
            case ADJUST_NEGATIVE:
                inputMat = BitmapToMat(((BitmapDrawable) mImgViewEdited.getDrawable()).getBitmap());
                Mat negativeMat =new Mat(inputMat.rows(), inputMat.cols(), inputMat.type(), new Scalar(255,255,255));
                Core.subtract(negativeMat,inputMat,inputMat);

                mBitmapEdited = MatToBitmap(inputMat);
                mImgViewEdited.setImageBitmap(mBitmapEdited);
                break;
        }
    }

    public void showImage(String imagePath) {
        try {
            mBitmapOriginal = BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(imagePath)));
            ByteArrayOutputStream outPutStream = new ByteArrayOutputStream();
            mBitmapOriginal.compress(Bitmap.CompressFormat.JPEG, 80, outPutStream);
            byte[] byteArray = outPutStream.toByteArray();
            mBitmapOriginal = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
        mImgViewEdited.setImageBitmap(mBitmapOriginal);
    }

    public Mat BitmapToMat(Bitmap bitmap) {
        Mat mat = new Mat();
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bitmap, mat);
        return mat;
    }

    public Bitmap MatToBitmap(Mat mat) {
        Bitmap bitmap = Bitmap.createBitmap(mat.cols(), mat.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mat, bitmap);
        Bitmap image = Bitmap.createBitmap(bitmap);
        return bitmap;
    }

    public void initVariable() {
        mMatIntermediate = new Mat();
        mSize0 = new Size();
        mChannels = new MatOfInt[]{new MatOfInt(0), new MatOfInt(1), new MatOfInt(2)};
        mBuff = new float[mHistSizeNum];
        mHistSize = new MatOfInt(mHistSizeNum);
        mRanges = new MatOfFloat(0f, 256f);
        mMat0 = new Mat();
        mColorsRGB = new Scalar[]{new Scalar(200, 0, 0, 255), new Scalar(0, 200, 0, 255), new Scalar(0, 0, 200, 255)};
        mColorsHue = new Scalar[]{
                new Scalar(255, 0, 0, 255), new Scalar(255, 60, 0, 255), new Scalar(255, 120, 0, 255), new Scalar(255, 180, 0, 255), new Scalar(255, 240, 0, 255),
                new Scalar(215, 213, 0, 255), new Scalar(150, 255, 0, 255), new Scalar(85, 255, 0, 255), new Scalar(20, 255, 0, 255), new Scalar(0, 255, 30, 255),
                new Scalar(0, 255, 85, 255), new Scalar(0, 255, 150, 255), new Scalar(0, 255, 215, 255), new Scalar(0, 234, 255, 255), new Scalar(0, 170, 255, 255),
                new Scalar(0, 120, 255, 255), new Scalar(0, 60, 255, 255), new Scalar(0, 0, 255, 255), new Scalar(64, 0, 255, 255), new Scalar(120, 0, 255, 255),
                new Scalar(180, 0, 255, 255), new Scalar(255, 0, 255, 255), new Scalar(255, 0, 215, 255), new Scalar(255, 0, 85, 255), new Scalar(255, 0, 0, 255)
        };
        mWhite = Scalar.all(255);
        mP1 = new Point();
        mP2 = new Point();

        // Fill sepia kernel
        mSepiaKernel = new Mat(4, 4, CvType.CV_32F);
        mSepiaKernel.put(0, 0, /* R */0.189f, 0.769f, 0.393f, 0f);
        mSepiaKernel.put(1, 0, /* G */0.168f, 0.686f, 0.349f, 0f);
        mSepiaKernel.put(2, 0, /* B */0.131f, 0.534f, 0.272f, 0f);
        mSepiaKernel.put(3, 0, /* A */0.000f, 0.000f, 0.000f, 1f);
        alpha = 1;
        beta = 0;
        kSize = 1;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.cancel_btn:
                TurnOnFilter(MainActivity.VIEW_MODE_RGBA);
                mLayoutBtn.setVisibility(View.GONE);
                mLayoutSeekbar.setVisibility(View.GONE);
                mLayoutBlurSeekbar.setVisibility(View.GONE);
                break;
            case R.id.save_btn:
                String today = new Date().toString();
                mJavaCameraView.savePicture(this, mBitmapEdited, "OpenCV_" + today + ".jpeg");
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        switch (seekBar.getId()) {
            case R.id.brightness_seekbar:
                beta = i - 100;
                TurnOnFilter(ADJUST_BRIGHTNESS);
                break;
            case R.id.contrast_seekbar:
                alpha = i;
                TurnOnFilter(ADJUST_CONTRAST);
                break;
            case R.id.blur_seekbar:
                if (i % 2 != 0) {
                    kSize = i;
                } else {
                    kSize = i + 1;
                }
                seekBar.setProgress(kSize);
                TurnOnFilter(ADJUST_GAUSSIAN_BLUR);
                break;
        }
        mLayoutBtn.setVisibility(View.VISIBLE);


    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
