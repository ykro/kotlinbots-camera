package com.bitandik.labs.kotlinbots.camera

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.util.Base64
import android.util.Log
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.CHILD_IMAGE
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.CHILD_TIMESTAMP
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.REFERENCE_IMAGES
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.TAG
import com.google.android.things.contrib.driver.button.Button
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.firebase.database.FirebaseDatabase
import java.time.LocalDateTime



class FirebaseCameraActivity : Activity() {
    private lateinit var camera: RobotCamera
    private lateinit var cameraHandler: Handler
    private lateinit var cameraThread: HandlerThread

    private lateinit var button: Button
    private lateinit var database: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        database= FirebaseDatabase.getInstance()

        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
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

    private fun storeImageOnDatabase(imageBytes: ByteArray) {
        val imageString = Base64.encodeToString(imageBytes, Base64.NO_WRAP or Base64.URL_SAFE)
        val reference = database.getReference(REFERENCE_IMAGES).push()
        reference.child(CHILD_IMAGE).setValue(imageString)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            reference.child(CHILD_TIMESTAMP).setValue(LocalDateTime.now().toString())
        }
    }

    private var onImageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        reader?.let {
            val image = it.acquireLatestImage()

            val imageBuf = image.planes[0].buffer
            val imageBytes = ByteArray(imageBuf.remaining())
            imageBuf.get(imageBytes)
            image.close()

            storeImageOnDatabase(imageBytes)
        }
    }
}