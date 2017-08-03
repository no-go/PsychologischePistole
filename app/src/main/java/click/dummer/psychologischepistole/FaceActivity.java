package click.dummer.psychologischepistole;

import android.Manifest;
import android.app.Activity;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FaceActivity extends Activity implements SensorEventListener {
    public static String PACKAGE_NAME;
    public static String LINKURL = "http://bomelino.de/?what=pistoleApp";

    SensorManager sensorManager;
    Sensor sensor;
    boolean fulls = false;

    Bitmap normal;
    Bitmap peng;

    MediaPlayer mp;

    ImageView imageView;
    Button btn;
    int[] forces = {0, 25, 40};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = this.getPackageName();
        setContentView(R.layout.face);
        normal = BitmapFactory.decodeResource(getResources(), R.drawable.normal);
        peng = BitmapFactory.decodeResource(getResources(), R.drawable.pengimg);

        mp = new MediaPlayer();
        mp = MediaPlayer.create(getApplicationContext(), R.raw.peng);
        if(mp.isPlaying()) mp.pause();

        btn = (Button) findViewById(R.id.button);
        imageView = (ImageView) findViewById(R.id.imageView);
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        imageView.setBackgroundColor(Color.WHITE);
        btn.setBackgroundColor(Color.WHITE);
        btn.setTextColor(Color.BLACK);

        toFullscreen();
    }

    public void pengNow(View v) {
        imageView.setImageBitmap(peng);
        if (!mp.isPlaying()) mp.start();
    }

    public void website(View v) {
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(LINKURL));
        startActivity(i);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        toFullscreen();
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
        fulls = false;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Float v = Math.abs(sensorEvent.values[0]) + Math.abs(sensorEvent.values[1]) + Math.abs(sensorEvent.values[2]);
        int val = Math.round(v);
        if (val < forces[1] && !mp.isPlaying()) {
            imageView.setImageBitmap(normal);
        } else if (val < forces[2]) {
            pengNow(new View(this));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void toFullscreen() {
        if (!fulls) {
            int uiOptions = getWindow().getDecorView().getSystemUiVisibility();
            int newUiOptions = uiOptions;

            if (Build.VERSION.SDK_INT >= 14) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            }

            if (Build.VERSION.SDK_INT >= 16) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_FULLSCREEN;
            }

            if (Build.VERSION.SDK_INT >= 18) {
                newUiOptions ^= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }

            getWindow().getDecorView().setSystemUiVisibility(newUiOptions);
            fulls = true;
        }
    }
}
