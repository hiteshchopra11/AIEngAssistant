# Sets the minimum CMake version required for this project.
cmake_minimum_required(VERSION 3.22.1)

# Declares the project name
project("llama-android")

# Set C++ standard
set(CMAKE_CXX_STANDARD 11)
set(CMAKE_CXX_STANDARD_REQUIRED ON)

# Path to llama.cpp submodule
set(LLAMA_CPP_DIR "${CMAKE_CURRENT_SOURCE_DIR}/../../../../third-party/llama.cpp")

# Configure llama.cpp build options for Android
set(LLAMA_CURL OFF CACHE BOOL "llama: use libcurl to download model from an URL" FORCE)
set(LLAMA_BUILD_TESTS OFF CACHE BOOL "llama: build tests" FORCE)
set(LLAMA_BUILD_EXAMPLES OFF CACHE BOOL "llama: build examples" FORCE)
set(LLAMA_BUILD_SERVER OFF CACHE BOOL "llama: build server example" FORCE)
set(GGML_LLAMAFILE OFF CACHE BOOL "ggml: use llamafile sgemm" FORCE)

# Add llama.cpp as subdirectory
add_subdirectory(${LLAMA_CPP_DIR} llama.cpp EXCLUDE_FROM_ALL)

# Create shared library
add_library(${CMAKE_PROJECT_NAME} SHARED
    # List C/C++ source files with relative paths to this CMakeLists.txt.
    llama-android.cpp)

# Include directories for llama.cpp headers
target_include_directories(${CMAKE_PROJECT_NAME} PRIVATE
    ${LLAMA_CPP_DIR}/include
    ${LLAMA_CPP_DIR}/common
    ${LLAMA_CPP_DIR}/ggml/include
    ${LLAMA_CPP_DIR}/src)

# Link llama.cpp libraries
target_link_libraries(${CMAKE_PROJECT_NAME}
    llama
    ggml
    common
    android
    log)