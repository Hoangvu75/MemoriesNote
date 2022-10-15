package com.example.happyplaces

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.content.ContentValues
import android.content.Intent
import android.content.res.Resources
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.drawToBitmap
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_happy_places.*
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlaceActivity : AppCompatActivity() {

    private var cal = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_happy_places)

        setSupportActionBar(toolbarAddHappyPlaceActivity)
        supportActionBar?.title = "Add Happy Place"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbarAddHappyPlaceActivity.setNavigationOnClickListener {
            finish()
            onBackPressed()
        }

        etDate.setOnClickListener {
            dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, day ->
                cal.set(Calendar.YEAR, year)
                cal.set(Calendar.MONTH, month)
                cal.set(Calendar.DAY_OF_MONTH, day)
                etDate.setText(SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(cal.time))
            }
            DatePickerDialog(this@AddHappyPlaceActivity, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        tvAddImage.setOnClickListener { addImage() }

        ivAddImage.setOnClickListener { addImage() }

        tvRotateLeft.setOnClickListener {
            val matrix = Matrix()
            matrix.postRotate(-90f)
            val rotatedBitmap = Bitmap.createBitmap(ivAddImage.drawable.toBitmap(), 0, 0, ivAddImage.drawable.intrinsicWidth, ivAddImage.drawable.intrinsicHeight, matrix, true)
            ivAddImage.setImageBitmap(rotatedBitmap)
        }

        tvRotateRight.setOnClickListener {
            val matrix = Matrix()
            matrix.postRotate(90f)
            val rotatedBitmap = Bitmap.createBitmap(ivAddImage.drawable.toBitmap(), 0, 0, ivAddImage.drawable.intrinsicWidth, ivAddImage.drawable.intrinsicHeight, matrix, true)
            ivAddImage.setImageBitmap(rotatedBitmap)
        }

        val intentId = intent.getStringExtra("id").toString()

        btnSave.setOnClickListener {
            if (etTitle.text!!.isEmpty() || etDescription.text!!.isEmpty() || etDate.text!!.isEmpty() || etLocation.text!!.isEmpty()) {
                Toast.makeText(this@AddHappyPlaceActivity, "Please fill all the fields", Toast.LENGTH_SHORT).show()
            } else {
                val id = System.currentTimeMillis().toString()
                val title = etTitle.text.toString()
                val description = etDescription.text.toString()
                val date = etDate.text.toString()
                val location = etLocation.text.toString()
                val place = Place(id, title, description, date, location)
                addPlaceData(place)
                FirebaseDatabase.getInstance().getReference(obj).child(intentId).removeValue()
                FirebaseStorage.getInstance().getReference(obj).child(intentId).delete()
            }
        }

        onEdit(intentId)

//        if(!Places.isInitialized()) {
//            Places.initialize(this@AddHappyPlaceActivity, resources.getString(R.string.google_maps_api_key))
//        }
        // open map to select location
//        etLocation.setOnClickListener {
//            try {
//                val field = listOf(
//                    com.google.android.libraries.places.api.model.Place.Field.ID,
//                    com.google.android.libraries.places.api.model.Place.Field.NAME,
//                    com.google.android.libraries.places.api.model.Place.Field.LAT_LNG,
//                    com.google.android.libraries.places.api.model.Place.Field.ADDRESS
//                )
//                val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, field).build(this@AddHappyPlaceActivity)
//                startActivityForResult(intent, 2)
//            }catch (e: Exception){
//                e.printStackTrace()
//            }
//        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 0 && resultCode == Activity.RESULT_OK){
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, data?.data)
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            if(bitmap.width > screenWidth){
                val scale = screenWidth.toFloat()/bitmap.width
                val matrix = Matrix()
                matrix.postScale(scale, scale)
                val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                ivAddImage.setImageBitmap(scaledBitmap)
            } else {
                ivAddImage.setImageBitmap(bitmap)
            }
        } else if (requestCode == 1 && resultCode == Activity.RESULT_OK) {
//            Glide.with(this@AddHappyPlaceActivity).load(imageUrl).into(ivAddImage)
            val imageUrl = getRealPathFromURI(imageUri)
            val bitmap = BitmapFactory.decodeFile(imageUrl)
            val screenWidth = Resources.getSystem().displayMetrics.widthPixels
            if(bitmap.width > screenWidth){
                val scale = screenWidth.toFloat()/bitmap.width
                val matrix = Matrix()
                matrix.postScale(scale, scale)
                val scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
                ivAddImage.setImageBitmap(scaledBitmap)
            } else {
                ivAddImage.setImageBitmap(bitmap)
            }
        } else {
            // do nothing
        }
