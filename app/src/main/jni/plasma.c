#include <string.h>
#include <jni.h>
#include <math.h>
#include <stdio.h>
#include <android/log.h>

jint* _palette = 0;
int _sin[512] = { 0 };
int _nextFrame[4] = { 0 };
int _currentFrame[4] = { 0 };
int _xMovementModifier1 = 1;
int _xMovementModifier2 = 4;
int _yMovementModifier1 = 1;
int _yMovementModifier2 = 4;
int _xShapeModifier1 = 1;
int _xShapeModifier2 = 2;
int _yShapeModifier1 = 1;
int _yShapeModifier2 = 2;

void buildSinTable(int frequency) {
    // we need 1 "complete" sin wave -- that is, 360 degrees worth
    // of data in 512 discrete values. sin() accepts arguments in radians,
    // so do some simple math to figure out how many degrees there are
    // per step (360/512), then convert to radians (DEG * (PI/180))
    double radPerStep, degrees;
    int i;
    degrees = (float) (360 * frequency);
    radPerStep = ((degrees / 512.0) * (3.1415 / 180.0));
    for (i = 0; i < 512; i++) {
        // scale values to be between 0 and 255 (number of elements in
        // our pallete, generated below)
        _sin[i] = 128 + (int)(127.0 * sin((double)i * radPerStep));
    }
}

void Java_org_clangen_gfx_plasma_Plasma_nativeSetFrequency(
    JNIEnv* env,
    jobject obj,
    jint frequency)
{
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeSetFrequency start");
    buildSinTable((int) frequency);
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeSetFrequency end");
}

void Java_org_clangen_gfx_plasma_Plasma_nativeSetMovement(
    JNIEnv* env,
    jobject obj,
    jint xModifier1,
    jint xModifier2,
    jint yModifier1,
    jint yModifier2)
{
    _xMovementModifier1 = (int) xModifier1;
    _xMovementModifier2 = (int) xModifier2;
    _yMovementModifier1 = (int) yModifier1;
    _yMovementModifier2 = (int) yModifier2;
}

void Java_org_clangen_gfx_plasma_Plasma_nativeSetShape(
        JNIEnv* env,
        jobject obj,
        jint xModifier1,
        jint xModifier2,
        jint yModifier1,
        jint yModifier2)
{
    _xShapeModifier1 = (int) xModifier1;
    _xShapeModifier2 = (int) xModifier2;
    _yShapeModifier1 = (int) yModifier1;
    _yShapeModifier2 = (int) yModifier2;
}

void Java_org_clangen_gfx_plasma_Plasma_nativeSetPalette(
    JNIEnv* env,
    jobject obj,
    jintArray palette, 
    jint count)
{
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeSetPalette start");
    if (_palette == 0) {
        _palette = (jint*) malloc(count * sizeof(jint));
    }
    (*env)->GetIntArrayRegion(env, palette, 0, count, _palette);
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeSetPalette end");
}

void Java_org_clangen_gfx_plasma_Plasma_nativeNextFrame(
    JNIEnv* env,
    jobject obj,
    jobject buffer,
    jint width,
    jint height)
{
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeNextFrame start: %d %d", width, height);
    int index;
    jint *target;
    jint x, y;
    jint halfWidth, halfHeight;
    jint color;
    jint totalPixels;
    jint pos = 0;

    halfWidth = width / 2;
    halfHeight = height / 2;
    totalPixels = width * height;     
    target = (jint *) (*env)->GetDirectBufferAddress(env, buffer);

    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeNextFrame %x %d %d", target, width, height);

    _currentFrame[2] = _nextFrame[2];
    _currentFrame[3] = _nextFrame[3];

#define SET_PIXEL(target, index, color) \
  target[(index)] = color;
  //if ((index) < (totalPixels)) target[(index)] = color; \
  //else __android_log_print(ANDROID_LOG_INFO, "Plasma", "overflow! %d", index);

    for (y = 0; y < halfHeight; y++)
    {
        _currentFrame[2] &= 0x1ff;
        _currentFrame[3] &= 0x1ff;

        _currentFrame[0] = _nextFrame[0];
        _currentFrame[1] = _nextFrame[1];

        for (x = 0; x < halfWidth; x++)
        {
            _currentFrame[0] &= 0x1ff;
            _currentFrame[1] &= 0x1ff;

            index = (
                _sin[_currentFrame[0]] +
                _sin[_currentFrame[1]] +
                _sin[_currentFrame[2]] +
                _sin[_currentFrame[3]]) >> 2;

            color = _palette[index];
 
            SET_PIXEL(target, pos + 0, color);
            SET_PIXEL(target, pos + 1, color);
            SET_PIXEL(target, pos + width + 0, color);
            SET_PIXEL(target, pos + width + 1, color);

            _currentFrame[0] += _xShapeModifier1;
            _currentFrame[1] += _xShapeModifier2;

            pos += 2;
        }

        pos += width;

        _currentFrame[2] += _yShapeModifier1;
        _currentFrame[3] += _yShapeModifier2;
    }

    _nextFrame[0] += _xMovementModifier1;
    _nextFrame[1] += _xMovementModifier2;
    _nextFrame[2] += _yMovementModifier1;
    _nextFrame[3] += _yMovementModifier2;
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeNextFrame end");
}
