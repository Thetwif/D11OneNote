package com.onenote.android

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import android.media.MediaPlayer

// This class lets the user assign a grade for the project
// The imageview shows the user if he chose wisely
class NoteGradeActivity : AppCompatActivity() {


    private  lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //Set xml layout for this activity
        setContentView(R.layout.activity_grade_selector)

        // Set up toolbar
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

        // Access the items of the dropdown list
        val languages = resources.getStringArray(R.array.grades)

        // Get the imageView
        imageView = findViewById(R.id.gradeImageView)

        // Access the spinner (Dropdown)
        val spinner = findViewById<Spinner>(R.id.spinner)
        if (spinner != null) {
            val adapter = ArrayAdapter(this,
                android.R.layout.simple_spinner_item, languages)
            spinner.adapter = adapter

            // Set ItemListener so stuff happens when the user changes his selection
            spinner.onItemSelectedListener = object :
                AdapterView.OnItemSelectedListener {
                override fun onItemSelected(parent: AdapterView<*>,
                                            view: View, position: Int, id: Long) {

                    // the only acceptable answer!!
                    if (languages[position].equals("1 - Perfect")){
                        imageView.setBackgroundResource(R.drawable.backgroundimage)
                        MediaPlayer.create(this@NoteGradeActivity, R.raw.cheer).start()
                    }
                    // Why would you select something else? Grml
                    else {
                        vibrate()
                        MediaPlayer.create(this@NoteGradeActivity, R.raw.boo).start()
                        imageView.setBackgroundResource(R.drawable.angry)
                    }
                }

                // Fallback Function, should never happen because per default the first item is selected
                override fun onNothingSelected(parent: AdapterView<*>) {
                    Toast.makeText(this@NoteGradeActivity,
                        "Please select one option", Toast.LENGTH_SHORT).show()
                }
            }

        }
    }

    // Function for vibrate
    private fun vibrate() {
        val vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            //deprecated in API 26
            @Suppress("DEPRECATION")
            vibrator.vibrate(500)
        }
    }

    // Create the menu
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //menuInflater.inflate(R.menu.menu_plain, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // If the arrow back is selected the user goes back to the NoteList
        if (item.itemId == android.R.id.home) {
            finish()
        }
        return super.onOptionsItemSelected(item)
    }



}