package com.example.docsuipickerdemo

import com.example.docsuipickerdemo.data.FileMeta
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.util.*

class CreateDocumentActivity : AppCompatActivity() {

    private lateinit var selectedFileText: TextView
    private lateinit var mimeTypeSpinner: Spinner

    private val createDocumentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            val uri = result.data?.data
            uri?.let {
                val meta = getFileMetaFromUri(this, it)
                val mimeType = contentResolver.getType(it)
                val readableSize = meta.sizeInBytes?.let { bytes -> formatFileSize(bytes) } ?: getString(R.string.unknown)

                val fileInfo = getString(
                    R.string.file_info,
                    meta.name ?: getString(R.string.unknown),
                    mimeType ?: getString(R.string.unknown),
                    readableSize
                )
                selectedFileText.text = fileInfo
            } ?: run {
                selectedFileText.text = getString(R.string.no_file_selected)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_document)

        val fileNameInput = findViewById<EditText>(R.id.fileNameInput)
        val createFileButton = findViewById<Button>(R.id.createFileButton)
        selectedFileText = findViewById(R.id.createdFileText)
        mimeTypeSpinner = findViewById(R.id.mimeTypeSpinnerCreate)

        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        mimeTypeSpinner.adapter = ArrayAdapter(
            this,
            R.layout.spinner_item_centered,
            resources.getStringArray(R.array.mime_types)
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }

        createFileButton.setOnClickListener {
            val selectedMimeType = mimeTypeSpinner.selectedItem as String
            val fileName = fileNameInput.text.toString().ifBlank { "new_file.txt" }

            val createIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = selectedMimeType
                putExtra(Intent.EXTRA_TITLE, fileName)
            }

            createDocumentLauncher.launch(createIntent)
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

                if (nameIndex != -1) name = it.getString(nameIndex)
                if (sizeIndex != -1) size = it.getLong(sizeIndex)
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
