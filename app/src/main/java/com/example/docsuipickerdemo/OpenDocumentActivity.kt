package com.example.docsuipickerdemo

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.appcompat.widget.SwitchCompat
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.docsuipickerdemo.data.FileMeta
import java.util.Locale

class OpenDocumentActivity : AppCompatActivity() {

    private lateinit var selectedFileText: TextView
    private lateinit var multiSelectSwitch: SwitchCompat
    private lateinit var mimeTypeSpinner: Spinner

    private val pickSingleFileLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
            uri?.let {
                val meta = getFileMetaFromUri(this, it)
                val mimeType = contentResolver.getType(it)
                val readableSize = meta.sizeInBytes?.let { bytes -> formatFileSize(bytes) } ?: "Unknown"
                val fileInfo = getString(
                    R.string.file_info,
                    meta.name ?: getString(R.string.unknown),
                    mimeType ?: getString(R.string.unknown),
                    readableSize
                )
                selectedFileText.text = fileInfo
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } ?: run {
                selectedFileText.text = getString(R.string.no_file_selected)
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
                    val meta = getFileMetaFromUri(this, uri)
                    val mimeType = contentResolver.getType(uri)
                    val readableSize = meta.sizeInBytes?.let { bytes -> formatFileSize(bytes) } ?: getString(R.string.unknown)

                    getString(
                        R.string.file_info,
                        meta.name ?: getString(R.string.unknown),
                        mimeType ?: getString(R.string.unknown),
                        readableSize
                    )
                }

                selectedFileText.text = getString(R.string.selected_files, fileDescriptions)
            } else {
                selectedFileText.text = getString(R.string.no_files_selected)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_document)

        val pickFileButton = findViewById<Button>(R.id.pickFileButton)
        selectedFileText = findViewById(R.id.selectedFileText)
        multiSelectSwitch = findViewById(R.id.multiSelectSwitch)
        mimeTypeSpinner = findViewById(R.id.mimeTypeSpinner)
        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // optional: closes the current activity
        }

        val mimeTypes = resources.getStringArray(R.array.mime_types)
        val adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_centered,
            mimeTypes
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mimeTypeSpinner.adapter = adapter

        pickFileButton.setOnClickListener {
            val selectedMimeType = mimeTypeSpinner.selectedItem as String

            if (multiSelectSwitch.isChecked) {
                pickMultipleFilesLauncher.launch(arrayOf(selectedMimeType))
            } else {
                pickSingleFileLauncher.launch(arrayOf(selectedMimeType))
            }
        }
    }

    private fun getFileMetaFromUri(context: Context, uri: Uri): FileMeta {
        var name: String? = null
        var size: Long? = null
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = it.getColumnIndex(OpenableColumns.SIZE)

                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
                if (sizeIndex != -1) {
                    size = it.getLong(sizeIndex)
                }
            }
        }
        return FileMeta(name, size)
    }

    private fun formatFileSize(sizeInBytes: Long): String {
        val kb = 1024
        val mb = kb * 1024
        val gb = mb * 1024

        return when {
            sizeInBytes >= gb -> String.format(Locale.US, "%.2f GB", sizeInBytes.toFloat() / gb)
            sizeInBytes >= mb -> String.format(Locale.US, "%.2f MB", sizeInBytes.toFloat() / mb)
            sizeInBytes >= kb -> String.format(Locale.US, "%.2f KB", sizeInBytes.toFloat() / kb)
            else -> "$sizeInBytes B"
        }
    }
}