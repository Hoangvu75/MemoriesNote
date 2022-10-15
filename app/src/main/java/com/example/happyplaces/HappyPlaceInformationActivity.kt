package com.example.happyplaces

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.text.LineBreaker
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_happy_place_information.*
import java.io.File

class HappyPlaceInformationActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_happy_place_information)

        getHappyPlaceInformation()

        setSupportActionBar(toolbarHappyPlaceInformationActivity)
        supportActionBar?.title = "Happy Place Info"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarHappyPlaceInformationActivity.setNavigationOnClickListener {
            finish()
            onBackPressed()
        }

        val intentBackground = intent.getStringExtra("color")
        llActivityHappyPlaceInformation.setBackgroundColor(Color.parseColor(intentBackground))

        btnGetMoreInfo.setOnClickListener {
            val title = tvHappyPlaceTitle.text

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/search?q=$title"))
            startActivity(intent)
        }
        btnGetMoreInfo.setOnLongClickListener {
            var location = tvHappyPlaceLocation.text
            location = location.toString().substring(9)

            val builder = AlertDialog.Builder(this@HappyPlaceInformationActivity)
            builder.setTitle("Get More Info")
            builder.setMessage("Do you want to get the location of this place?")
            builder.setPositiveButton("Yes") { _, _ ->
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=$location"))
                startActivity(intent)
            }.setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }.show()
            return@setOnLongClickListener true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                tvHappyPlaceDescription.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
            }
        }

        ivHappyPlaceImage.setOnLongClickListener {
            val dialog = Dialog(this)
            dialog.setContentView(R.layout.dialog_image)
            dialog.setCancelable(true)
            val ivDialogImage = dialog.findViewById<androidx.appcompat.widget.AppCompatImageView>(R.id.ivDialogImage)
            ivDialogImage.setImageDrawable(ivHappyPlaceImage.drawable)
            dialog.window?.setGravity(Gravity.CENTER)
            dialog.window?.setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.show()
            ivDialogImage.setOnClickListener {
                dialog.dismiss()
            }
            return@setOnLongClickListener true
        }
    }

    private fun getHappyPlaceInformation() {
        // get the information from the intent
        val intentId = intent.getStringExtra("id").toString()

        // get image from firebase storage and set it to image view
        val localFile = File.createTempFile("tempImage", "jpg")
        FirebaseStorage.getInstance().getReference(obj).child(intentId).getFile(localFile).addOnSuccessListener {
            ivHappyPlaceImage.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
        }

        // get data from intent
        FirebaseDatabase.getInstance().getReference(obj).child(intentId).addValueEventListener(object:
            ValueEventListener {
            @SuppressLint("SetTextI18n")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val place = dataSnapshot.getValue(Place::class.java)
                tvHappyPlaceTitle.text = place?.title
                tvHappyPlaceDescription.text = "    " + place?.description
                tvHappyPlaceLocation.text = "Location: " + place?.location
                tvHappyPlaceEditedDate.text = place?.date
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@HappyPlaceInformationActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
    }
}