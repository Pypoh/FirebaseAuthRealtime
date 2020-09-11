package com.example.firebaseauthrealtime

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mUser: FirebaseUser

    private lateinit var database: FirebaseDatabase
    private lateinit var userRef: DatabaseReference

    private lateinit var imagePreview: ImageView
    private lateinit var selectButton: Button

    companion object {
        const val SELECT_PICTURE: Int = 1
    }

    private val storage by lazy { FirebaseStorage.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance()
        userRef = database.getReference("Users")

        imagePreview = findViewById(R.id.image_view)
        selectButton = findViewById(R.id.select_btn)

        // Image Storage Ref


        registerUser("Naufal Afif", "afif@gmail.com", "password")

        selectButton.setOnClickListener {
            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhoto, SELECT_PICTURE)
        }
    }

    fun uploadPictureDatabase(imageView: ImageView) {
        imageView.isDrawingCacheEnabled = true
        imageView.buildDrawingCache()
        val bitmap = (imageView.drawable as BitmapDrawable).bitmap

        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val filename = "images/" + UUID.randomUUID().toString()
        val ref = storage.getReference(filename)

        val uploadTask = ref.putBytes(data)
        uploadTask.addOnSuccessListener {
            Log.d("UploadDebug: ", "Success: ${ref.downloadUrl}")

        }.addOnFailureListener {
            Log.d("UploadDebug: ", it.message.toString())
        }

        val urlTask = uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            ref.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                Log.d("UploadDebug: ", "download url: $downloadUri")
            } else {
                // Handle failures
                // ...
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode === Activity.RESULT_OK) {
            when (requestCode) {
                SELECT_PICTURE -> {
                    val selectedImageURI: Uri = data!!.data!!
//                        image_uri.setText(selectedImageURI.toString())
                    Picasso.get().load(selectedImageURI)
                        .centerCrop().fit()
                        .into(imagePreview, object : Callback {
                            override fun onSuccess() {
                                uploadPictureDatabase(imagePreview)
                            }

                            override fun onError(e: Exception?) {
                            }
                        })
                }
            }
        }
    }

    fun registerUser(username: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d("RegisterUserDebug: ", "User: $email registered")
                    insertUserDataToDatabase(username, email)
                } else {
                    Log.d("RegisterUserDebug: ", "User: $email failed")
                    Log.d("RegisterUserDebug: ", task.exception.toString())
                }
            }
    }

    fun insertUserDataToDatabase(username: String, email: String) {
        val uid = mAuth.currentUser!!.uid
        mUser = mAuth.currentUser!!
        val uri: Uri // return https:www.gambar.com/pic/1

        Log.d("UserUID", "User Registered UID: $uid")
        val user = UserModel(username, email)
        userRef.child(uid).setValue(user)


    }
}