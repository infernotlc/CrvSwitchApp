package com.example.blucar

import android.Manifest
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import java.io.IOException
import java.util.*

class Bluetooth(private val bluetoothActivity: BluetoothActivity) {
    private var myBluetooth: BluetoothAdapter? = null
    private var btSocket: BluetoothSocket? = null

    var isBtConnected: Boolean = false
        private set
    private var address: String? = null

    private var sharedPreferences: SharedPreferences =
        PreferenceManager.getDefaultSharedPreferences(bluetoothActivity)
    private var editor: SharedPreferences.Editor = sharedPreferences.edit()

    // The BroadcastReceiver that listens for bluetooth broadcasts
    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_ACL_DISCONNECTED) {
                disconnected(true)
            }
        }
    }

    init {
        val disconnectedFilter = IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        bluetoothActivity.registerReceiver(mReceiver, disconnectedFilter)

        address = sharedPreferences.getString("address", "undefined")
        if (address != "undefined") ConnectBT().execute()
    }

    fun setAddress(address: String?) {
        this.address = address
        editor.putString("address", address).apply()
        ConnectBT().execute()
    }

    fun btSendData(s: String) {
        val lineBreak = "\n"
        if (isBtConnected) {
            try {
                btSocket!!.outputStream.write((s + lineBreak).toByteArray())
            } catch (e: IOException) {
                e.printStackTrace()
            }
        } else {
            bluetoothActivity.msg("Bluetooth is not connected.")
        }
    }

    fun disconnect() {
        bluetoothActivity.unregisterReceiver(mReceiver)
        if (btSocket != null) {
            try {
                btSocket!!.inputStream.close()
                btSocket!!.outputStream.close()
                btSocket!!.close() // Close connection
                disconnected(false)
            } catch (e: IOException) {
                bluetoothActivity.msg("Error.")
            }
        } else {
            bluetoothActivity.msg("Bluetooth is not connected.")
            disconnected(false)
        }
    }

    private fun disconnected(notify: Boolean) {
        isBtConnected = false
        btSocket = null
        bluetoothActivity.disconnected(notify)
    }

    @SuppressLint("StaticFieldLeak")
    private inner class ConnectBT : AsyncTask<Void?, Void?, Boolean>() {
        private var connectSuccess = true

        @Deprecated("Deprecated in Java")
        override fun onPreExecute() {
            super.onPreExecute()
            val progress =
                ProgressDialog.show(bluetoothActivity, "Connecting...", "Please wait.")
            bluetoothActivity.setProgress(progress)
        }

        @RequiresApi(Build.VERSION_CODES.S)
        @Deprecated("Deprecated in Java")
        override fun doInBackground(vararg params: Void?): Boolean {
            try {
                if (btSocket == null || !isBtConnected) {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter()
                    if (myBluetooth == null) {
                        connectSuccess = false
                        return connectSuccess
                    }

                    val btDevice = myBluetooth!!.getRemoteDevice(address)

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        // For API 31 and above
                        if (ActivityCompat.checkSelfPermission(
                                bluetoothActivity,
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // Request Bluetooth permissions if not granted
                            ActivityCompat.requestPermissions(
                                bluetoothActivity,
                                arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                                1
                            )
                            connectSuccess = false
                            return connectSuccess
                        }
                    } else {
                        // For API 30 and below
                        if (ActivityCompat.checkSelfPermission(
                                bluetoothActivity,
                                Manifest.permission.BLUETOOTH
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // Request Bluetooth permissions if not granted
                            ActivityCompat.requestPermissions(
                                bluetoothActivity,
                                arrayOf(Manifest.permission.BLUETOOTH),
                                1
                            )
                            connectSuccess = false
                            return connectSuccess
                        }
                    }

                    btSocket = btDevice.createInsecureRfcommSocketToServiceRecord(myUUID)
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery()
                    btSocket!!.connect()
                }
            } catch (e: IOException) {
                connectSuccess = false
            }
            return connectSuccess
        }


        @Deprecated("Deprecated in Java")
        override fun onPostExecute(result: Boolean) {
            super.onPostExecute(result)
            if (!result) {
                bluetoothActivity.msg("Connection Failed.")
            } else {
                isBtConnected = true
                bluetoothActivity.connected(true)
                try {
                    btSocket!!.outputStream.write("c\n".toByteArray())
                } catch (e: IOException) {
                    e.printStackTrace()
                } catch (e: NullPointerException) {
                    disconnect()
                    ConnectBT().execute()
                }
            }
            bluetoothActivity.dismissProgress()
        }
    }

    companion object {
        val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    }
}
