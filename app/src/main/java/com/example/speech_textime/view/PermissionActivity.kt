package com.example.speech_textime.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.speech_textime.R

class PermissionActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                Toast.makeText(this, "Microphone permission granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission denied. Enable it in Settings.", Toast.LENGTH_LONG).show()
                startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = android.net.Uri.fromParts("package", packageName, null)
                })
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_permission)

        val btn = findViewById<Button>(R.id.btnGrantPermission)
        btn.setOnClickListener {
            when {
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED -> {
                    Toast.makeText(this, "Permission already granted", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }
}
