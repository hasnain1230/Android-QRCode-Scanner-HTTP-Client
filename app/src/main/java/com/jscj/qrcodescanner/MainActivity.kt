package com.jscj.qrcodescanner

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jscj.qrcodescanner.camera.CameraPreviewInitializer
import com.jscj.qrcodescanner.savedlinks.SavedLinksUI
import com.jscj.qrcodescanner.savedlinks.SavedLinksViewModel
import com.jscj.qrcodescanner.settings.SettingsEnums
import com.jscj.qrcodescanner.settings.SettingsUI
import com.jscj.qrcodescanner.settings.SettingsViewModel
import com.jscj.qrcodescanner.ui.theme.JSCJQRCodeScannerTheme
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions

class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestPermissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
                if (isGranted) {
                    onPermissionsGranted(
                        Constants.CAMERA_PERMISSION_REQUEST_CODE,
                        mutableListOf(android.Manifest.permission.CAMERA)
                    )
                } else {
                    onPermissionsDenied(
                        Constants.CAMERA_PERMISSION_REQUEST_CODE,
                        mutableListOf(android.Manifest.permission.CAMERA)
                    )
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
        val context: Context = LocalContext.current

        val savedLinksViewModel: SavedLinksViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SavedLinksViewModel(context) as T
                }
            }
        )

        val settingsViewModel: SettingsViewModel = viewModel(
            factory = object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return SettingsViewModel(context, savedLinksViewModel) as T
                }
            }
        )


        NavHost(navController = navController, startDestination = "cameraPreview") {
            composable("cameraPreview") {
                CameraPreviewInitializer(navController, settingsViewModel).CameraPreview()
            }

            composable("settings") {
                SettingsUI(settingsViewModel).SettingsScreen(onNavigateBack = { navController.popBackStack() })
                BackHandler(enabled = navController.currentDestination?.route == "settings") {
                    if (settingsViewModel.getCurrentMode().value == SettingsEnums.HTTP_MODE && settingsViewModel.getUrl().value.isEmpty()) {
                        // Tell the SettingsUI to show the dialog
                        settingsViewModel.showDialog()
                    } else {
                        navController.navigateUp()
                    }
                }
            }

            composable("savedLinks") {
                SavedLinksUI(savedLinksViewModel).SavedLinksScreen(onNavigateBack = { navController.popBackStack() })
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





