package com.onenote.android

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat

class NoteEditActivity : AppCompatActivity() {

    lateinit var preferences: Preferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_note_edit)

        // Init Preferences
        preferences = Preferences(this)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.navigationIcon?.setTint(ContextCompat.getColor(this, R.color.white))

        // Find views by ID
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)
        val buttonSave = findViewById<Button>(R.id.buttonSave)

        // Set title and message if available
        noteEditTitle.setText(preferences.getNoteTitle())
        noteEditMessage.setText(preferences.getNoteMessage())

        // Set OnClickListener
        buttonSave.setOnClickListener{
            Toast.makeText(this, R.string.saved, Toast.LENGTH_LONG).show()

            preferences.setNoteMessage(noteEditMessage.editableText.toString())
            preferences.setNoteTitle(noteEditTitle.editableText.toString())

            finish()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_edit, menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.delete) {
            preferences.setNoteTitle(null)
            preferences.setNoteMessage(null)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}