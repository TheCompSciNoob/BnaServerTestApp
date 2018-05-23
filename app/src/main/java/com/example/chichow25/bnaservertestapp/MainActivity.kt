package com.example.chichow25.bnaservertestapp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    private var socket: Socket? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //wire widgets
        val pref = getPreferences(Context.MODE_PRIVATE)
        ipAdress.setText(pref.getString("url", "default"))
        message.setText(pref.getString("message", "default"))
        connectButton.setOnClickListener {
            try {
                socket = IO.socket("http://${ipAdress.text}:8080")
                //socket = IO.socket("http://10.100.139.103:8080")
                configSocket()
                socket?.connect() ?: log("Socket is null")
            } catch (e: Exception) {
                log(e.message)
            }
        }
        disconnectButton.setOnClickListener {
            try {
                socket?.disconnect() ?: log("Socket is null")
            } catch (e: Exception) {
                log(e.message)
            }
        }
        emitButton.setOnClickListener {
            try {
                socket?.emit("TestEmit", json("message" to message.text.toString())) ?: log("Socket is null")
            } catch (e: Exception) {
                log(e.message)
            }
        }
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause()")
        getPreferences(Context.MODE_PRIVATE).edit().apply {
            putString("url", ipAdress.text.toString())
            putString("message", message.text.toString())
        }.apply()
    }

    private fun configSocket() {
        socket?.apply {
            on("NewPlayerJoined") {
                val json = it[0] as JSONObject
                log("New Player Joined, ID: ${json.getString("id")}")
            }
            on("SocketID") {
                val json = it[0] as JSONObject
                log("Joined Server, ID: ${json.getString("id")}")
            }
        }
    }

    fun log(message: String?) = eventLog.post {
        eventLog.append("\n\n$message")
    }

    fun json(vararg pairs: Pair<String, Any>) = JSONObject().apply {
        pairs.forEach {
            put(it.first, it.second)
        }
    }
}
