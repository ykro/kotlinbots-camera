package com.bitandik.labs.kotlinbots.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.media.ImageReader
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.TAG

import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import kotlinx.android.synthetic.main.activity_camera.*

class CameraActivity : Activity() {
    private lateinit var camera: RobotCamera
    private lateinit var cameraHandler: Handler
    private lateinit var cameraThread: HandlerThread

    private lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        setContentView(R.layout.activity_camera)

        if (checkSelfPermission(Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
          Log.e(TAG, "No permission")
          return
        }

        cameraThread = HandlerThread("CameraBackground")
        cameraThread.start()
        cameraHandler = Handler(cameraThread.looper)

        camera = RobotCamera
        camera.initializeCamera(this, cameraHandler, onImageAvailableListener)

        button = RainbowHat.openButtonA()
        button.setOnButtonEventListener { _, pressed -> if (pressed) camera.takePicture() }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        camera.shutDown()
        cameraThread.quitSafely()
    }

    private var onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        reader?.let {
            val image = it.acquireLatestImage()

            val imageBuf = image.planes[0].buffer
            val imageBytes = ByteArray(imageBuf.remaining())
            imageBuf.get(imageBytes)
            image.close()

            val bitmapImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, null)

            /*
            launch(UI) {
                imageView.setImageBitmap(bitmapImage)
            }
            */

            runOnUiThread {
              imageView.setImageBitmap(bitmapImage)
            }

        }
    }
}

