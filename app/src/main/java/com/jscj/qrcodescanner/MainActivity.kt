package com.jscj.qrcodescanner

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.AbsoluteRoundedCornerShape
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
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
        Toast.makeText(this, "Camera permission granted", Toast.LENGTH_SHORT).show()
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

    // Check if flash is on or off
    val isFlashOn = remember { mutableStateOf(false) }
    val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
    val cameraInfo = remember { mutableStateOf<CameraInfo?>(null) }
    val qrCodeBounds = remember { mutableStateOf<Rect?>(null) }
    val qrCodeData = remember { mutableStateOf<String?>(null) }

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
                                    // Display a popup with the scanned QR data
                                    qrCodeData.value = result.text
                                    qrCodeBounds.value = getBoundingBox(result.resultPoints, imageProxy, previewView)
                                } else {
                                    qrCodeBounds.value = null
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            context as ComponentActivity,
                            cameraSelector,
                            preview,
                            imageAnalysis
                        )
                        cameraControl.value = camera.cameraControl
                        cameraInfo.value = camera.cameraInfo
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

        IconButton(
            onClick = {
                isFlashOn.value = !isFlashOn.value
                cameraControl.value?.let { toggleFlash(cameraControl = it, isFlashOn = isFlashOn.value) }
            },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            Icon(
                painter = painterResource(id = isFlashOn.value
                    .let { if (it) R.drawable.twotone_flashlight_on_24 else R.drawable.twotone_flashlight_off_24 }),
                contentDescription = "Toggle Flash"
            )
        }

        IconButton(
            onClick = {
                println("Settings clicked")
            },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.TwoTone.Settings,
                contentDescription = "Settings"
            )
        }

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
                    size = Size(bounds.width().toFloat(), bounds.height().toFloat()), // Use the actual width of the bounding box
                    style = Stroke(width = 3.dp.toPx())
                )
            }
        }


        qrCodeData.value?.let {
            ShowQRCodeDataPopup(qrCodeData = it, context = context)
        }
    }
}

@Composable
fun ShowQRCodeDataPopup(qrCodeData: String, context: Context) {
    val showDialog = remember { mutableStateOf(true) }

    // Function to open URL in browser
    fun openUrlInBrowser(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        context.startActivity(intent)
    }

    // Check if the QR code data is a URL
    val isUrl = Patterns.WEB_URL.matcher(qrCodeData).matches()

    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text("Scanned QR Code") },
            text = {
                if (isUrl) {
                    val colorHex = Color(android.graphics.Color.parseColor("#2196F3"))
                    val annotatedText = buildAnnotatedString {
                        withStyle(style = SpanStyle(color = colorHex, textDecoration = TextDecoration.Underline)) {
                            append(qrCodeData)
                        }
                    }
                    ClickableText(
                        text = annotatedText,
                        onClick = { openUrlInBrowser(qrCodeData) },
                    )
                } else {
                    Text(qrCodeData)
                }
            },
            confirmButton = {
                Button(onClick = { showDialog.value = false }) {
                    Text("Okay")
                }
            },
            shape = RoundedCornerShape(size = 12.dp),
            modifier = Modifier
                .padding(6.dp)
                .background(MaterialTheme.colorScheme.surface, AbsoluteRoundedCornerShape(12.dp))
        )
    }
}



private fun toggleFlash(cameraControl: CameraControl, isFlashOn: Boolean) {
    cameraControl.enableTorch(isFlashOn)
}

private fun getBoundingBox(resultPoints: Array<ResultPoint>?, imageProxy: ImageProxy, previewView: PreviewView): Rect? {
    // Check if resultPoints is null or has less than 4 points
    if (resultPoints == null || resultPoints.size < 4) {
        return null
    }

    // Rotate the points by 90 degrees clockwise
    val rotatedPoints = resultPoints.map {
        // Rotating each point 90 degrees clockwise
        val rotatedX = imageProxy.height - it.y
        val rotatedY = it.x
        ResultPoint(rotatedX.toFloat(), rotatedY.toFloat())
    }

    // Scale the rotated points to the previewView size
    val scaleX = previewView.width.toFloat() / imageProxy.height.toFloat()
    val scaleY = previewView.height.toFloat() / imageProxy.width.toFloat()

    val scaledPoints = rotatedPoints.map {
        ResultPoint(it.x * scaleX, it.y * scaleY)
    }

    return Rect(
        scaledPoints[1].x.toInt(),
        scaledPoints[1].y.toInt(),
        scaledPoints[2].x.toInt(),
        scaledPoints[3].y.toInt()
    )
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



