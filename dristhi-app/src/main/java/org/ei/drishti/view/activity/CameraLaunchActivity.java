package org.ei.drishti.view.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;
import org.ei.drishti.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import static android.os.Environment.DIRECTORY_PICTURES;
import static android.os.Environment.getExternalStoragePublicDirectory;

public class CameraLaunchActivity extends Activity {
    private static final int TAKE_PHOTO_REQUEST_CODE = 111;
    private static final String JPG_FILE_SUFFIX = ".jpg";
    private static final String DRISTHI_DIRECTORY_NAME = "Dristhi";
    private File imageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startCamera();
    }

    public void startCamera() {
        Intent takePhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            imageFile = createImageFile();
        } catch (IOException e) {
            Log.logError("Could not create temp file for storing image. Not taking photo.");
            return;
        }
        takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(takePhotoIntent, TAKE_PHOTO_REQUEST_CODE);

    }

    private File createImageFile() throws IOException {
        String imageFileName = UUID.randomUUID().toString();
        File directory = new File(getExternalStoragePublicDirectory(DIRECTORY_PICTURES), DRISTHI_DIRECTORY_NAME);
        return File.createTempFile(imageFileName, JPG_FILE_SUFFIX, directory);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode != TAKE_PHOTO_REQUEST_CODE)
            return;
        if (imageFile.exists()) {
            setPic(imageFile.getAbsolutePath());
            Toast.makeText(this, imageFile.getAbsolutePath(), Toast.LENGTH_SHORT).show();
        }
        super.onBackPressed();
    }

    private void setPic(String mCurrentPhotoPath) {
        int targetWidth = 100;
        int targetHeight = 100;

        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapOptions);
        int originalWidth = bitmapOptions.outWidth;
        int originalHeight = bitmapOptions.outHeight;

        int scaleFactor = Math.min(originalWidth / targetWidth, originalHeight / targetHeight);
        bitmapOptions.inJustDecodeBounds = false;
        bitmapOptions.inSampleSize = scaleFactor;
        bitmapOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bitmapOptions);
        saveBitmap(bitmap, mCurrentPhotoPath);
    }

    private void saveBitmap(Bitmap bitmap, String location) {
        try {
            FileOutputStream out = new FileOutputStream(location);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            Log.logError("Could not save resized image.");
        }
    }
}
