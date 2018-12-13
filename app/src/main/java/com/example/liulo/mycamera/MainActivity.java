package com.example.liulo.mycamera;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.media.MediaCodec;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.EventLogTags;
import android.util.Log;
import android.util.Size;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.Policy;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("NewApi")
public class MainActivity extends Activity {
    private Camera camera;
    private boolean mPreview = false;
    private ImageView change, takephoto, flash;
    private int cameraPosition = 0;
    private SurfaceHolder holder;
    private ImageView imageView;
    private LinearLayout ll_view;
    int picWidth = 4000;
    int picHeight = 2250;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        checkPermission();
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);// 拍照过程屏幕一直处于高亮

        setContentView(R.layout.activity_main);
        initView();
        ll_view.getBackground().setAlpha(80);
        SurfaceView surfaceView = findViewById(R.id.surfaceView);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //holder.setFixedSize(1920, 1080);
        holder.addCallback(new SurfaceViewCallback());

        imageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
                // 传递路径
                intent.putExtra("path", currentImageFile.getAbsolutePath());
                startActivity(intent);
            }
        });

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
                                DisplayMetrics dm = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(dm);
                                int screenWidth = dm.widthPixels;
                                int screenHeight = dm.heightPixels;
                                Camera.Parameters parameters = camera.getParameters();
                                Camera.Size preSize = getCloselyPreSize(true, screenWidth , screenHeight, parameters.getSupportedPreviewSizes());
                                parameters.setPreviewSize(preSize.width, preSize.height);
                                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                camera.setParameters(parameters);
                                camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            getWH(camera);
                            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera, 270);
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
                                DisplayMetrics dm = new DisplayMetrics();
                                getWindowManager().getDefaultDisplay().getMetrics(dm);
                                int screenWidth = dm.widthPixels;
                                int screenHeight = dm.heightPixels;
                                Camera.Parameters parameters = camera.getParameters();
                                Camera.Size preSize = getCloselyPreSize(true, screenWidth , screenHeight, parameters.getSupportedPreviewSizes());
                                parameters.setPreviewSize(preSize.width, preSize.height);
                                parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
                                camera.setParameters(parameters);
                                camera.setPreviewDisplay(holder);// 通过surfaceview显示取景画面
                            } catch (IOException e) {
                                // TODO Auto-generated catch block
                                e.printStackTrace();
                            }
                            getWH(camera);
                            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera, 0);
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



    //添加手指觸摸屏幕對焦功能
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getPointerCount() == 1) {
            handleFocus(event, camera);
        }
        return true;
    }

    private void initView() {
        imageView = findViewById(R.id.image);
        takephoto = findViewById(R.id.takephoto);
        change = findViewById(R.id.change);
        flash = findViewById(R.id.flash);
        ll_view = findViewById(R.id.ll_view);
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
            setCameraDisplayOrientation(MainActivity.this, cameraPosition, camera, 0);
            camera.startPreview();
            mPreview = true;
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            int screenWidth = dm.widthPixels;
            int screenHeight = dm.heightPixels;
            Camera.Parameters parameters = camera.getParameters();
            Camera.Size preSize = getCloselyPreSize(true, screenWidth , screenHeight, parameters.getSupportedPreviewSizes());
            parameters.setPreviewSize(preSize.width, preSize.height);
            parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            camera.setParameters(parameters);
        }

        /**
         * SurfaceView 被销毁时释放掉 摄像头
         */
        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            if (camera != null) {
                /* 若摄像头正在工作，先停止它 */
                if (mPreview) {
                    camera.stopPreview();
                    mPreview = false;
                }
                camera.release();
            }
        }

    }

    // 创建jpeg图片回调数据对象
    private File currentImageFile = null;
    PictureCallback jpeg = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            // TODO Auto-generated method stub
            String path = "/mnt/sdcard/";
            try {
                currentImageFile = new File(path, System.currentTimeMillis() + ".jpg");
                FileOutputStream fos = new FileOutputStream(currentImageFile);
                if (!currentImageFile.exists()) {
                    try {
                        currentImageFile.createNewFile();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                fos.write(data);
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
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
            filePic.delete();

            e.printStackTrace();
            return "";
        }
        return filePic.getAbsolutePath();
    }

    public void setCameraDisplayOrientation(Activity activity, int cameraId, Camera camera, int setRotation) {

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
        System.out.println("rotation:" + rotation + " result:" + result + " picWidth:" + picWidth + " picHeight:" + picHeight + " setRotation:" + setRotation);
        Camera.Parameters parameters = camera.getParameters();
        if (setRotation != 0)
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
    private void setFlashMode() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) return;
        if (null == camera)
            camera = Camera.open();
        Camera.Parameters parameter = camera.getParameters();
        if (camera.getParameters().getFlashMode().equals(Parameters.FLASH_MODE_OFF)) {
            parameter.setFlashMode(Parameters.FLASH_MODE_ON);
        } else {
            parameter.setFlashMode(Parameters.FLASH_MODE_OFF);

        }
        camera.setParameters(parameter);

    }

    private void getWH(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        List<Camera.Size> sizeList = parameters.getSupportedPictureSizes();
        boolean isExt = false;
        if (sizeList.size() > 1) {
            for (Camera.Size cur : sizeList) {
                if (cur.width <= 4000 && cur.height <= 2250) {
                    picWidth = cur.width;
                    picHeight = cur.height;
                    isExt = true;
                    System.out.println("break PreviewWidth(0):" + picWidth + " PreviewHeight:" + picHeight);
                    break;
                } else {
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

    private void checkPermission() {
        requestPermissions(new String[]{Manifest.permission.CAMERA}, 2);
        requestPermissions(new String[]{Manifest.permission_group.STORAGE}, 3);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                Log.e("PermissionsResult", "露皮，权限你倒是点允许啊！！！");
            } else {
                Log.e("PermisssionsResult", "开始自拍吧！");
            }

        }
    }

    public static Camera.Size getCloselyPreSize(boolean isPortrait, int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        int reqTmpWidth;
        int reqTmpHeight;
        // 当屏幕为垂直的时候需要把宽高值进行调换，保证宽大于高
        if (isPortrait) {
            reqTmpWidth = surfaceHeight;
            reqTmpHeight = surfaceWidth;
        } else {
            reqTmpWidth = surfaceWidth;
            reqTmpHeight = surfaceHeight;
        }
        //先查找preview中是否存在与surfaceview相同宽高的尺寸
        for (Camera.Size size : preSizeList) {
            if ((size.width == reqTmpWidth) && (size.height == reqTmpHeight)) {
                return size;
            }
        }

        // 得到与传入的宽高比最接近的size
        float reqRatio = ((float) reqTmpWidth) / reqTmpHeight;
        float curRatio, deltaRatio;
        float deltaRatioMin = Float.MAX_VALUE;
        Camera.Size retSize = null;
        for (Camera.Size size : preSizeList) {
            curRatio = ((float) size.width) / size.height;
            deltaRatio = Math.abs(reqRatio - curRatio);
            if (deltaRatio < deltaRatioMin) {
                deltaRatioMin = deltaRatio;
                retSize = size;
            }
        }

        return retSize;
    }
    private Rect calculateTapArea(float x, float y, float coefficient) {
        float focusAreaSize = 300;
        int areaSize = Float.valueOf(focusAreaSize * coefficient).intValue();
        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);
        int screenWidth = dm.widthPixels;
        int screenHeight = dm.heightPixels;
        int centerY = (int) (x / screenWidth* 2000 - 1000);
        int centerX = (int) (y / screenHeight * 2000 - 1000);
        int left = clamp(centerX - areaSize / 2, -1000, 1000);
        int top = clamp(centerY - areaSize / 2, -1000, 1000);
        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }


    private static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private  void handleFocus(MotionEvent event, Camera camera) {

        Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);

        camera.cancelAutoFocus();
        Camera.Parameters params = camera.getParameters();
        if (params.getMaxNumFocusAreas() > 0) {
            List<Camera.Area> focusAreas = new ArrayList<>();
            focusAreas.add(new Camera.Area(focusRect, 800));
            params.setFocusAreas(focusAreas);
            ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
            margin.setMargins((int)(event.getX()-46.5), (int)(event.getY()-46.5), 0, 0);
            FrameLayout.LayoutParams params_focus = new FrameLayout.LayoutParams(margin);  ;
            final ImageView focus = findViewById(R.id.focus);
            focus.setLayoutParams(params_focus);
            System.out.println(focus.getLayoutParams().getClass());
            focus.setVisibility(ImageView.VISIBLE);
            /*
             * 2秒后，自动对焦框消失
             * */
            Handler timeHandler = new Handler();
            timeHandler.post(new Runnable() {

                @Override
                public void run() {
                    try {
                        Thread.sleep(2000);
                        if (View.VISIBLE == focus.getVisibility()) {
                            focus.setVisibility(ImageView.GONE);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            });
        } else {
            Log.i("tag", "focus areas not supported");
        }

        final String currentFocusMode = params.getFocusMode();
        params.setFocusMode(Camera.Parameters.FOCUS_MODE_MACRO);
        camera.setParameters(params);

        camera.autoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {
                Camera.Parameters params = camera.getParameters();
                params.setFocusMode(currentFocusMode);
                camera.setParameters(params);
            }
        });
    }
}









