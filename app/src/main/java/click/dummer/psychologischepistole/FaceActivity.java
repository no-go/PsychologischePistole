package click.dummer.psychologischepistole;

import android.app.Activity;
import android.app.Service;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class FaceActivity extends Activity implements SensorEventListener {
    public static String PACKAGE_NAME;

    SensorManager sensorManager;
    Sensor sensor;

    MediaPlayer mp;

    ImageView imageView;
    ArrayList<Bitmap> bitmaps;
    String[] bitmapNames = {"pengimg.jpg", "normal.jpg"};
    String mp3Name = "peng.mp3";
    int[] forces = {0, 40, 60};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PACKAGE_NAME = this.getPackageName();
        setContentView(R.layout.face);
        imageView = (ImageView) findViewById(R.id.imageView);
        sensorManager = (SensorManager) getSystemService(Service.SENSOR_SERVICE);
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        String path = makeDir();
        if(path != null) {
            storeDefaultBitmaps(path);
            storeMp3(path + mp3Name, R.raw.peng);
            loadBitmaps(path);
            setBackgrounds();
        }
        Uri uri = Uri.fromFile(new File(path + mp3Name));
        mp = new MediaPlayer();
        try {
            mp.setDataSource(this, uri);
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        toggleFullscreen();
    }

    private String makeDir() {
        File file = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
            file = new File(
                    Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                    PACKAGE_NAME
            );
        } else {
            file = new File(Environment.getExternalStorageDirectory() + "/Documents/"+PACKAGE_NAME);
        }

        String path = file.getPath() + "/";
        try {
            file.mkdirs();
            file = new File(path);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.fileErr) + ": " + file.getPath(), Toast.LENGTH_SHORT).show();
            return null;
        }
        return path;
    }

    private void storeDefaultBitmaps(String path) {
        storeMipmap(path + bitmapNames[0], R.mipmap.pengimg);
        storeMipmap(path + bitmapNames[1], R.mipmap.normal);
    }

    private void storeMipmap(String filename, int res) {
        Bitmap b = BitmapFactory.decodeResource(getResources(), res);
        FileOutputStream out = null;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                out = new FileOutputStream(filename);
                b.compress(Bitmap.CompressFormat.JPEG, 90, out);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void storeMp3(String filename, int res) {
        InputStream in_s = getResources().openRawResource(res);
        FileOutputStream out = null;
        try {
            File file = new File(filename);
            if (!file.exists()) {
                byte[] b = new byte[in_s.available()];
                in_s.read(b);
                out = new FileOutputStream(filename);
                out.write(b);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadBitmaps(String path) {
        bitmaps = new ArrayList<>();
        bitmaps.add(BitmapFactory.decodeFile(path + bitmapNames[0]));
        bitmaps.add(BitmapFactory.decodeFile(path + bitmapNames[1]));
    }

    private void setBackgrounds() {
        int pixel = bitmaps.get(0).getPixel(0,0);
        imageView.setBackgroundColor(pixel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Float v = Math.abs(sensorEvent.values[0]) + Math.abs(sensorEvent.values[1]) + Math.abs(sensorEvent.values[2]);
        int val = Math.round(v);
        if (val < forces[1] && !mp.isPlaying()) {
            imageView.setImageBitmap(bitmaps.get(1));
        } else if (val < forces[2]) {
            imageView.setImageBitmap(bitmaps.get(0));
            if (!mp.isPlaying()) {
                mp.start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}

    public void toggleFullscreen() {
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
    }
}
