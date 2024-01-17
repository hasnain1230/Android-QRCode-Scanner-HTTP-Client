package com.jscj.qrcodescanner

import android.graphics.Rect
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.zxing.BarcodeFormat
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.PlanarYUVLuminanceSource
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.google.zxing.common.HybridBinarizer
import com.jscj.qrcodescanner.ui.theme.JSCJQRCodeScannerTheme
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import java.util.EnumMap

class MainActivity : ComponentActivity(), EasyPermissions.PermissionCallbacks {

    companion object {
        private const val CAMERA_PERMISSION = android.Manifest.permission.CAMERA
        private const val REQUEST_CODE = 567
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        this.requestCameraPermission()

        setContent {
            JSCJQRCodeScannerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraPreview() // Camera preview composable which will be placed on top of the background
                }
            }
        }
    }

    private fun requestCameraPermission() {
        if (EasyPermissions.hasPermissions(this, CAMERA_PERMISSION)) {
            return
        } else {
            // Ask for one permission
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to your camera for scanning QR codes.",
                REQUEST_CODE,
                CAMERA_PERMISSION
            )
        }
    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        // Do nothing.
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this, perms)) {
            // This will navigate user to app settings.
            AppSettingsDialog.Builder(this).build().show()
        }
    }
}

@Composable
fun CameraPreview() {
    val context = LocalContext.current
    val scanner = MultiFormatReader().apply {
        val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
        hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
        setHints(hints)
    }

    val qrCodeBounds = remember { mutableStateOf<Rect?>(null) }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val screenWidth = maxWidth
        val scanBoxSize = screenWidth * 0.6f // adjust the size of the scanning area here

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    val imageAnalysis = ImageAnalysis.Builder()
                        .build()
                        .also {
                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                val result = scanQRCode(imageProxy, scanner)
                                imageProxy.close()
                                if (result != null) {
                                    println("QR Code found: ${result.text}")
                                    println("Image Proxy Width: ${imageProxy.width}, Height: ${imageProxy.height}")
                                    qrCodeBounds.value = getBoundingBox(result.resultPoints, imageProxy.width, imageProxy.height, previewView.width, previewView.height)
                                } else {
                                    qrCodeBounds.value = null
                                }
                            }
                        }

                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            context as ComponentActivity,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageAnalysis
                        )
                    } catch (exc: Exception) {
                        // Handle exceptions
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )

        Box(modifier = Modifier
            .matchParentSize()
            .background(Color.Black.copy(alpha = 0.8f))
        )

        // Scanning area box with a clear cutout
        Box(
            modifier = Modifier
                .size(scanBoxSize)
                .align(Alignment.Center)
                .drawBehind {
                    // Draw a rounded clear rectangle to create a cutout effect
                    drawRoundRect(
                        color = Color.Transparent,
                        topLeft = Offset(0f, 0f),
                        size = Size(size.width, size.height),
                        cornerRadius = CornerRadius(x = 12.dp.toPx(), y = 12.dp.toPx()),
                        blendMode = BlendMode.Clear
                    )
                }
                .border(2.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(12.dp))
        )

        qrCodeBounds.value?.let { bounds ->
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawRect(
                    color = Color.Red,
                    topLeft = Offset(bounds.left.toFloat(), bounds.top.toFloat()),
                    size = Size(bounds.width().toFloat(), bounds.height().toFloat()),
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }
    }
}

private fun getBoundingBox(resultPoints: Array<ResultPoint>?, imageWidth: Int, imageHeight: Int, previewWidth: Int, previewHeight: Int): Rect? {
    if (resultPoints == null || resultPoints.size != 4) {
        return null
    }

    // Calculate scale factors
    val scaleX = previewWidth.toFloat() / imageWidth
    val scaleY = previewHeight.toFloat() / imageHeight

    // Apply scale factors to the coordinates
    val left = (resultPoints[0].x * scaleX).toInt()
    val top = (resultPoints[0].y * scaleY).toInt()
    val right = (resultPoints[2].x * scaleX).toInt()
    val bottom = (resultPoints[2].y * scaleY).toInt()

    return Rect(left, top, right, bottom)
}


private fun scanQRCode(imageProxy: ImageProxy, scanner: MultiFormatReader): Result? {
    val data = imageProxy.planes[0].buffer.let { buffer ->
        val data = ByteArray(buffer.capacity())
        buffer.get(data)
        buffer.clear()
        data
    }
    val source = PlanarYUVLuminanceSource(
        data,
        imageProxy.width,
        imageProxy.height,
        0,
        0,
        imageProxy.width,
        imageProxy.height,
        false
    )
    val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
    return try {
        scanner.decodeWithState(binaryBitmap)
    } catch (e: Exception) {
        null // QR Code not found
    }
}



