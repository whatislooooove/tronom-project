#include "MetronomeEngine.h"

MetronomeEngine::MetronomeEngine() {
    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output)
           ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
           ->setSharingMode(oboe::SharingMode::Exclusive)
           ->setFormat(oboe::AudioFormat::Float)
           ->setChannelCount(oboe::ChannelCount::Mono)
           ->setDataCallback(this)
           ->openStream(mStream);

    if (mStream) {
        mSampleRate = mStream->getSampleRate();
    }
}

MetronomeEngine::~MetronomeEngine() {
    if (mStream) {
        mStream->close();
    }
}

void MetronomeEngine::setBpm(int bpm) {
    mBpm.store(bpm);
}

void MetronomeEngine::setSignature(int numerator, int denominator) {
    mNumerator.store(numerator);
    mDenominator.store(denominator);
}

void MetronomeEngine::setPlaying(bool isPlaying) {
    if (isPlaying && !mIsPlaying.load()) {
        mCurrentBeat = 0;
        mTotalSamples = 0;
        if (mStream) mStream->requestStart();
    } else if (!isPlaying && mIsPlaying.load()) {
        if (mStream) mStream->requestStop();
    }
    mIsPlaying.store(isPlaying);
}

int MetronomeEngine::getAndClearBeatMarker() {
    return mBeatMarker.exchange(-1);
}

oboe::DataCallbackResult MetronomeEngine::onAudioReady(oboe::AudioStream *audioStream, void *audioData, int32_t numFrames) {
    float *output = static_cast<float *>(audioData);

    int bpm = mBpm.load();
    int numerator = mNumerator.load();
    int denominator = mDenominator.load();

    // Calculate samples per beat
    double samplesPerBeat = (60.0 / bpm) * (4.0 / denominator) * mSampleRate;

    for (int i = 0; i < numFrames; ++i) {
        double frameOffset = mTotalSamples + i;
        double beatProgress = fmod(frameOffset, samplesPerBeat);

        // If we just crossed a beat boundary
        if (beatProgress < 1.0 && (frameOffset - mTotalSamples) < 1.0) {
            mBeatMarker.store(mCurrentBeat);
            mCurrentBeat = (mCurrentBeat + 1) % numerator;
        }

        // Generate click sound
        float sample = 0;
        double freq = (mCurrentBeat == 1) ? 1500.0 : 1000.0; // Beat index was already incremented
        double timeSec = beatProgress / mSampleRate;

        if (timeSec < mClickDuration) {
            double envelope = 1.0 - (timeSec / mClickDuration);
            sample = sin(2.0 * M_PI * freq * timeSec) * 0.5 * envelope;
        }

        output[i] = sample;
    }

    mTotalSamples += numFrames;
    return oboe::DataCallbackResult::Continue;
}
