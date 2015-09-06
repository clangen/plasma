package org.clangen.gfx.plasma;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.WindowManager;

public class Plasma {
    private static final String TAG = "Plasma";

    private static final int PALLETTE_SIZE = 256;
    private static final int FRAMES_PER_SEC = 22;
    private static final int MILLIS_PER_FRAME = 1000 / FRAMES_PER_SEC;

    private static Plasma sInstance;
    private static int sThreadPriority = Thread.MIN_PRIORITY;

    private Context mContext;
    private SurfaceHolder mSurfaceHolder;
    private Effect mEffect;
    private DrawThread mDrawThread;
    private DisplayMetrics mDisplayMetrics = new DisplayMetrics();

    static {
        System.loadLibrary("plasma");
    }

    private Plasma(Context context) {
        mContext = context.getApplicationContext();

        mEffect = Effect.getInstance(mContext);

        WindowManager wm = (WindowManager)
            mContext.getSystemService(Context.WINDOW_SERVICE);

        wm.getDefaultDisplay().getMetrics(mDisplayMetrics);
    }

    public static Plasma getInstance(Context context) {
        synchronized (Plasma.class){
            if (sInstance == null) {
                sInstance = new Plasma(context);
            }

            return sInstance;
        }
    }

    public static void onEffectChanged() {
        synchronized (Plasma.class) {
            if (sInstance != null) {
                sInstance.start(sInstance.mSurfaceHolder);
            }
        }
    }

    public static void setThreadPriority(int priority) {
        synchronized (Plasma.class) {
            if (sThreadPriority != priority) {
                sThreadPriority = priority;
                onEffectChanged();
            }
        }
    }

    public synchronized void start(SurfaceHolder surfaceHolder) {
        stop(mSurfaceHolder);
        mSurfaceHolder = surfaceHolder;

        if (surfaceHolder != null) {
            mDrawThread = new DrawThread(this);
            mDrawThread.start();
        }
    }

    public synchronized void stop(SurfaceHolder surfaceHolder) {
        if (mSurfaceHolder != surfaceHolder) {
            return;
        }

        if (mDrawThread != null) {
            mDrawThread.cancel();
            mDrawThread = null;
        }

        mSurfaceHolder = null;
    }

    public synchronized SurfaceHolder getHolder() {
        return mSurfaceHolder;
    }

    private static native void nativeSetFrequency(int frequency);
    private static native void nativeSetPallette(int pallette[], int size);
    private static native void nativeNextFrame(Buffer pixels, int width, int height);
    private static native void nativeSetSpeed(int speed1, int speed2, int speed3, int speed4);

    private static class DrawThread extends Thread {
        private static Bitmap sBitmap = null;
        private static IntBuffer sPixelBuffer = null;
        private static int sPixelBufferSize = 0;

        private volatile boolean mCancel;
        private int mWidth, mHeight;
        private CountDownLatch mStopLatch;
        private SurfaceHolder mSurfaceHolder;
        private Effect mEffect;
        private Plasma mPlasma;

        public DrawThread(Plasma plasma) {
            setName("PlasmaService.DrawThread");
            setPriority(sThreadPriority);

            mPlasma = plasma;
            mSurfaceHolder = mPlasma.mSurfaceHolder;
            mEffect = mPlasma.mEffect;
            mCancel = false;
            mStopLatch = new CountDownLatch(1);
        }

        public void cancel() {
            mCancel = true;

            try {
                mStopLatch.await();
            }
            catch (InterruptedException ex) {
                throw new RuntimeException("Latch wait timed out unexpectedly");
            }
        }

