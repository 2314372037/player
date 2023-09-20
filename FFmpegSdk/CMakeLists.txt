# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

project("player")

set(LIBS_DIR ${CMAKE_SOURCE_DIR}/libs/${CMAKE_ANDROID_ARCH_ABI})
set(HEADER_DIR ${CMAKE_SOURCE_DIR}/src/main/cpp/include)
include_directories(${HEADER_DIR})

message(WARNING "${LIBS_DIR}")
message(WARNING "${HEADER_DIR}")

find_library( # Sets the name of the path variable.
        log-lib
        log)

add_library(
        media_handler
        SHARED
        src/main/cpp/media_ffmpeg_method.cpp
        src/main/cpp/media_player_20.cpp
        src/main/cpp/media_frame_get.cpp)

add_library(libavcodec
        SHARED
        IMPORTED)
set_target_properties(libavcodec
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/libavcodec.so)

add_library(libavdevice
        SHARED
        IMPORTED)
set_target_properties(libavdevice
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/libavdevice.so)

add_library(libavfilter
        SHARED
        IMPORTED)
set_target_properties(libavfilter
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/libavfilter.so)

add_library(libavformat
        SHARED
        IMPORTED)
set_target_properties(libavformat
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/libavformat.so)

add_library(libavutil
        SHARED
        IMPORTED)
set_target_properties(libavutil
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}//libavutil.so)

add_library(libswresample
        SHARED
        IMPORTED)
set_target_properties(libswresample
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}//libswresample.so)

add_library(libswscale
        SHARED
        IMPORTED)
set_target_properties(libswscale
        PROPERTIES IMPORTED_LOCATION
        ${LIBS_DIR}/libswscale.so)

target_link_libraries(
        media_handler
        libavcodec
        libavdevice
        libavfilter
        libavformat
        libavutil
        libswresample
        libswscale
        -landroid #native_window
        -ljnigraphics #bitmap
        ${log-lib})
