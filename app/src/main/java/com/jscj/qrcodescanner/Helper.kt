package com.jscj.qrcodescanner

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri

class Helper {
    companion object {
        // Function to open URL
        fun openUrl(context: Context, url: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            context.startActivity(intent)
        }

        fun playSound(context: Context, resId: Int) {
            val mediaPlayer = MediaPlayer.create(context, resId)
            mediaPlayer.start()
        }
    }
}