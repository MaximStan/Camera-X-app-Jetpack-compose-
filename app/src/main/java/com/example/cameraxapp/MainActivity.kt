package com.example.cameraxapp

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import coil.compose.rememberImagePainter
import com.example.cameraxapp.ui.theme.CameraXAppTheme
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class MainActivity : ComponentActivity() {

    private lateinit var outputDirectory: File              // will  store actual photos
    private lateinit var cameraExecutor: ExecutorService   // specifically designated for camera

    private var shouldShowCamera by mutableStateOf(false)

    private lateinit var photoUri: Uri
    private var shouldShowPhoto by mutableStateOf(false)

    private  val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ){ isGranted ->
        if (isGranted){
            Log.i("camera X", "Camera permission granted")
            shouldShowCamera = true
        }else{
            Log.i("camera X", "Camera permission denied")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            if(shouldShowCamera){
                CameraView(
                    outputDirectory = outputDirectory,
                    executor = cameraExecutor,
                    onImageCaptured = ::handleImageCapture,
                    onError = {Log.e("camera X", "Camera view error:", it)})
            }

            if(shouldShowPhoto){
                Image(
                    painter = rememberImagePainter(photoUri), // coil provides
                    contentDescription = "taken photo",
                    modifier = Modifier.fillMaxWidth()
                )
            }
            requestCameraPermission()

            outputDirectory = getOutputDirectory()
            cameraExecutor = Executors.newSingleThreadExecutor()

        }
    }

    private fun requestCameraPermission() {
        when{
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("camera X", "Camera permission previously granted")
                shouldShowCamera = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) ->  Log.i("camera X", " Show camera permission dialog window")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)

        }
    }

    private fun handleImageCapture(uri: Uri) {
        Log.i("camera X", "Image captured: $uri")
        shouldShowCamera = false

        photoUri = uri
        shouldShowPhoto = true
    }

    private fun getOutputDirectory(): File {
        val mediaDirectory = externalMediaDirs.firstOrNull()?.let{
            File(it, resources.getString(R.string.app_name)).apply{mkdirs()}
        }
        return if (mediaDirectory !=null && mediaDirectory.exists()) mediaDirectory else filesDir
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }



}
