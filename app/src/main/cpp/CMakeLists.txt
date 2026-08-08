cmake_minimum_required(VERSION 3.22.1)

project("drummersmetronome")

# Path to Oboe
set(OBOE_DIR ${CMAKE_CURRENT_SOURCE_DIR}/oboe)
add_subdirectory(${OBOE_DIR} ./oboe)

add_library(metronome SHARED
    metronome/MetronomeEngine.cpp
    metronome/jni-bridge.cpp
)

target_include_directories(metronome PRIVATE
    metronome
    ${OBOE_DIR}/include
)

target_link_libraries(metronome
    oboe
    log
    android
)
