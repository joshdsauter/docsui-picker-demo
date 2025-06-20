package com.example.docsuipickerdemo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.openDocumentButton).setOnClickListener {
            startActivity(Intent(this, OpenDocumentActivity::class.java))
        }

        findViewById<Button>(R.id.createDocumentButton).setOnClickListener {
            startActivity(Intent(this, CreateDocumentActivity::class.java))
        }

        findViewById<Button>(R.id.openDocumentTreeButton).setOnClickListener {
            startActivity(Intent(this, OpenDocumentTreeActivity::class.java))
        }
    }
}
