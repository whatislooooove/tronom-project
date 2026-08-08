#include <jni.h>
#include "MetronomeEngine.h"

extern "C" {

JNIEXPORT jlong JNICALL
Java_com_example_drummersmetronome_audio_MetronomeEngine_nativeCreate(JNIEnv *env, jobject thiz) {
    return reinterpret_cast<jlong>(new MetronomeEngine());
}

JNIEXPORT void JNICALL
Java_com_example_drummersmetronome_audio_MetronomeEngine_nativeDelete(JNIEnv *env, jobject thiz, jlong engine_ptr) {
    delete reinterpret_cast<MetronomeEngine *>(engine_ptr);
}

JNIEXPORT void JNICALL
Java_com_example_drummersmetronome_audio_MetronomeEngine_nativeSetBpm(JNIEnv *env, jobject thiz, jlong engine_ptr, jint bpm) {
    reinterpret_cast<MetronomeEngine *>(engine_ptr)->setBpm(bpm);
}

JNIEXPORT void JNICALL
Java_com_example_drummersmetronome_audio_MetronomeEngine_nativeSetSignature(JNIEnv *env, jobject thiz, jlong engine_ptr, jint numerator, jint denominator) {
    reinterpret_cast<MetronomeEngine *>(engine_ptr)->setSignature(numerator, denominator);
}

JNIEXPORT void JNICALL
Java_com_example_drummersmetronome_audio_MetronomeEngine_nativeSetPlaying(JNIEnv *env, jobject thiz, jlong engine_ptr, jboolean is_playing) {
    reinterpret_cast<MetronomeEngine *>(engine_ptr)->setPlaying(is_playing);
}

JNIEXPORT jint JNICALL
Java_com_example_drummersmetronome_audio_MetronomeEngine_nativeGetAndClearBeatMarker(JNIEnv *env, jobject thiz, jlong engine_ptr) {
    return reinterpret_cast<MetronomeEngine *>(engine_ptr)->getAndClearBeatMarker();
}

}
