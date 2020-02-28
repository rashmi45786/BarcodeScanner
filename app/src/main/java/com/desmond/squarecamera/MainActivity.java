package com.desmond.squarecamera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.Display;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_CAMERA = 0;
    private static final int REQUEST_PERMISSION_CODE = 1;
    private Point mSize;
    private Bitmap myBitmap;
    private TextView txtView;
    private TextView btnResult;
    ImageView imageView;
    Button btnOpenCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnOpenCamera = findViewById(R.id.btnLaunchCamera);
        txtView = (TextView) findViewById(R.id.txtContent);
        btnResult = (TextView) findViewById(R.id.txtDesc);

        imageView = (ImageView) findViewById(R.id.image);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = Math.round(getDeviceWidth());
        params.width = Math.round(getDeviceWidth());
        imageView.setLayoutParams(params);
        Display display = getWindowManager().getDefaultDisplay();
        mSize = new Point();
        display.getSize(mSize);


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) return;

        if (requestCode == REQUEST_CAMERA) {
            Uri photoUri = data.getData();
            // Get the bitmap in according to the width of the device
            myBitmap = ImageUtility.decodeSampledBitmapFromPath(photoUri.getPath(), mSize.x, mSize.x);
            ((ImageView) findViewById(R.id.image)).setImageBitmap(myBitmap);

            ScanImage();
        }

    }

    public void requestForCameraPermission(View view) {
        final String permission = Manifest.permission.CAMERA;
        if (ContextCompat.checkSelfPermission(MainActivity.this, permission)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, permission)) {
                showPermissionRationaleDialog("You need to allow permissions to scan.", permission);
            } else {
                requestPermissions();
            }
        } else {
            launch();
        }
    }

    private void showPermissionRationaleDialog(final String message, final String permission) {
        new AlertDialog.Builder(MainActivity.this)
                .setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.this.requestPermissions();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create()
                .show();
    }

    private void requestPermissions() {

        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                REQUEST_PERMISSION_CODE);

    }


    private void launch() {
        Intent startCustomCameraIntent = new Intent(this, CameraActivity.class);
        startActivityForResult(startCustomCameraIntent, REQUEST_CAMERA);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_CODE:
                final int numOfRequest = grantResults.length;
                final boolean isGranted = numOfRequest == 1
                        && PackageManager.PERMISSION_GRANTED == grantResults[numOfRequest - 1];
                if (isGranted) {
                    launch();
                }
                else if (grantResults[0]==PackageManager.PERMISSION_DENIED){
                    finish();
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    void ScanImage() {
        BarcodeDetector detector =
                new BarcodeDetector.Builder(getApplicationContext())
                        .setBarcodeFormats(Barcode.DRIVER_LICENSE | Barcode.PDF417)
                        .build();
        if (!detector.isOperational()) {
            txtView.setText("Could not set up the detector!");
            return;
        }

        Frame frame = new Frame.Builder().setBitmap(myBitmap).build();
        SparseArray<Barcode> barcodes = detector.detect(frame);
        if (barcodes.size() > 0) {
            Barcode thisCode = barcodes.valueAt(0);
            btnResult.setVisibility(View.VISIBLE);
            txtView.setText(thisCode.rawValue);
            txtView.setVisibility(View.VISIBLE);
            imageView.setVisibility(View.VISIBLE);
            btnOpenCamera.setVisibility(View.VISIBLE);
            btnOpenCamera.setText("Recapture Photo");

        } else {
            Toast.makeText(getApplicationContext(), "Unable to parse the driver license. Please try again", Toast.LENGTH_LONG).show();
            btnResult.setVisibility(View.GONE);
            btnOpenCamera.setVisibility(View.VISIBLE);
            txtView.setVisibility(View.GONE);

        }

    }

    float getDeviceWidth() {
//        DisplayMetrics dm = getResources().getDisplayMetrics();
//        return dm.densityDpi;

        WindowManager wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }

}
