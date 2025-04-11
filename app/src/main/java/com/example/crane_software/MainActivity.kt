package com.example.crane_software

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothSocket
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

class MainActivity : Activity() {

    private val hc05MACAddress = "98:D3:41:F6:EA:F7" // Replace with your MAC
    private val REQUEST_BLUETOOTH_PERMISSION = 1
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        // Request permission (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.BLUETOOTH_CONNECT),
                REQUEST_BLUETOOTH_PERMISSION
            )
        }

        // UI elements
        statusText = findViewById(R.id.statusText)
        val connectBtn = findViewById<Button>(R.id.connectBtn)

        // Remote Buttons
        val remoteButtons = mapOf(
            R.id.upBtn to "u",
            R.id.downBtn to "d",
            R.id.leftBtn to "l",
            R.id.rightBtn to "r",
            R.id.clockwiseBtn to "c",
            R.id.anticlockwiseBtn to "a"
        )

        // Numeric Inputs
        val numericButtons = mapOf(
            R.id.sendUp to R.id.inputUp,
            R.id.sendDown to R.id.inputDown,
            R.id.sendLeft to R.id.inputLeft,
            R.id.sendRight to R.id.inputRight,
            R.id.sendCW to R.id.inputCW,
            R.id.sendACW to R.id.inputACW
        )

        connectBtn.setOnClickListener {
            connectToHC05()
        }

        remoteButtons.forEach { (btnId, char) ->
            val btn = findViewById<Button>(btnId)
            btn.setOnTouchListener { _, event ->
                when (event.action) {
                    android.view.MotionEvent.ACTION_DOWN -> sendData(char)
                    android.view.MotionEvent.ACTION_UP -> sendData("0")
                }
                true
            }
        }

        numericButtons.forEach { (sendBtnId, inputId) ->
            val btn = findViewById<Button>(sendBtnId)
            val input = findViewById<EditText>(inputId)
            btn.setOnClickListener {
                val value = input.text.toString()
                if (value.isNotEmpty()) {
                    val prefix = when (inputId) {
                        R.id.inputUp -> "u"
                        R.id.inputDown -> "d"
                        R.id.inputLeft -> "l"
                        R.id.inputRight -> "r"
                        R.id.inputCW -> "c"
                        R.id.inputACW -> "a"
                        else -> ""
                    }
                    sendData(prefix + value)
                }
            }
        }
    }

    private fun connectToHC05() {
        try {
            val device: BluetoothDevice = bluetoothAdapter!!.getRemoteDevice(hc05MACAddress)
            bluetoothSocket = device.createRfcommSocketToServiceRecord(
                UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            )
            bluetoothSocket!!.connect()
            outputStream = bluetoothSocket!!.outputStream
            inputStream = bluetoothSocket!!.inputStream
            statusText.text = "Status: Connected to HC-05"
        } catch (e: IOException) {
            statusText.text = "Status: Connection Failed"
            e.printStackTrace()
        }
    }

    private fun sendData(data: String) {
        try {
            outputStream?.write(data.toByteArray())
        } catch (e: IOException) {
            Toast.makeText(this, "Failed to send data", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_BLUETOOTH_PERMISSION &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(this, "Bluetooth permission granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth permission denied", Toast.LENGTH_SHORT).show()
        }
    }
}
