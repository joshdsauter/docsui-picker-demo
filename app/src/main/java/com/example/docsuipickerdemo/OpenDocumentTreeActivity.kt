package com.example.docsuipickerdemo

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class OpenDocumentTreeActivity : AppCompatActivity() {

    private lateinit var selectedDirectoryText: TextView

    private val openDocumentTreeLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
            uri?.let {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
                selectedDirectoryText.text = getString(R.string.selected_directory, it.toString())
                Log.d("DirectoryPicker", "Selected directory URI: $it")
            } ?: run {
                selectedDirectoryText.text = getString(R.string.no_directory_selected)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_document_tree)
        findViewById<Button>(R.id.backToMainButton).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish() // optional: closes the current activity
        }

        val pickDirectoryButton = findViewById<Button>(R.id.pickDirectoryButton)
        selectedDirectoryText = findViewById(R.id.selectedDirectoryText)

        pickDirectoryButton.setOnClickListener {
            openDocumentTreeLauncher.launch(null)
        }
    }
}