        public void run() {
            try {
                synchronized (DrawThread.class){
                    cacheDimensions();
                    checkedInitBitmap();
                    checkedInitNativeBuffer();
                    initPallette();

                    if (sBitmap == null && sPixelBuffer == null) {
                        Log.i(TAG, "DrawThread: bitmap or buffer null!");
                        return;
                    }

                    while (!mCancel) {
                        long start = System.currentTimeMillis();
                        final Canvas canvas = mSurfaceHolder.lockCanvas();

                        if (canvas != null) {
                            try {
                            	sPixelBuffer.rewind(); /* necessary!! the copy call
                            	below will not rewind, so trying to copy again will
                            	cause an exception */

                                nativeNextFrame(sPixelBuffer, mWidth, mHeight);
                                sBitmap.copyPixelsFromBuffer(sPixelBuffer);
                                canvas.drawBitmap(sBitmap, 0.0f, 0.0f, null);
                            }
                            catch(Throwable ex) {
                                Log.i(TAG, "failed to draw frame");
                            }
                            finally {
                            	mSurfaceHolder.unlockCanvasAndPost(canvas);                        	
                            }                        	
                        }
                        
                        long elapsed = System.currentTimeMillis() - start;
                        long delay = MILLIS_PER_FRAME - elapsed;

                        if (delay > 0) {
                            try {
                                Thread.sleep(delay);
                            }
                            catch (InterruptedException ex) {
                            }
                        }
                    }
                }
            }
            finally {
                mStopLatch.countDown();
            }
        }

        private void cacheDimensions() {
            Canvas canvas = mSurfaceHolder.lockCanvas();
            if (canvas != null) {
                try {
                    mWidth = canvas.getWidth();
                    mHeight = canvas.getHeight();
                }
                finally {
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }

        private void checkedInitBitmap() {
            if (sBitmap != null) {
                if ((mWidth == sBitmap.getWidth()) && (mHeight == sBitmap.getHeight())) {
                    return;
                }
            }

//            Log.i(TAG, "flipping orientation!");

            if (sBitmap != null) {
                sBitmap.recycle();
            }

            if (mWidth > 0 && mHeight > 0) {
                sBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            }
        }

        private void checkedInitNativeBuffer() {
        	int newBufferSize = mWidth * mHeight * 4;
        	Log.i(TAG, "buffer size: " + newBufferSize);
            if ((sPixelBuffer == null) || (sPixelBufferSize != newBufferSize)) {
//                Log.i(TAG, "creating native buffer!");

                if (newBufferSize > 0) {
                    sPixelBufferSize = newBufferSize;
                    sPixelBuffer = ByteBuffer.allocateDirect(newBufferSize).asIntBuffer();
                }
            }
        }

        private void initPallette() {
            final int pallette[] = new int[PALLETTE_SIZE];

            final int   mR1 = mEffect.getRedAmount();
            final float mR2 = mEffect.getRedIntensity();
            final float mR3 = mEffect.getRedWavelength();
            final int   mG1 = mEffect.getGreenAmount();
            final float mG2 = mEffect.getGreenIntensity();
            final float mG3 = mEffect.getGreenWavelength();
            final int   mB1 = mEffect.getBlueAmount();
            final float mB2 = mEffect.getBlueIntensity();
            final float mB3 = mEffect.getBlueWavelength();

            for (int i = 0; i < pallette.length; i++) {
                pallette[i] =  Color.rgb(
                    clampByte(mR1 + (int)(mR2 * Math.sin((double)i * 3.1415 / mR3))),
                    clampByte(mG1 + (int)(mG2 * Math.sin((double)i * 3.1415 / mG3))),
                    clampByte(mB1 + (int)(mB2 * Math.cos((double)i * 3.1415 / mB3))));
            }

            nativeSetPallette(pallette, pallette.length);

            nativeSetFrequency(mEffect.getSize());

            nativeSetSpeed(
                mEffect.getXPulseSpeed(), mEffect.getXMoveSpeed(),
                mEffect.getYPulseSpeed(), mEffect.getYMoveSpeed());
        }

        private static int clampByte(int input) {
            return Math.min(255, Math.max(0, input));
        }
    }
}
