#ifndef DRUMMERSMETRONOME_METRONOMEENGINE_H
#define DRUMMERSMETRONOME_METRONOMEENGINE_H

#include <oboe/Oboe.h>
#include <atomic>
#include <math.h>

class MetronomeEngine : public oboe::AudioStreamDataCallback {
public:
    MetronomeEngine();
    virtual ~MetronomeEngine();

    void setBpm(int bpm);
    void setSignature(int numerator, int denominator);
    void setPlaying(bool isPlaying);
    bool isPlaying() const { return mIsPlaying.load(); }

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) override;

    // To be called from JNI to check if a beat occurred
    int getAndClearBeatMarker();

private:
    void generateClick(float* buffer, int32_t numFrames, float freq, double durationSec);

    std::shared_ptr<oboe::AudioStream> mStream;
    std::atomic<bool> mIsPlaying{false};
    std::atomic<int> mBpm{120};
    std::atomic<int> mNumerator{4};
    std::atomic<int> mDenominator{4};

    std::atomic<int> mBeatMarker{-1}; // -1 means no beat, 0..N means beat index

    double mSampleRate = 44100.0;
    double mTotalSamples = 0;
    int mCurrentBeat = 0;

    // Pre-calculated click parameters
    const double mClickDuration = 0.02;
};

#endif //DRUMMERSMETRONOME_METRONOMEENGINE_H