//        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
//            val place = Autocomplete.getPlaceFromIntent(data!!)
//            etLocation.setText(place.address)
//        } else {
//            onBackPressed()
//        }
    }

    private fun addPlaceData(place: Place) {
        val dialog = Dialog(this@AddHappyPlaceActivity)
        dialog.setContentView(R.layout.dialog_custom_progress)
        dialog.setCancelable(false)
        dialog.show()
        FirebaseDatabase.getInstance().getReference(obj).child(place.id.toString()).setValue(place)
        val byteArray = ByteArrayOutputStream()
        ivAddImage.drawToBitmap().compress(Bitmap.CompressFormat.JPEG, 100, byteArray)
        FirebaseStorage.getInstance().getReference(obj).child(place.id.toString()).putBytes(byteArray.toByteArray()).addOnSuccessListener {
            Toast.makeText(this@AddHappyPlaceActivity, "Saved", Toast.LENGTH_SHORT).show()
            finish()
            onBackPressed()
        }
    }

    private fun onEdit(intentData: String) {
        FirebaseDatabase.getInstance().getReference(obj).child(intentData).addValueEventListener(object: ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val place = dataSnapshot.getValue(Place::class.java)
                etTitle.setText(place?.title)
                etDescription.setText(place?.description)
                etDate.setText(place?.date)
                etLocation.setText(place?.location)
            }
            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AddHappyPlaceActivity, error.message, Toast.LENGTH_SHORT).show()
            }
        })
        val localFile = File.createTempFile("tempImage", "jpg")
        FirebaseStorage.getInstance().getReference(obj).child(intentData).getFile(localFile).addOnSuccessListener {
            ivAddImage.setImageBitmap(BitmapFactory.decodeFile(localFile.absolutePath))
        }
    }

    private fun addImage() {
        val builder = AlertDialog.Builder(this@AddHappyPlaceActivity)
        builder.setTitle("Select Image")
        val pictureDialogItems = arrayOf("Select photo from gallery", "Capture photo from camera")
        builder.setItems(pictureDialogItems) { _, which ->
            if (which == 0) {
                // ask for permission to access gallery
                Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE).withListener(object: MultiplePermissionsListener{
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(Intent.ACTION_PICK)
                            intent.type = "image/*"
                            startActivityForResult(intent, 0)
                        } else {
                            Toast.makeText(this@AddHappyPlaceActivity, "Permissions are not granted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        AlertDialog.Builder(this@AddHappyPlaceActivity)
                            .setTitle("Permission Denied")
                            .setMessage("You have denied permission to access gallery. Please allow permission to access gallery to select image")
                            .setNegativeButton("Cancel") { _, _ ->
                                token?.cancelPermissionRequest()
                            }
                            .setPositiveButton("GO TO SETTING") { _, _ ->
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }.show()
                    }
                }).check()
            } else {
                // ask for permission to access camera
                Dexter.withActivity(this).withPermissions(Manifest.permission.CAMERA, Manifest.permission.CAMERA).withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                        if (report.areAllPermissionsGranted()) {
                            val values = ContentValues()
                            values.put(MediaStore.Images.Media.TITLE, "New Picture")
                            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera")
                            imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
                            startActivityForResult(intent, 1)
                        } else {
                            Toast.makeText(this@AddHappyPlaceActivity, "Permissions are not granted", Toast.LENGTH_SHORT).show()
                        }
                    }
                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?) {
                        AlertDialog.Builder(this@AddHappyPlaceActivity)
                            .setTitle("Permission Denied")
                            .setMessage("You have denied permission to access camera. Please allow permission to access camera to take image")
                            .setNegativeButton("CANCEL") { _, _ ->
                                token?.cancelPermissionRequest()
                            }
                            .setPositiveButton("GO TO SETTING") { _, _ ->
                                val intent = Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                                val uri = Uri.fromParts("package", packageName, null)
                                intent.data = uri
                                startActivity(intent)
                            }.show()
                    }
                }).check()
            }
        }.show()
    }

    private fun getRealPathFromURI(contentUri: Uri?): String? {
        val proj = arrayOf(MediaStore.Images.Media.DATA)
        val cursor: Cursor = managedQuery(contentUri, proj, null, null, null)
        val columnIndex: Int = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
        cursor.moveToFirst()
        return cursor.getString(columnIndex)
    }
}