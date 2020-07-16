
cmake_minimum_required(VERSION 3.4.1)

set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -std=c++11")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -O2")
set(CMAKE_CXX_FLAGS "${CMAKE_CXX_FLAGS} -msse4.1")

set(PRELIB_ROOT ${CMAKE_SOURCE_DIR})
set(SEETAFACE_LANDMARKER_DIR ${PRELIB_ROOT}/seetaface2/landmarker)
set(SEETAFACE_SEETANET_DIR ${PRELIB_ROOT}/seetaface2/seetanet)

INCLUDE_DIRECTORIES(
        ${SEETAFACE_LANDMARKER_DIR}/
        ${SEETAFACE_SEETANET_DIR}/include_inner
        ${SEETAFACE_SEETANET_DIR}/include_inner/layers
        ${SEETAFACE_SEETANET_DIR}/men
        ${SEETAFACE_SEETANET_DIR}/proto
        ${SEETAFACE_SEETANET_DIR}/ )

###opencv
ADD_LIBRARY(opencv
        SHARED
        IMPORTED)

SET_TARGET_PROPERTIES(opencv
        PROPERTIES
        IMPORTED_LOCATION
        ${PRELIB_ROOT}/libs/${ANDROID_ABI}/libopencv_java3.so)



####seetaface
FILE(GLOB SEETAFACE_SEETANET_SRCS "${SEETAFACE_SEETANET_DIR}/*.cpp")
FILE(GLOB SEETAFACE_SEETANET_MEMS "${SEETAFACE_SEETANET_DIR}/mem/*.cpp")
FILE(GLOB SEETAFACE_SEETANET_PROTOS "${SEETAFACE_SEETANET_DIR}/proto/*.cc")
FILE(GLOB SEETAFACE_SEETANET_ORZ_MEMS "${SEETAFACE_SEETANET_DIR}/orz/mem/*.cpp")
FILE(GLOB SEETAFACE_SEETANET_ORZ_SYNC "${SEETAFACE_SEETANET_DIR}/orz/sync/*.cpp")
FILE(GLOB SEETAFACE_SEETANET_ORZ_TOOLS "${SEETAFACE_SEETANET_DIR}/orz/tools/*.cpp")

ADD_LIBRARY(seetaface_anet
            SHARED
            ${SEETAFACE_SEETANET_SRCS}
            ${SEETAFACE_SEETANET_MEMS}
            ${SEETAFACE_SEETANET_PROTOS}
            ${SEETAFACE_SEETANET_ORZ_MEMS}
            ${SEETAFACE_SEETANET_ORZ_SYNC}
            ${SEETAFACE_SEETANET_ORZ_TOOLS}  )


FILE(GLOB SEETAFACE_LANDMARKER_SRCS "${SEETAFACE_LANDMARKER_DIR}/*.cpp")
ADD_LIBRARY(
            learnopengl
            SHARED
            ${SEETAFACE_LANDMARKER_SRCS}
            native-lib.cpp )

FIND_LIBRARY(
              log-lib
              log )


TARGET_LINK_LIBRARIES(
                    learnopengl
                    seetaface_anet
                    opencv
                    android
                    ${log-lib} )