package com.jscj.qrcodescanner

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jscj.qrcodescanner.camera.CameraPreviewInitializer
import com.jscj.qrcodescanner.settings.SettingsUI
import com.jscj.qrcodescanner.ui.theme.JSCJQRCodeScannerTheme
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                onPermissionsGranted(Constants.CAMERA_PERMISSION_REQUEST_CODE, mutableListOf(android.Manifest.permission.CAMERA))
            } else {
                onPermissionsDenied(Constants.CAMERA_PERMISSION_REQUEST_CODE, mutableListOf(android.Manifest.permission.CAMERA))
            }
        }

        this.requestCameraPermission()

        setContent {
            JSCJQRCodeScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
    
    @Composable
    private fun AppNavigator() {
        val navController = rememberNavController()
        
        NavHost(navController = navController, startDestination = "cameraPreview") {
            composable("cameraPreview") {
                CameraPreviewInitializer(navController).CameraPreview()
            }
            composable("settings") {
                SettingsUI().SettingsScreen(onNavigateBack = { navController.popBackStack() })
            }
        }
    }

    private fun requestCameraPermission() {
        if (EasyPermissions.hasPermissions(this, android.Manifest.permission.CAMERA)) {
            onPermissionsGranted(Constants.CAMERA_PERMISSION_REQUEST_CODE, mutableListOf())
        } else {
            this.requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        }
    }
    

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        Toast.makeText(this, R.string.camera_permission_granted, Toast.LENGTH_SHORT).show()
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // This will navigate user to app settings.
            AppSettingsDialog.Builder(this).build().show()
        }

        Toast.makeText(this, R.string.camera_permission_denied, Toast.LENGTH_SHORT).show()
    }
}





