package com.example.blucar

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class DeviceList : AppCompatActivity() {
    private var btnPaired: Button? = null
    private var devicelist: ListView? = null

    private var myBluetooth: BluetoothAdapter? = null

    companion object {
        const val REQUEST_ENABLE_BT = 1
        const val REQUEST_PERMISSION_BT = 2
        var address = "device_address"
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_device_list)
        setResult(Activity.RESULT_CANCELED)

        // btnPaired = findViewById(R.id.button)
        devicelist = findViewById(R.id.pairedDevices)

        myBluetooth = BluetoothAdapter.getDefaultAdapter()
        if (myBluetooth == null) {
            // Show a message that the device has no bluetooth adapter
            Toast.makeText(applicationContext, "Bluetooth Device Not Available", Toast.LENGTH_LONG)
                .show()
            // Finish app
            finish()
        } else {
            if (!myBluetooth!!.isEnabled) {
                // Ask to the user to turn the Bluetooth on
                val turnBTon = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                if (ActivityCompat.checkSelfPermission(
                        this,
                        Manifest.permission.BLUETOOTH_CONNECT
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                        REQUEST_PERMISSION_BT
                    )
                    return
                }
                startActivityForResult(turnBTon, REQUEST_ENABLE_BT)
            } else {
                checkBTPermissionsAndShowPairedDevices()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun checkBTPermissionsAndShowPairedDevices() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSION_BT
            )
        } else {
            pairedDevicesList()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_BT && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pairedDevicesList()
        } else {
            Toast.makeText(this, "Bluetooth permission is required", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_OK) {
            checkBTPermissionsAndShowPairedDevices()
        } else {
            Toast.makeText(this, "Bluetooth is required", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun pairedDevicesList() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_PERMISSION_BT
            )
            return
        }

        val pairedDevices = myBluetooth?.bondedDevices
        val list = ArrayList<String>()

        if (pairedDevices != null && pairedDevices.isNotEmpty()) {
            for (bt in pairedDevices) {
                list.add("${bt.name}\n${bt.address}")
            }
        } else {
            Toast.makeText(
                applicationContext,
                "No Paired Bluetooth Devices Found.",
                Toast.LENGTH_LONG
            ).show()
        }

        val adapter: ArrayAdapter<String> =
            ArrayAdapter(this, android.R.layout.simple_list_item_1, list)
        devicelist!!.adapter = adapter
        devicelist!!.setOnItemClickListener { _, v, _, _ ->
            val info = (v as TextView).text.toString()
            val macAddress = info.substring(info.length - 17)
            val returnIntent = Intent()
            returnIntent.putExtra(address, macAddress)
            setResult(Activity.RESULT_OK, returnIntent)
            finish()
        }
    }
}
