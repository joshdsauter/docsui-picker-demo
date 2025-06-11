package com.example.docsuipickerdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
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

            // Get and display human-readable file name
            val fileName = getFileNameFromUri(this, uri)
            val mimeType = contentResolver.getType(uri)
            selectedFileText.text = "Selected file:\n${fileName ?: "Unknown"}\nType: ${mimeType ?: "Unknown"}"

            // Optional: persist URI permission
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

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index != -1) {
                    name = it.getString(index)
                }
            }
        }
        return name
    }

}