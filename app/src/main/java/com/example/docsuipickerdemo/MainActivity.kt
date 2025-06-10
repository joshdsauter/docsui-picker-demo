package com.example.docsuipickerdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var selectedFileText: TextView

    // Registering the file picker launcher
    private val pickFileLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            Log.d("FilePicker", "Selected file URI: $uri")
            selectedFileText.text = "Selected file:\n$uri"

            // Optional: persist URI permission if you need to access the file later
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
        } ?: run {
            selectedFileText.text = "No file selected"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pickFileButton = findViewById<Button>(R.id.pickFileButton)
        selectedFileText = findViewById(R.id.selectedFileText)

        pickFileButton.setOnClickListener {
            // Launch file picker for any file type
            pickFileLauncher.launch(arrayOf("*/*"))
        }
    }
}