package com.jscj.qrcodescanner.util

import com.jscj.qrcodescanner.R

class Constants {
    companion object {
        val QR_CODE_BEEP: Int = com.google.zxing.client.android.R.raw.zxing_beep
        val ERROR_BEEP: Int = R.raw.error_sound
        const val TRANSLUCENT_BACKGROUND: Float = 0.6f
        const val FOCUS_AUTO_CANCEL_SECONDS: Long = 1L
        const val SCAN_AREA_MULTIPLIER: Float = 0.6f
        const val CAMERA_PERMISSION_REQUEST_CODE: Int = 100
        const val QR_CODE_SCAN_DELAY: Long = 2000L
    }
}