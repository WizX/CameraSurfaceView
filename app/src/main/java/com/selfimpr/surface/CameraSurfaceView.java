package com.selfimpr.surface;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.os.Environment;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * description：   <br/>
 * ===============================<br/>
 * creator：Jiacheng<br/>
 * create time：2017/9/26 下午2:39<br/>
 * ===============================<br/>
 * reasons for modification：  <br/>
 * Modifier：  <br/>
 * Modify time：  <br/>
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback, Camera.AutoFocusCallback {
    private static final String TAG = "wjc";

    private static SurfaceHolder holder;
    private Camera mCamera;

    private int mScreenWidth;
    private int mScreenHeight;
    private Context context;

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getScreenMetrix(context);
        holder = getHolder();//后面会用到！
        holder.addCallback(this);
    }

    private void getScreenMetrix(Context context) {
        WindowManager WM = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        WM.getDefaultDisplay().getMetrics(outMetrics);
        mScreenWidth = outMetrics.widthPixels;
        mScreenHeight = outMetrics.heightPixels;
    }

    @Override
    public void surfaceCreated(SurfaceHolder arg0) {
        Log.e(TAG, "surfaceCreated...");
        if (mCamera == null) {
            mCamera = Camera.open();//开启相机，可以放参数 0 或 1，分别代表前置、后置摄像头，默认为 0
            try {
                mCamera.setPreviewDisplay(holder);//整个程序的核心，相机预览的内容放在 holder
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
        Log.i(TAG, "surfaceChanged...");
        // TODO: 2017/9/26 在预览前要设置摄像头的分辨率、预览分辨率和图片分辨率的宽高比保持一致。这样图片才不会变形
        setCameraParams(mCamera, mScreenWidth, mScreenHeight);
        //mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.startPreview();//该方法只有相机开启后才能调用
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder arg0) {
        Log.i(TAG, "surfaceChanged...");
        if (mCamera != null) {
            mCamera.release();//释放相机资源
            mCamera = null;
        }
    }

    public void takePicture() {
       /* mCamera.takePicture(new Camera.ShutterCallback() { // 拍照瞬间调用
            @Override
            public void onShutter() {
                Log.e(TAG, "onShutter");
            }
        }, new Camera.PictureCallback() { // 获得没有压缩过的图片数据
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e(TAG, "onPictureTaken:111--->" +(data==null?0: data.length));

            }
        },null*//*, new Camera.PictureCallback() { //创建jpeg图片回调数据对象
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {//4264172
                Log.e(TAG, "onPictureTaken:222--->" +(data==null?0: data.length));
                camera.stopPreview();
                camera.startPreview();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        producePicture(data);
                    }
                }).start();
            }
        }*//**//*, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                Log.e(TAG, "onPictureTaken:333--->" +(data==null?0: data.length));//4510288
            }
        }*//*);*/
        mCamera.setOneShotPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] data, Camera camera) {//3110400
                Log.e("wjc","onPreviewFrame:"+data.length);
            }
        });
    }

    private void producePicture(byte[] data) {
        BufferedOutputStream bos = null;
        Bitmap bm = null;
        try {
            // 获得图片
            bm = BitmapFactory.decodeByteArray(data, 0, data.length);
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                Log.i(TAG, "Environment.getExternalStorageDirectory()=" + Environment.getExternalStorageDirectory());
                String filePath = "/sdcard/dyk" + System.currentTimeMillis() + ".jpeg";//照片保存路径
                File file = new File(filePath);
                Log.e(TAG, "filePath:" + filePath);
                if (!file.exists()) {
                    file.createNewFile();
                }
                bos = new BufferedOutputStream(new FileOutputStream(file));
                bm.compress(Bitmap.CompressFormat.JPEG, 100, bos);//将图片压缩到流中
            } else {
                Toast.makeText(context, "没有检测到内存卡", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bos != null) {
                    bos.flush();//输出
                    bos.close();//关闭
                }
                if (bm != null) {
                    bm.recycle();// 回收bitmap空间
                }
//                mCamera.stopPreview();// 关闭预览
//                mCamera.startPreview();// 开启预览
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void setCameraParams(Camera camera, int width, int height) {
        Log.e(TAG, "setCameraParams  width=" + width + "  height=" + height);
        Camera.Parameters parameters = mCamera.getParameters();
        // 获取摄像头支持的PictureSize列表, 获取受支持的图片大小。
        List<Camera.Size> pictureSizeList = parameters.getSupportedPictureSizes();
        for (Camera.Size size : pictureSizeList) {
            Log.e(TAG, "pictureSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        /**从列表中选取合适的分辨率*/
        Camera.Size picSize = getProperSize(pictureSizeList, ((float) height / width));
        if (null == picSize) {
            Log.e(TAG, "null == picSize");
            picSize = parameters.getPictureSize(); //返回设置图片尺寸。
        }
        Log.e(TAG, "picSize.width=" + picSize.width + "  picSize.height=" + picSize.height);
        // 根据选出的PictureSize重新设置SurfaceView大小
        float w = picSize.width;
        float h = picSize.height;
        parameters.setPictureSize(picSize.width, picSize.height); //设置图片的尺寸。
        this.setLayoutParams(new FrameLayout.LayoutParams((int) (height * (h / w)), height));

        // 获取摄像头支持的PreviewSize列表,(获取受支持的预览图片)
        List<Camera.Size> previewSizeList = parameters.getSupportedPreviewSizes();
        for (Camera.Size size : previewSizeList) {
            Log.e(TAG, "previewSizeList size.width=" + size.width + "  size.height=" + size.height);
        }
        Camera.Size preSize = getProperSize(previewSizeList, ((float) height) / width);
        if (null != preSize) {
            Log.e(TAG, "preSize.width=" + preSize.width + "  preSize.height=" + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        parameters.setJpegQuality(100); // 设置照片质量
        //getSupportedFocusModes 获取受支持的对焦模式。
        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 连续对焦模式
        }

        mCamera.cancelAutoFocus();//自动对焦。
        mCamera.setDisplayOrientation(90);// 设置PreviewDisplay的方向，效果就是将捕获的画面旋转多少度显示
        mCamera.setParameters(parameters);
    }

    /**
     * 从列表中选取合适的分辨率
     * 默认w:h = 4:3
     * <p>注意：这里的w对应屏幕的height
     * h对应屏幕的width<p/>
     */
    private Camera.Size getProperSize(List<Camera.Size> pictureSizeList, float screenRatio) {
        Log.e(TAG, "screenRatio=" + screenRatio);
        Camera.Size result = null;
        for (Camera.Size size : pictureSizeList) {
            float currentRatio = ((float) size.width) / size.height;
            if (currentRatio - screenRatio == 0) {
                result = size;
                Log.e(TAG, "getProperSize 111");
                break;
            }
        }
        if (null == result) {
            for (Camera.Size size : pictureSizeList) {
                float curRatio = ((float) size.width) / size.height;
                if (curRatio == 4f / 3) {// 默认w:h = 4:3
                    result = size;
                    Log.e(TAG, "getProperSize 222");
                    break;
                }
            }
        }
        return result;
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        Log.i(TAG, "onAutoFocus success=" + success);
    }
}