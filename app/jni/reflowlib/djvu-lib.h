/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
#include "common.h"

/* Header for class com_example_sergey_djvu_viewer_ImageLoader */

#ifndef _Included_com_example_sergey_djvu_viewer_ImageLoader
#define _Included_com_example_sergey_djvu_viewer_ImageLoader
#ifdef __cplusplus
extern "C" {
#endif


JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeGrayscaleBytes
        (JNIEnv *, jclass, jlong, jint);

JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeBytes
        (JNIEnv *, jclass, jlong, jint);

    JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativePageGlyphs
        (JNIEnv *, jclass, jlong, jint, jobject);

    JNIEXPORT jobject JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeReflownBytes
        (JNIEnv *, jclass, jlong, jint, jfloat, jobject, jobject, jboolean);

    JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_getNativeTitle
        (JNIEnv *, jclass, jlong);


    JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_getNativeAuthor
        (JNIEnv *, jclass, jlong);



JNIEXPORT jlong JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_openBook
        (JNIEnv *, jobject, jstring);

    JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_getNumberOfPages
        (JNIEnv *, jobject, jlong);

    JNIEXPORT jstring JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBook_openStringBook
        (JNIEnv *, jobject,  jstring);

    /*
     * Class:     com_veve_flowreader_model_impl_djvu_DjvuBookPage
     * Method:    getNativeWidth
     * Signature: (JI)I
     */
    JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeWidth
        (JNIEnv *, jclass, jlong, jint);

    /*
     * Class:     com_veve_flowreader_model_impl_djvu_DjvuBookPage
     * Method:    getNativeHeight
     * Signature: (JI)I
     */
    JNIEXPORT jint JNICALL Java_com_veve_flowreader_model_impl_djvu_DjvuBookPage_getNativeHeight
        (JNIEnv *, jclass, jlong, jint);

    image_format get_djvu_pixels(JNIEnv*, jlong, jint, jboolean, char**);

#ifdef __cplusplus
}

#endif
#endif
