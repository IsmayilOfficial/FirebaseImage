package com.example.uptf

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.IOException

class MainActivity : AppCompatActivity() {
    // Folder path for Firebase Storage.
    var Storage_Path = "All_Image_Uploads/"

    // Root Database Name for Firebase Database.
    var Database_Path = "All_Image_Uploads_Database"

    // Creating button.
    var ChooseButton: Button? = null
    var UploadButton: Button? = null

    // Creating EditText.
    var ImageName: EditText? = null

    // Creating ImageView.
    var SelectImage: ImageView? = null

    // Creating URI.
    var FilePathUri: Uri? = null

    // Creating StorageReference and DatabaseReference object.
    var storageReference: StorageReference? = null
    var databaseReference: DatabaseReference? = null

    // Image request code for onActivityResult() .
    var Image_Request_Code = 7
    var progressDialog: ProgressDialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().reference

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference(Database_Path)

        //Assign ID'S to button.
        ChooseButton = findViewById<View>(R.id.ButtonChooseImage) as Button
        UploadButton = findViewById<View>(R.id.ButtonUploadImage) as Button

        // Assign ID's to EditText.
        ImageName = findViewById<View>(R.id.ImageNameEditText) as EditText

        // Assign ID'S to image view.
        SelectImage = findViewById<View>(R.id.ShowImageView) as ImageView

        // Assigning Id to ProgressDialog.
        progressDialog = ProgressDialog(this@MainActivity)

        // Adding click listener to Choose image button.
        ChooseButton!!.setOnClickListener { // Creating intent.
            val intent = Intent()

            // Setting intent type as image to select image from phone storage.
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Please Select Image"), Image_Request_Code)
        }


        // Adding click listener to Upload image button.
        UploadButton!!.setOnClickListener { // Calling method to upload selected image on Firebase storage.
            UploadImageFileToFirebaseStorage()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Image_Request_Code && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            FilePathUri = data.data
            try {

                // Getting selected image into Bitmap.
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, FilePathUri)

                // Setting up bitmap selected image into ImageView.
                SelectImage!!.setImageBitmap(bitmap)

                // After selecting image change choose button above text.
                ChooseButton!!.text = "Image Selected"
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Creating Method to get the selected image file Extension from File Path URI.
    fun GetFileExtension(uri: Uri?): String? {
        val contentResolver = contentResolver
        val mimeTypeMap = MimeTypeMap.getSingleton()

        // Returning the file Extension.
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri!!))
    }

    // Creating UploadImageFileToFirebaseStorage method to upload image on storage.
    fun UploadImageFileToFirebaseStorage() {

        // Checking whether FilePathUri Is empty or not.
        if (FilePathUri != null) {

            // Setting progressDialog Title.
            progressDialog!!.setTitle("Image is Uploading...")

            // Showing progressDialog.
            progressDialog!!.show()

            // Creating second StorageReference.
            val storageReference2nd = storageReference!!.child(Storage_Path + System.currentTimeMillis() + "." + GetFileExtension(FilePathUri))

            // Adding addOnSuccessListener to second StorageReference.
            storageReference2nd.putFile(FilePathUri!!)
                    .addOnSuccessListener { taskSnapshot -> // Getting image name from EditText and store into string variable.
                        val TempImageName = ImageName!!.text.toString().trim { it <= ' ' }

                        // Hiding the progressDialog after done uploading.
                        progressDialog!!.dismiss()

                        // Showing toast message after done uploading.
                        Toast.makeText(applicationContext, "Image Uploaded Successfully ", Toast.LENGTH_LONG).show()
                        val imageUploadInfo = ImageUploadInfo(TempImageName, taskSnapshot.downloadUrl.toString())

                        // Getting image upload ID.
                        val ImageUploadId = databaseReference!!.push().key

                        // Adding image upload id s child element into databaseReference.
                        databaseReference!!.child(ImageUploadId).setValue(imageUploadInfo)
                    } // If something goes wrong .
                    .addOnFailureListener { exception -> // Hiding the progressDialog.
                        progressDialog!!.dismiss()

                        // Showing exception erro message.
                        Toast.makeText(this@MainActivity, exception.message, Toast.LENGTH_LONG).show()
                    } // On progress change upload time.
                    .addOnProgressListener { // Setting progressDialog Title.
                        progressDialog!!.setTitle("Image is Uploading...")
                    }
        } else {
            Toast.makeText(this@MainActivity, "Please Select Image or Add Image Name", Toast.LENGTH_LONG).show()
        }
    }
}