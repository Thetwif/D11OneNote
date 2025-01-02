package com.onenote.android

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import java.io.File
import java.io.FileOutputStream

class NoteEditActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var preferences: Preferences
    private lateinit var db: Database
    private var id = -1L
    private  lateinit var imageView: ImageView
    private var currentPath: String = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set xml layout for this activity
        setContentView(R.layout.activity_note_edit)

        // Init Preferences
        preferences = Preferences(this)

        // Init FusedLocationClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Find views by ID
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)
        val noteLocation = findViewById<EditText>(R.id.noteShowLocation)
        val buttonSave = findViewById<Button>(R.id.buttonSave)
        val buttonLocation = findViewById<Button>(R.id.buttonLocation)
        val buttonImage = findViewById<Button>(R.id.buttonImage)

        //Generate imageView
        imageView = findViewById(R.id.noteImageView)

        // Init database
        db = Database(this)
        id = intent.getLongExtra("id", -1)
        if (id >= 0) {
            val note = db.getNote(id)
            noteEditTitle.setText(note?.title)
            noteEditMessage.setText(note?.message)
            noteLocation.setText(note?.location)
            if(!note?.image.isNullOrEmpty()){
                currentPath = note?.image ?: ""
                getImage()
            }
        }

        // Set OnClickListener to display Location
        buttonLocation.setOnClickListener{
            displayLocation()
        }

        // Set OnClickListener to choose Image from Gallery
        buttonImage.setOnClickListener{
            chooseImage()
        }


        // Set OnClickListener to save Note Button, check Access-Permission
        buttonSave.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
            } else {
                saveNoteWithLocation()
            }
        }
    }

    // Display the Location in the locationfield
    private fun displayLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                2
            )
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val locationText = "Die Latitude ist: ${location.latitude} \n" +
                            "Die Longitude ist: ${location.longitude}"
                    findViewById<EditText>(R.id.noteShowLocation).setText(locationText)
                } else {
                    Toast.makeText(this, "The Location is not available. Please check your permissions", Toast.LENGTH_LONG).show()
                }
            }
    }

    //Choose Image from Gallery
    private fun chooseImage() {
        val options = arrayOf<CharSequence>("Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Photo")
        builder.setItems(options) { dialog, item ->
            when {
                options[item] == "Choose from Gallery" -> {
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 3)
                        Toast.makeText(this, "The Gallery is not available. Please check your permissions", Toast.LENGTH_LONG).show()
                    } else {
                        // Launches the Gallery if the permission is granted
                        val intent = Intent(Intent.ACTION_PICK)
                        intent.type = "image/*"
                        startActivityForResult(intent, 5)
                    }
                }
                options[item] == "Cancel" -> {
                    dialog.dismiss()
                }
            }
        }
        builder.show()
    }

    //Load previous uploaded Image
    private fun getImage() {
        if (currentPath.isNotEmpty()) {
            imageView.setImageURI(Uri.fromFile(File(currentPath)))
            imageView.visibility = View.VISIBLE
        }
    }


    // Load the image and copy it to the storage if the activity result is positive
    // Depending on the request code
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                // Load the Image
                4 -> {
                    // Load the image if path is not empty
                    if (currentPath.isNotEmpty()) {
                        imageView.setImageURI(Uri.fromFile(File(currentPath)))
                        imageView.visibility = View.VISIBLE
                    }
                }
                // Copy to storage
                5 -> {
                    data?.data?.let { uri ->
                        val inputStream = contentResolver.openInputStream(uri)
                        val timeStamp = System.currentTimeMillis()
                        val imageDirectory: File? = getExternalFilesDir(null)
                        val outputFile = File.createTempFile("IMG_${timeStamp}_", ".jpg", imageDirectory)
                            .apply { currentPath = absolutePath }
                        val outputStream = FileOutputStream(outputFile)
                        inputStream?.use { input ->
                            outputStream.use { output ->
                                input.copyTo(output)
                            }
                        }
                        // Load the image if path is not empty
                        if (currentPath.isNotEmpty()) {
                            imageView.setImageURI(Uri.fromFile(File(currentPath)))
                            imageView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    // Create Menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (id >= 0) {
            menuInflater.inflate(R.menu.menu_edit, menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    // Reroute the user to the selected screen
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
        } else if (item.itemId == R.id.delete) {
            showDeleteDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    // Show a Dialog to ask the User if he really wants to delete
    private fun showDeleteDialog() {
        AlertDialog.Builder(this)
            .setMessage(getString(R.string.delete_message))
            .setPositiveButton(R.string.yes) { _, _ ->
                db.deleteNote(id)
                finish()
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }

    // If the permission is set, the user can save the Note with the Location
    private fun saveNoteWithLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            saveNote("No Permission for Location, please check")
            return
        }
        try {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location: Location? ->
                    val locationStr = if (location != null) {
                        "Die Latitude ist: ${location.latitude} \n" +
                        "Die Longitude ist: ${location.longitude}"
                    } else {
                        "No Location is stored"
                    }
                    saveNote(locationStr)
                }
                .addOnFailureListener { e ->
                    saveNote("Location not available: ${e.message}")
                }
        } catch (e: Exception) {
            saveNote("Location permission not set, please check: ${e.message}")
        }
    }

    // If the user has no Location Permission set, he can save the Note without Location
    private fun saveNote(locationStr: String) {
        val noteEditTitle = findViewById<EditText>(R.id.noteEditTitle)
        val noteEditMessage = findViewById<EditText>(R.id.noteEditMessage)
        val note = Note(
            noteEditTitle.editableText.toString(),
            noteEditMessage.editableText.toString(),
            id,
            currentPath,
            locationStr
        )
        if (id >= 0) {
            db.updateNote(note)
        } else {
            db.insertNote(note)
        }

        finish()
    }

    // Get the results for the permissions
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    saveNoteWithLocation()
                } else {
                    saveNote("Location for permission not set, please check")
                }
            }
            2 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED
                ) {
                    displayLocation()

                }
            }
            3 -> {
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    val intent = Intent(Intent.ACTION_PICK)
                    intent.type = "image/*"
                    startActivityForResult(intent, 5)
                }
            }
        }
    }
}