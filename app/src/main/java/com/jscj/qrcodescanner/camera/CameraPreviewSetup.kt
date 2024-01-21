package com.jscj.qrcodescanner.camera

import android.content.Context
import android.graphics.Rect
import android.widget.Toast
import androidx.activity.ComponentActivity
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.twotone.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.ResultPoint
import com.jscj.qrcodescanner.Constants
import com.jscj.qrcodescanner.R
import com.jscj.qrcodescanner.qrcode.QRCodeViews
import com.jscj.qrcodescanner.qrcode.scanQRCode
import java.util.EnumMap

class CameraPreviewInitializer(navController: NavController) {
    private val navController = navController

    @Composable
    fun CameraPreview() {
        val context = LocalContext.current
        val scanner = setupScanner()
        val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
        val cameraInfo = remember { mutableStateOf<CameraInfo?>(null) }
        val qrCodeBounds = remember { mutableStateOf<Rect?>(null) }
        val qrCodeData = remember { mutableStateOf<String?>(null) }

        val isFlashOn = remember { mutableStateOf(false) }

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val scanBoxSize = maxWidth * Constants.SCAN_AREA_MULTIPLIER

            CameraSetup(context, scanner, cameraControl, cameraInfo, qrCodeBounds, qrCodeData)

            TranslucentBackground(
                modifier = Modifier.matchParentSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            )

            FlashButton(cameraInfo, cameraControl, isFlashOn,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )


            SettingsButton(modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(12.dp)
            )

            ScanningAreaBox(modifier = Modifier
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

            DrawBoundingBox(qrCodeBounds)

            qrCodeData.value?.let {
                QRCodeViews().ShowQRCodeDataPopup(qrCodeData = it, context = context)
            }
        }
    }

    @Composable
    private fun TranslucentBackground(modifier: Modifier) {
        Box(modifier = modifier)
    }

    @Composable
    private fun CameraSetup( // Will setup the camera and start scanning for QR codes in the preview frame
        context: Context,
        scanner: MultiFormatReader,
        cameraControl: MutableState<CameraControl?>,
        cameraInfo: MutableState<CameraInfo?>,
        qrCodeBounds: MutableState<Rect?>,
        qrCodeData: MutableState<String?>
    ) {
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
                                    qrCodeBounds.value = getBoundingBox(result.resultPoints, imageProxy, previewView = previewView)
                                } else {
                                    qrCodeBounds.value = null
                                }
                            }
                        }

                    val cameraSelector = CameraSelector.Builder()
                        .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

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
                        Toast.makeText(ctx, R.string.camera_init_error, Toast.LENGTH_SHORT).show()
                    }
                }, ContextCompat.getMainExecutor(ctx))
                previewView
            },
            modifier = Modifier.fillMaxSize()
        )
    }

    @Composable
    private fun FlashButton(
        cameraInfo: MutableState<CameraInfo?>,
        cameraControl: MutableState<CameraControl?>,
        isFlashOn: MutableState<Boolean>,
        modifier: Modifier
    ) {
        cameraInfo.value?.let {
            if (it.hasFlashUnit()) {
                // Show flash button
                IconButton(
                    onClick = {
                        isFlashOn.value = !isFlashOn.value
                        cameraControl.value?.let { toggleFlash(cameraControl = it, isFlashOn = isFlashOn.value) }
                    },
                    modifier = modifier
                ) {
                    Icon(
                        painter = painterResource(id = isFlashOn.value
                            .let { if (it) R.drawable.twotone_flashlight_on_24 else R.drawable.twotone_flashlight_off_24 }),
                        contentDescription = R.string.toggle_flash.toString()
                    )
                }
            }
        }
    }

    @Composable
    private fun SettingsButton(modifier: Modifier) {

        IconButton(
            onClick = {
                navController.navigate("settings")
            },
            modifier = modifier
        ) {
            Icon(
                imageVector = Icons.TwoTone.Settings,
                contentDescription = R.string.settings.toString()
            )
        }
    }

    @Composable
    private fun ScanningAreaBox(modifier: Modifier) {
        Box(
            modifier = modifier
        )
    }

    @Composable
    private fun DrawBoundingBox(qrCodeBounds: MutableState<Rect?>) {
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
    }

    private fun setupScanner(): MultiFormatReader {
        return MultiFormatReader().apply {
            val hints: MutableMap<DecodeHintType, Any> = EnumMap(DecodeHintType::class.java)
            hints[DecodeHintType.POSSIBLE_FORMATS] = listOf(BarcodeFormat.QR_CODE)
            setHints(hints)
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
            ResultPoint(rotatedX, rotatedY)
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
}
