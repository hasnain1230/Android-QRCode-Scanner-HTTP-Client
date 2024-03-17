package com.jscj.qrcodescanner.camera

import android.content.Context
import android.graphics.PointF
import android.graphics.Rect
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.camera.core.CameraControl
import androidx.camera.core.CameraInfo
import androidx.camera.core.CameraSelector
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.zxing.BarcodeFormat
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.Result
import com.google.zxing.ResultPoint
import com.jscj.qrcodescanner.AboutAlertIconButton
import com.jscj.qrcodescanner.R
import com.jscj.qrcodescanner.qrcode.QRCodeViews
import com.jscj.qrcodescanner.qrcode.scanQRCode
import com.jscj.qrcodescanner.settings.SettingsViewModel
import com.jscj.qrcodescanner.util.Constants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.EnumMap

class CameraPreviewInitializer(
    private val navController: NavController, private val settingsViewModel: SettingsViewModel
) {
    private fun focusCamera(
        cameraControl: CameraControl?, focusPoint: PointF, previewView: PreviewView
    ) {
        cameraControl?.let { control ->
            val meteringPointFactory = previewView.meteringPointFactory
            val meteringPoint = meteringPointFactory.createPoint(focusPoint.x, focusPoint.y)
            val focusAction =
                FocusMeteringAction.Builder(meteringPoint, FocusMeteringAction.FLAG_AF)
                    .setAutoCancelDuration(
                        Constants.FOCUS_AUTO_CANCEL_SECONDS, java.util.concurrent.TimeUnit.SECONDS
                    ).build()
            control.startFocusAndMetering(focusAction)
        }
    }

    @Composable
    fun CameraPreview() {
        val context = LocalContext.current
        val scanner = setupScanner()
        val cameraControl = remember { mutableStateOf<CameraControl?>(null) }
        val cameraInfo = remember { mutableStateOf<CameraInfo?>(null) }
        val qrCodeBounds = remember { mutableStateOf<Rect?>(null) }
        val qrCodeData = remember { mutableStateOf<String?>(null) }
        val isFlashOn = remember { mutableStateOf(false) }
        val isScanning = remember { mutableStateOf(false) }
        val qrCodeViews = QRCodeViews()
        val titleText = remember { mutableStateOf<String?>(null) }
        val titleColor = remember { mutableStateOf<Color?>(null) }
        val bodyText = remember { mutableStateOf<String?>(null) }
        val success = remember { mutableStateOf(false) }
        val showPopup = remember { mutableStateOf(false) }
        val focusPoint = remember { mutableStateOf<PointF?>(null) }
        val showFocusCircle = remember { mutableStateOf(false) }

        val cameraBindData: Pair<PreviewView, MutableState<(() -> Unit)?>> =
            cameraSetup( // We bind the camera right away so that the camera data can be accessed by other components like the autofocus function
                context, scanner, cameraControl, cameraInfo, qrCodeBounds, qrCodeData, isScanning
            )

        BoxWithConstraints(modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures { offset ->
                    focusPoint.value = PointF(offset.x, offset.y)
                    showFocusCircle.value = true
                    focusCamera(cameraControl.value, focusPoint.value!!, cameraBindData.first)
                }
            }) {
            val scanBoxSize = maxWidth * Constants.SCAN_AREA_MULTIPLIER

            TranslucentBackground(
                modifier = Modifier
                    .matchParentSize()
                    .background(Color.Black.copy(alpha = Constants.TRANSLUCENT_BACKGROUND))
            )

            FlashButton(
                cameraInfo,
                cameraControl,
                isFlashOn,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(12.dp)
            )

            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    SavedLinksButton(modifier = Modifier.padding(4.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    SettingsButton(modifier = Modifier.padding(4.dp))
                }
            }

            // About Button
            AboutAlertIconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
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
                .border(2.dp, MaterialTheme.colorScheme.onPrimary, RoundedCornerShape(12.dp)))

            DrawBoundingBox(qrCodeBounds)

            LaunchedEffect(showFocusCircle.value) {
                if (showFocusCircle.value) {
                    delay(500)
                    showFocusCircle.value = false
                }
            }

            focusPoint.value?.let {
                val alpha by animateFloatAsState(
                    targetValue = if (showFocusCircle.value) 1f else 0f,
                    animationSpec = tween(durationMillis = 500),
                    label = "Float Animation"
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawCircle(
                        color = Color.White,
                        center = Offset(it.x, it.y),
                        radius = 50f,
                        style = Stroke(width = 2.dp.toPx()),
                        alpha = alpha
                    )
                }
            }


            qrCodeData.value?.let {
                val qrCodeDataMap = qrCodeViews.handleQRCodeData(
                    qrCodeData = it, settingsViewModel = settingsViewModel
                )
                titleText.value = qrCodeDataMap["titleText"]?.value as String
                bodyText.value = qrCodeDataMap["bodyText"]?.value as String
                showPopup.value = qrCodeDataMap["showPopup"]?.value as Boolean
                titleColor.value = qrCodeDataMap["titleColor"]?.value as Color
                success.value = qrCodeDataMap["success"]?.value as Boolean
            }

            if (showPopup.value) {
                qrCodeViews.ShowQRCodeDataPopup(
                    qrCodeData = qrCodeData,
                    context = context,
                    titleText = titleText.value!!,
                    titleColor = titleColor.value!!,
                    bodyText = bodyText.value!!,
                    showDialog = showPopup,
                    qrCodeBounds = qrCodeBounds,
                    rebindCamera = cameraBindData.second.value!!,
                    success = success.value
                )
            }
        }
    }

    @Composable
    private fun TranslucentBackground(modifier: Modifier) {
        Box(modifier = modifier)
    }

    @Composable
    private fun cameraSetup(
        context: Context,
        scanner: MultiFormatReader,
        cameraControl: MutableState<CameraControl?>,
        cameraInfo: MutableState<CameraInfo?>,
        qrCodeBounds: MutableState<Rect?>,
        qrCodeData: MutableState<String?>,
        isScanning: MutableState<Boolean>,
    ): Pair<PreviewView, MutableState<(() -> Unit)?>> {
        val bindCamera = remember { mutableStateOf<(() -> Unit)?>(null) }
        val previewView = remember { mutableStateOf(PreviewView(context)) }

        AndroidView(factory = { ctx ->
            val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
            cameraProviderFuture.addListener({
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.value.surfaceProvider)
                }

                val imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageRotationEnabled(false)
                    .build().also {
                    it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                        if (isScanning.value) {
                            imageProxy.close()
                            return@setAnalyzer
                        }

                        val result = scanQRCode(imageProxy, scanner)

                        processQRCodeResults(
                            result = result,
                            imageProxy = imageProxy,
                            isScanning = isScanning,
                            qrCodeData = qrCodeData,
                            qrCodeBounds = qrCodeBounds,
                            previewView = previewView.value,
                            cameraProvider = cameraProvider
                        )

                        imageProxy.close()
                    }
                }

                val cameraSelector =
                    CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                        .build()

                bindCamera.value = {
                    try {
                        cameraProvider.unbindAll()
                        val camera = cameraProvider.bindToLifecycle(
                            context as ComponentActivity, cameraSelector, preview, imageAnalysis
                        )
                        cameraControl.value = camera.cameraControl
                        cameraInfo.value = camera.cameraInfo
                    } catch (exc: Exception) {
                        Toast.makeText(ctx, R.string.camera_init_error, Toast.LENGTH_SHORT).show()
                    }
                }

                bindCamera.value?.invoke()
            }, ContextCompat.getMainExecutor(ctx))
            previewView.value

        }, modifier = Modifier.fillMaxSize(), update = { view ->
            previewView.value = view
        })

        return Pair(previewView.value, bindCamera)
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
                        cameraControl.value?.let {
                            toggleFlash(
                                cameraControl = it, isFlashOn = isFlashOn.value
                            )
                        }
                    }, modifier = modifier
                ) {
                    Icon(
                        painter = painterResource(id = isFlashOn.value.let { if (it) R.drawable.twotone_flashlight_on_24 else R.drawable.twotone_flashlight_off_24 }),
                        contentDescription = LocalContext.current.getString(R.string.toggle_flash),
                        tint = Color.White
                    )
                }
            }
        }
    }

    @Composable
    private fun SavedLinksButton(modifier: Modifier) {
        IconButton(
            onClick = {
                navController.navigate("savedLinks")
            }, modifier = modifier
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_bookmarks_24),
                contentDescription = LocalContext.current.getString(R.string.saved_links),
                tint = Color.White
            )
        }
    }

    @Composable
    private fun SettingsButton(modifier: Modifier) {

        IconButton(
            onClick = {
                navController.navigate("settings")
            }, modifier = modifier
        ) {
            Icon(
                painter = painterResource(id = R.drawable.twotone_settings_24),
                contentDescription = LocalContext.current.getString(R.string.settings),
                tint = Color.White
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
                    size = Size(
                        bounds.width().toFloat(), bounds.height().toFloat()
                    ), // Use the actual width of the bounding box
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

    private fun getBoundingBox(
        resultPoints: Array<ResultPoint>?, imageProxy: ImageProxy, previewView: PreviewView
    ): Rect? {
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

    private fun processQRCodeResults(
        result: Result?,
        imageProxy: ImageProxy,
        isScanning: MutableState<Boolean>,
        qrCodeData: MutableState<String?>,
        qrCodeBounds: MutableState<Rect?>,
        previewView: PreviewView,
        cameraProvider: ProcessCameraProvider
    ) {
        if (result != null) {

            isScanning.value = true

            println("QR Code Data: ${result.text}")

            qrCodeData.value = result.text
            qrCodeBounds.value =
                getBoundingBox(result.resultPoints, imageProxy, previewView = previewView)

            cameraProvider.unbindAll()

            CoroutineScope(Dispatchers.Default).launch {
                delay(Constants.QR_CODE_SCAN_DELAY)
                isScanning.value = false
            }


        } else {
            qrCodeBounds.value = null
        }
    }
}
