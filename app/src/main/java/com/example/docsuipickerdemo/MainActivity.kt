package com.example.docsuipickerdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var selectedFileText: TextView
    private lateinit var multiSelectSwitch: SwitchCompat
    private lateinit var mimeTypeSpinner: Spinner

    private val pickSingleFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val fileName = getFileNameFromUri(this, it)
                val mimeType = contentResolver.getType(it)
                selectedFileText.text = "Selected file:\n${fileName ?: "Unknown"}\nType: ${mimeType ?: "Unknown"}"
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } ?: run {
                selectedFileText.text = "No file selected"
            }
        }

    private val pickMultipleFilesLauncher =
        registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris: List<Uri> ->
            if (uris.isNotEmpty()) {
                for (uri in uris) {
                    try {
                        contentResolver.takePersistableUriPermission(
                            uri,
                            Intent.FLAG_GRANT_READ_URI_PERMISSION
                        )
                    } catch (e: SecurityException) {
                        Log.w("FilePicker", "Failed to persist permission for $uri: ${e.message}")
                    }
                }

                val fileDescriptions = uris.joinToString("\n\n") { uri ->
                    val fileName = getFileNameFromUri(this, uri)
                    val mimeType = contentResolver.getType(uri)
                    "File: ${fileName ?: "Unknown"}\nType: ${mimeType ?: "Unknown"}"
                }
                selectedFileText.text = "Selected files:\n\n$fileDescriptions"
            } else {
                selectedFileText.text = "No files selected"
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pickFileButton = findViewById<Button>(R.id.pickFileButton)
        selectedFileText = findViewById(R.id.selectedFileText)
        multiSelectSwitch = findViewById(R.id.multiSelectSwitch)
        mimeTypeSpinner = findViewById(R.id.mimeTypeSpinner)


        pickFileButton.setOnClickListener {
            val selectedMimeType = mimeTypeSpinner.selectedItem as String

            if (multiSelectSwitch.isChecked) {
                pickMultipleFilesLauncher.launch(arrayOf(selectedMimeType))
            } else {
                pickSingleFileLauncher.launch(arrayOf(selectedMimeType))
            }
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