package com.example.bariume_server

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Telephony
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    companion object {
        private const val PERMISSION_REQUEST_CODE = 123
        private const val CLIENT_PHONE_NUMBER = "+989361720429" // Replace with actual client number
        private const val PASSWORD = "123"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkAndRequestPermissions()

        // Register SMS Receiver
        val smsReceiver = SMSReceiver()
        val filter = IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION)
        registerReceiver(smsReceiver, filter)
    }

    private fun checkAndRequestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.SEND_SMS
        )

        val permissionsToRequest = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toTypedArray(), PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                Toast.makeText(this, "All permissions granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Some permissions were denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    class SMSReceiver : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {
                val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent)
                for (message in messages) {
                    val sender = message.originatingAddress
                    val messageBody = message.messageBody
                    Log.d("SMSReceiver", "SMS received from: $sender, Message: $messageBody")

                    if (sender == CLIENT_PHONE_NUMBER && messageBody.trim() == PASSWORD) {
                        // Send verification SMS back to client
                        sendSMSToClient("You have been verified", context)
                    }
                }
            }
        }

        private fun sendSMSToClient(message: String, context: Context) {
            try {
                val smsManager = SmsManager.getDefault()
                smsManager.sendTextMessage(CLIENT_PHONE_NUMBER, null, message, null, null)
                Log.d("SMSReceiver", "Verification SMS sent to client: $message")
            } catch (e: Exception) {
                Log.e("SMSReceiver", "Failed to send verification SMS to client", e)
                Toast.makeText(context, "Failed to send verification SMS", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
