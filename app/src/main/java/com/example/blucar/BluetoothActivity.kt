package com.example.blucar

import android.app.ProgressDialog
import androidx.appcompat.app.AppCompatActivity

     abstract class BluetoothActivity : AppCompatActivity() {
        abstract fun connected(notify: Boolean)

        abstract fun disconnected(notify: Boolean)

        abstract fun msg(s: String?)

        abstract fun setProgress(show: ProgressDialog?)

        abstract fun dismissProgress()
    }

