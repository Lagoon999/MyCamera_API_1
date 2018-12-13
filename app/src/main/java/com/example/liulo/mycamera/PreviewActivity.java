package com.example.liulo.mycamera;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;

public class PreviewActivity extends Activity {
    private TextView tv_path;
    private ImageView iv_photo;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_preview);
        String path = getIntent().getStringExtra("path");
        int rotation = 0 ;
        getIntent().getIntExtra("rotation",rotation);
        tv_path = (TextView) findViewById(R.id.tv_path);
        tv_path.setText(path);
        iv_photo = (ImageView) findViewById(R.id.iv_photo);
        if(path != null){
            iv_photo.setImageURI(Uri.fromFile(new File(path)));
        }
        iv_photo.setRotation(rotation);
        iv_photo.setScaleType(ImageView.ScaleType. FIT_CENTER);
    }
}
