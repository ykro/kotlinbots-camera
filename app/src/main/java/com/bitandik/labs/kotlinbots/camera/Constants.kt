package com.bitandik.labs.kotlinbots.camera

/**
 * Created by ykro.
 */
class Constants {
    companion object {
        const val TAG = "kotlinbots"

        //Camera
        const val IMAGE_WIDTH = 640
        const val IMAGE_HEIGHT = 480
        const val MAX_IMAGES = 1

        //Firebase
        const val REFERENCE_IMAGES = "images"
        const val CHILD_IMAGE = "image"
        const val CHILD_TIMESTAMP = "timestamp"
    }
}