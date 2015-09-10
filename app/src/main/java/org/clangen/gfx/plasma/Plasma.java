package org.clangen.gfx.plasma;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Log;
import android.view.SurfaceHolder;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CountDownLatch;

public class Plasma {
    private static final String TAG = "Plasma";

    private static final int PALETTE_SIZE = 256;
    private static final int FRAMES_PER_SEC = 22;
    private static final int MILLIS_PER_FRAME = 1000 / FRAMES_PER_SEC;

    private static Plasma sInstance;

    private SurfaceHolder mSurfaceHolder;
    private Effect mEffect;
    private DrawThread mDrawThread;

    static {
        System.loadLibrary("plasma");
    }

    private Plasma(Context context) {
        context = context.getApplicationContext();
        mEffect = Effect.getInstance(context);
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

    private static native void nativeSetFrequency(int frequency);
    private static native void nativeSetPalette(int palette[], int size);
    private static native void nativeNextFrame(Buffer pixels, int width, int height);
    private static native void nativeSetMovement(int xModifier1, int xModifier2, int yModifier1, int yModifier2);
    private static native void nativeSetShape(int xModifier1, int xModifier2, int yModifier1, int yModifier2);

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
            setPriority(Thread.NORM_PRIORITY);

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
                    initPalette();

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

        private void initPalette() {
            final int palette[] = new int[PALETTE_SIZE];

            final int   mR1 = mEffect.getRedBrightness();
            final float mR2 = mEffect.getRedContrast();
            final float mR3 = mEffect.getRedFrequency();
            final int   mG1 = mEffect.getGreenBrightness();
            final float mG2 = mEffect.getGreenContrast();
            final float mG3 = mEffect.getGreenFrequency();
            final int   mB1 = mEffect.getBlueBrightness();
            final float mB2 = mEffect.getBlueContrast();
            final float mB3 = mEffect.getBlueFrequency();

            for (int i = 0; i < palette.length; i++) {
                palette[i] =  Color.rgb(
                    clampByte(mR1 + (int)(mR2 * Math.sin((double)i * 3.1415 / mR3))),
                    clampByte(mG1 + (int)(mG2 * Math.sin((double)i * 3.1415 / mG3))),
                    clampByte(mB1 + (int)(mB2 * Math.sin((double)i * 3.1415 / mB3))));
            }

            nativeSetPalette(palette, palette.length);

            nativeSetFrequency(mEffect.getSize());

            nativeSetMovement(
                mEffect.getXMoveModifier1(),
                mEffect.getXMoveModifier2(),
                mEffect.getYMoveModifier1(),
                mEffect.getYMoveModifier2());

            nativeSetShape(
                mEffect.getXShapeModifier1(),
                mEffect.getXShapeModifier2(),
                mEffect.getYShapeModifier1(),
                mEffect.getYShapeModifier2());
        }

        private static int clampByte(int input) {
            return Math.min(255, Math.max(0, input));
        }
    }
}
