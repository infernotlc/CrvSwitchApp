package com.example.blucar

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.Toolbar


open class MainActivity : BluetoothActivity() {

    private var connectedIcon: MenuItem? = null
    private var disconnectedIcon: MenuItem? = null
    private var progress: ProgressDialog? = null
    private var mainLayout: LinearLayout? = null
    private var bluetooth: Bluetooth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        addLayoutChannels()
        bluetooth = Bluetooth(this)
    }

    private fun addLayoutChannels() {
        mainLayout = findViewById(R.id.main_layout)
        val channels = listOf(
            "1" to "A", "2" to "B", "3" to "C", "4" to "D",
            "5" to "E", "6" to "F", "7" to "G", "8" to "H",
            "9" to "I", "10" to "J", "11" to "K", "12" to "L",
            "13" to "M", "14" to "N", "15" to "O", "16" to "P"
        )
        for ((channelName, letter) in channels) {
            addLayoutChannel(channelName, letter)
        }
    }

    private fun addLayoutChannel(channelName: String, letter: String) {
        val inflater: LayoutInflater = layoutInflater
        val channel: View = inflater.inflate(R.layout.include_channel, mainLayout, false)
        val buttons = listOf(R.id.b0, R.id.b1, R.id.b2, R.id.b3, R.id.b4)
        for (i in buttons.indices) {
            initButton(buttons[i], "$letter$i", "C$channelName $letter$i", channel)
        }
        mainLayout?.addView(channel)
    }

    private fun initButton(id: Int, tag: String, text: String, channel: View) {
        val button = channel.findViewById<Button>(id)
        button.tag = tag
        button.text = text
    }

    fun onButtonClick(view: View) {
        val data = view.tag as String
        bluetooth?.btSendData(data)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        connectedIcon = menu.findItem(R.id.connected).apply { isVisible = false }
        disconnectedIcon = menu.findItem(R.id.disconnected)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.connectBluetooth -> {
                if (bluetooth?.isBtConnected == false) {
                    val intent = Intent(this, DeviceList::class.java)
                    startActivityForResult(intent, 1)
                } else {
                    msg("Bluetooth is already connected.")
                }
                true
            }
            R.id.disconnectBluetooth -> {
                bluetooth?.disconnect()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1) { // deviceList
            if (resultCode == Activity.RESULT_OK) {
                val address = data?.getStringExtra(DeviceList.address)
                if (address != null) {
                    bluetooth?.setAddress(address)
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                msg("Canceled.")
            }
        } else {
            msg("Invalid request code")
        }
    }

    override fun onDestroy() {
        bluetooth?.disconnect()
        try {
            Thread.sleep(200)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        super.onDestroy()
    }

    override fun msg(s: String?) {
        Toast.makeText(applicationContext, s, Toast.LENGTH_LONG).show()
    }

    override fun setProgress(show: ProgressDialog?) {
        this.progress = show
    }

    override fun dismissProgress() {
        progress?.dismiss()
    }

    override fun connected(notify: Boolean) {
        if (notify) msg("Connected.")
        connectedIcon?.isVisible = true
        disconnectedIcon?.isVisible = false
    }

    override fun disconnected(notify: Boolean) {
        if (notify) msg("Disconnected.")
        disconnectedIcon?.isVisible = true
        connectedIcon?.isVisible = false
    }
}
