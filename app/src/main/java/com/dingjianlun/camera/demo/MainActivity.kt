package com.dingjianlun.camera.demo

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.VideoCapture
import androidx.camera.view.CameraView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {

    companion object {
        private const val REQUEST_CODE_PERMISSIONS = 10
        private val REQUIRED_PERMISSIONS =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
    }

    private val outputDirectory by lazy {
        File(getExternalFilesDir(null), "/camera")
            .apply { mkdirs() }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val isPermission = REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
        if (!isPermission) {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS)
            return
        }

        btn_takePhoto.setOnClickListener { takePhoto() }

        btn_startRecording.setOnClickListener { startRecord() }
        btn_stopRecording.setOnClickListener { stopRecord() }

        btn_toggleCamera.setOnClickListener { cameraView.toggleCamera() }

        startCamera()
    }

    @RequiresPermission(Manifest.permission.CAMERA)
    private fun startCamera() {
        cameraView.captureMode = CameraView.CaptureMode.MIXED
        cameraView.bindToLifecycle(this)
    }

    private fun takePhoto() {
        val photoFile = File(outputDirectory, "${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
        cameraView.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    exc.printStackTrace()
                    Toast.makeText(this@MainActivity, exc.message, Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    Toast.makeText(baseContext, photoFile.toString(), Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun startRecord() {
        if (cameraView.isRecording) return

        val videoFile = File(outputDirectory, "${System.currentTimeMillis()}.mp4")
        cameraView.startRecording(videoFile,
            ContextCompat.getMainExecutor(this),
            object : VideoCapture.OnVideoSavedCallback {
                override fun onVideoSaved(file: File) {
                    Toast.makeText(this@MainActivity, file.toString(), Toast.LENGTH_LONG).show()
                }

                override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                    Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
                }
            }
        )
    }

    private fun stopRecord() {
        if (!cameraView.isRecording) return

        cameraView.stopRecording()
    }

}