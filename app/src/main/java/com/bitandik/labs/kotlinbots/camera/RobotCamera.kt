package com.bitandik.labs.kotlinbots.camera

import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Log
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.IMAGE_HEIGHT
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.IMAGE_WIDTH
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.MAX_IMAGES
import com.bitandik.labs.kotlinbots.camera.Constants.Companion.TAG

/**
 * Created by ykro.
 */
object RobotCamera {
    private var imageReader: ImageReader? = null
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null

    fun initializeCamera(context: Context,
                         backgroundHandler: Handler,
                         imageAvailableListener: ImageReader.OnImageAvailableListener) {

        val manager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
        var id: String? = null
        try {
            var camIds:Array<String> = manager.cameraIdList
            if (camIds.isEmpty()) {
                Log.d(TAG, "No cameras available")
                return
            }
            id = camIds[0]
        } catch (e: CameraAccessException) {
            Log.d(TAG, "Can't access camera", e)
        }

        imageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                                              ImageFormat.JPEG, MAX_IMAGES)
        imageReader?.setOnImageAvailableListener(imageAvailableListener, backgroundHandler)
        try {
            manager.openCamera(id, stateCallback, backgroundHandler)

        } catch (cae: CameraAccessException) {
            Log.d(TAG, "Camera access exception", cae)
        }

    }

    fun shutDown() {
        cameraDevice?.close()
    }

    fun takePicture() {
        cameraDevice?.let {
            it.createCaptureSession(
                    listOf(imageReader?.surface),
                    sessionCallback,
                    null)
        }
    }

    fun captureRequest(){
        try {
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)

            captureBuilder.addTarget(imageReader?.surface)
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON)
            captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO)
            Log.d(TAG, "Capture captureRequest created.")

            captureSession?.capture(captureBuilder.build(), captureCallback, null)
        } catch (e: CameraAccessException) {
            Log.d(TAG, "Can't access camera for capture", e)
        }

    }

    private var sessionCallback: CameraCaptureSession.StateCallback =
            object : CameraCaptureSession.StateCallback() {
                override fun onConfigureFailed(session: CameraCaptureSession?) {}

                override fun onConfigured(session: CameraCaptureSession?) {
                    cameraDevice?.let {
                        captureSession = session
                        captureRequest()
                    }
                }
            }

    private var captureCallback: CameraCaptureSession.CaptureCallback =
            object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession?,
                                                request: CaptureRequest?,
                                                result: TotalCaptureResult?) {
                    super.onCaptureCompleted(session, request, result)
                    captureSession?.close()
                    captureSession = null
                    Log.d(TAG, "Capture session closed")
                }
            }

    private var stateCallback: CameraDevice.StateCallback =
            object : CameraDevice.StateCallback() {
                override fun onOpened(device: CameraDevice?) {
                    cameraDevice = device
                }

                override fun onDisconnected(device: CameraDevice?) {
                    captureSession?.close()
                    captureSession = null
                    device?.close()

                }

                override fun onError(device: CameraDevice?, p1: Int) {
                    captureSession?.close()
                    captureSession = null
                    device?.close()
                }

                override fun onClosed(camera: CameraDevice?) {
                    super.onClosed(camera)
                    cameraDevice = null
                }
            }
}