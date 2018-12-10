package com.example.liulo.mycamera;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
    private Camera camera;
    private boolean preview = false;
    private ImageView change, takephoto,flash;
    private int cameraPosition = 0;
    private SurfaceHolder holder;
    private ImageView imageView;
    private LinearLayout ll_view;
    int picWidth = 1920;
    int picHeight = 1080;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 拍照过程屏幕一直处于高亮

        setContentView(R.layout.activity_main);
        initView();
        ll_view.getBackground().setAlpha(80);
        SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //holder.setFixedSize(1080, 1920);
        holder.addCallback(new SurfaceViewCallback());

        takephoto.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                camera.autoFocus(new AutoFocusCallback() {// 自动对焦
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            camera.takePicture(null, null, jpeg);// 将拍摄到的照片给自定义的对象
                        }
                    }
                });
            }
        });

        change.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                int cameraCount = 0;
                CameraInfo cameraInfo = new CameraInfo();
                cameraCount = Camera.getNumberOfCameras();// 得到摄像头的个数

                for (int i = 0; i < cameraCount; i++) {
                    Camera.getCameraInfo(i, cameraInfo);// 得到每一个摄像头的信息
                    if (cameraPosition == 0) {
                        // 现在是后置，变更为前置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置													// CAMERA_FACING_BACK后置
                            camera.stopPreview();// 停掉原来摄像头的预览
                            camera.release();// 释放资源
                            camera = null;// 取消原来摄像头
                            camera = Camera.open(i);// 打开当前选中的摄像头
                            try {
                                camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            getWH(camera);
                            setCameraDisplayOrientation(MainActivity.this,cameraPosition, camera,270);
                            camera.startPreview();// 开始预览
                            cameraPosition = 1;
                            break;
                        }
                    } else {
                        // 现在是前置， 变更为后置
                        if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置													// CAMERA_FACING_BACK后置
                            camera.stopPreview();// 停掉原来摄像头的预览
                            camera.release();// 释放资源
                            camera = null;// 取消原来摄像头
                            camera = Camera.open(i);// 打开当前选中的摄像头
                            try {
                                camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            getWH(camera);
                            setCameraDisplayOrientation(MainActivity.this,cameraPosition, camera,0);
                            camera.startPreview();// 开始预览
                            cameraPosition = 0;
                            break;
                        }
                    }

                }
            }
        });

        flash.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                setFlashMode();
            }
        });

    }

    private void initView() {
        imageView = (ImageView) findViewById(R.id.image);
        takephoto = (ImageView) findViewById(R.id.takephoto);
        change = (ImageView) findViewById(R.id.change);
        flash= (ImageView) findViewById(R.id.flash);
        ll_view=(LinearLayout) findViewById(R.id.ll_view);
    }

    private final class SurfaceViewCallback implements android.view.SurfaceHolder.Callback {
        /**
         * surfaceView 被创建成功后调用此方法
         */
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            camera = Camera.open(cameraPosition);

            try {
                camera.setPreviewDisplay(holder);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            getWH(camera);
            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera,0);
            camera.startPreview();
            preview = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        /**
         * SurfaceView 被销毁时释放掉 摄像头
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                /* 若摄像头正在工作，先停止它 */
                if (preview) {
                    camera.stopPreview();
                    preview = false;
                }
                camera.release();
            }
        }

    }

    // 创建jpeg图片回调数据对象
    PictureCallback jpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
                saveBitmap("/mnt/sdcard/Ad/1.jpg", bitmap);
                imageView.setImageBitmap(bitmap);
                camera.stopPreview();// 关闭预览 处理数据
                camera.startPreview();// 数据处理完后继续开始预览
                // bitmap.recycle();//回收bitmap空间
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

    /**
     * 保存bitmap到本地
     *
     * @param
     * @param mBitmap
     * @return
     */
    public static String saveBitmap(String cachePath, Bitmap mBitmap) {
        File filePic = null;
        try {
            filePic = new File(cachePath);
            filePic.createNewFile();
            FileOutputStream fos = new FileOutputStream(filePic);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            if (null != filePic)
                filePic.delete();

            e.printStackTrace();
            return "";
        }
        return filePic.getAbsolutePath();
    }

    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera,int setRotation) {

        CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else {
            result = (info.orientation - degrees + 360) % 360;
        }
        System.out.println("rotation:" + rotation + " result:" + result+ " picWidth:"+picWidth+" picHeight:"+picHeight+" setRotation:"+setRotation);
        Camera.Parameters parameters = camera.getParameters();
        if(setRotation!=0)
            parameters.setRotation(setRotation);
        else
            parameters.setRotation(result);


        parameters.setPictureSize(picWidth, picHeight);
        /* 每秒从摄像头捕获5帧画面， */
        //parameters.setPreviewFrameRate(5);
        /* 设置照片的输出格式:jpg */
        parameters.setPictureFormat(PixelFormat.JPEG);
        camera.setParameters(parameters);

        camera.setDisplayOrientation(result);

    }

    /*
     * 设置闪光灯
     */
    private void setFlashMode(){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return;
        if(null==camera)
            camera= Camera.open();
        Camera.Parameters parameter = camera.getParameters();
        if(camera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)){
            parameter.setFlashMode(Parameters.FLASH_MODE_TORCH);
        }else{
            parameter.setFlashMode(Parameters.FLASH_MODE_OFF);

        }
        camera.setParameters(parameter);

    }
    private void getWH(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
        boolean isExt=false;
        if (sizeList.size() > 1) {
            Iterator<Camera.Size> itor = sizeList.iterator();
            while (itor.hasNext()) {
                Camera.Size cur = itor.next();
                if (cur.width <= 1920 && cur.height <= 1280) {
                    picWidth = cur.width;
                    picHeight = cur.height;
                    isExt=true;
                    System.out.println("break PreviewWidth(0):" + picWidth + " PreviewHeight:" + picHeight);
                    break;
                }else{
                    System.out.println("cur.width;:" + cur.width + " cur.height:" + cur.height);
                }
            }

        }
        if (!isExt) {
            picWidth = sizeList.get(0).width;
            picHeight = sizeList.get(0).height;
            System.out.println("PreviewWidth(1):" + picWidth + " PreviewHeight:" + picHeight);
        }

    }
}
