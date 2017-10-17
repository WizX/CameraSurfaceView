package com.selfimpr.surface;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * surfaceview变得可见时，surface被创建；surfaceview隐藏前，surface被销毁。这样能节省资源。
 * 如果你要查看 surface被创建和销毁的时机，可以重载surfaceCreated(SurfaceHolder)和 surfaceDestroyed(SurfaceHolder)。
 * <p>
 * surfaceview的核心在于提供了两个线程：UI线程和渲染线程。这里应注意：
 * (1)所有SurfaceView和SurfaceHolder.Callback的方法都应该在UI线程里调用，一般来说就是应用程序主线程。渲染线程所要访问的各种变量应该作同步处理。
 * (2)由于surface可能被销毁，它只在SurfaceHolder.Callback.surfaceCreated()和 SurfaceHolder.Callback.surfaceDestroyed()之间有效，
 * 所以要确保渲染线程访问的是合法有效的surface。
 */
public class MainActivity extends AppCompatActivity {

    private CameraSurfaceView vSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        vSurface = (CameraSurfaceView) findViewById(R.id.surface_view);
        vSurface.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vSurface.takePicture();
            }
        });
    }
}
