#include <string.h>
#include <jni.h>
#include <math.h>
#include <stdio.h>
#include <android/log.h>

static const int MAX_INT = 2147483647;

jint* _pallette = 0;
int _sin[512] = { 0 };
int _plasmaPos[4] = { 0 };
int _framePos[4] = { 0 };
int _speed1 = 1;
int _speed2 = 4;
int _speed3 = 1;
int _speed4 = 4;

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

void Java_org_clangen_gfx_plasma_Plasma_nativeSetSpeed(
    JNIEnv* env,
    jobject obj,
    jint speed1,
    jint speed2,
    jint speed3,
    jint speed4)
{
    _speed1 = (int) speed1;
    _speed2 = (int) speed2;
    _speed3 = (int) speed3;
    _speed4 = (int) speed4;
}

void Java_org_clangen_gfx_plasma_Plasma_nativeSetPallette(
    JNIEnv* env,
    jobject obj,
    jintArray pallette, 
    jint count)
{
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeSetPallette start");
    if (_pallette == 0) {
        _pallette = (jint*) malloc(count * sizeof(jint));
    }
    (*env)->GetIntArrayRegion(env, pallette, 0, count, _pallette);
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeSetPallette end");
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

    _framePos[2] = _plasmaPos[2];
    _framePos[3] = _plasmaPos[3];

#define SET_PIXEL(target, index, color) \
  target[(index)] = color;
  //if ((index) < (totalPixels)) target[(index)] = color; \
  //else __android_log_print(ANDROID_LOG_INFO, "Plasma", "overflow! %d", index);

    for (y = 0; y < halfHeight; y++)
    {
        _framePos[2] &= 0x1ff;
        _framePos[3] &= 0x1ff;

        _framePos[0] = _plasmaPos[0];
        _framePos[1] = _plasmaPos[1];

        for (x = 0; x < halfWidth; x++)
        {
            _framePos[0] &= 0x1ff;
            _framePos[1] &= 0x1ff;

            index = (
                _sin[_framePos[0]] +
                _sin[_framePos[1]] +
                _sin[_framePos[2]] +
                _sin[_framePos[3]]) >> 2;

            color = _pallette[index];
 
            SET_PIXEL(target, pos + 0, color);
            SET_PIXEL(target, pos + 1, color);
            SET_PIXEL(target, pos + width + 0, color);
            SET_PIXEL(target, pos + width + 1, color);

            _framePos[0] += 1; 
            _framePos[1] += 2;

            pos += 2;
        }

        pos += width;

        _framePos[2] += 1;
        _framePos[3] += 2;
    }

    _plasmaPos[0] += _speed1;
    _plasmaPos[1] += _speed2;
    _plasmaPos[2] += _speed3;
    _plasmaPos[3] += _speed4;
    //__android_log_print(ANDROID_LOG_INFO, "Plasma", "nativeNextFrame end");
}
