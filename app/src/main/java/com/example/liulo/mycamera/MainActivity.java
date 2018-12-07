package com.example.liulo.mycamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private SurfaceView sfv_preview;
    private Button btn_take;
    private Camera camera = null;
    private SurfaceHolder.Callback cpHolderCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            startPreview();
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            if (camera != null)
            {
                camera.stopPreview();
                if (holder != null) {
                    startPreview();
                    }
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            stopPreview();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bindViews();
    }

    private void bindViews() {
        sfv_preview = (SurfaceView) findViewById(R.id.sfv_preview);
        btn_take = (Button) findViewById(R.id.btn_take);
        sfv_preview.getHolder().addCallback(cpHolderCallback);

        btn_take.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setPictureFormat(ImageFormat.JPEG);
                parameters.setPictureSize(3000,4000);
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
                camera.autoFocus(new Camera.AutoFocusCallback(){
                    @Override
                    public void onAutoFocus(boolean sucess, Camera camera){
                        if(sucess){
                            camera.takePicture(null, null, new Camera.PictureCallback() {
                                @Override
                                public void onPictureTaken(byte[] data, Camera camera) {
                                    String path = "";
                                    if ((path = saveFile(data)) != null) {
                                        Intent it = new Intent(MainActivity.this, PreviewActivity.class);
                                        it.putExtra("path", path);
                                        startActivity(it);
                                    } else {
                                        Toast.makeText(MainActivity.this, "保存照片失败", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        }
                    }
                });

            }
        });
    }

    //保存临时文件的方法
    private String saveFile(byte[] bytes){
        try {
            File file = File.createTempFile("img","");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(bytes);
            fos.flush();
            fos.close();
            return file.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
       /* File dir = new File(Environment.getExternalStorageDirectory(),"pictures");
        if(dir.exists()){
            dir.mkdirs();
        }
        File currentImageFile = new File(dir,System.currentTimeMillis() + ".jpg");
        if(!currentImageFile.exists()){
            try {
                currentImageFile.createNewFile();
                FileOutputStream fos = new FileOutputStream(currentImageFile);
                fos.write(bytes);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(currentImageFile));
        startActivityForResult(it, Activity.DEFAULT_KEYS_DIALER);
        return "";*/
    }
    //onActivityResult：


    //开始预览
    private void startPreview(){
        camera = Camera.open();
        try {
            camera.setPreviewDisplay(sfv_preview.getHolder());
            camera.setDisplayOrientation(90);   //让相机旋转90度
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //停止预览
    private void stopPreview() {
        camera.setPreviewCallback(null);
        camera.stopPreview();
        camera.release();
        camera = null;
    }

}