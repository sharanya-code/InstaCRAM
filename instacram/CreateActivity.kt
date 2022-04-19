package com.example.instacram

import android.app.Activity
import android.content.Intent

import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instacram.models.Post
import com.example.instacram.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.android.synthetic.main.activity_create.*

private const val TAG = "Create Activity"
private const val PIC_CODE = 1234
class CreateActivity : AppCompatActivity() {

    private var PhotoUri: Uri?=null
    private var signedInUser: User?=null
    private lateinit var firestoredb: FirebaseFirestore
    private lateinit var storageReferance: StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create)
        storageReferance = FirebaseStorage.getInstance().reference

        firestoredb = FirebaseFirestore.getInstance()
        firestoredb.collection("users")
            .document(FirebaseAuth.getInstance().currentUser?.uid as String)
            .get()
            .addOnSuccessListener { userSnapshot ->
                signedInUser = userSnapshot.toObject(User::class.java)
                Log.i(TAG, "Signed in ID $signedInUser")
            }

            .addOnFailureListener{ exception ->
                Log.i(TAG, "Access Failed", exception)
            }

        btnPickImage.setOnClickListener{
            Log.i(TAG, "Open up image picker on device")
            val imagePickerIntent = Intent(Intent.ACTION_GET_CONTENT)
            imagePickerIntent.type = "image/*"
            if (imagePickerIntent.resolveActivity(packageManager) != null){
                startActivityForResult(imagePickerIntent, PIC_CODE)
            }
        }

        btnSubmit.setOnClickListener {
            handleSubmitButtonClick()
        }
    }

    private fun handleSubmitButtonClick() {
        if (PhotoUri == null){
            Toast.makeText(this, "No Photo selected", Toast.LENGTH_SHORT).show()
            return
        }


        if (etDescription.text.isBlank()){
            Toast.makeText(this, "Please enter a description", Toast.LENGTH_SHORT).show()
            return
        }

        if (signedInUser == null){
            Toast.makeText(this, "No User logged in, please wait", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false

        val PhotoUploadUri = PhotoUri as Uri

        val photoReference = storageReferance.child("images/${System.currentTimeMillis()}-photo.jpg")
        photoReference.putFile(PhotoUploadUri)
            .continueWithTask {photoUploadTask->
                Log.i(TAG, "Bytes Uploaded ${photoUploadTask.result?.bytesTransferred}" )
                photoReference.downloadUrl
            }.continueWithTask { downloadUrlTask ->
                val post = Post(
                    etDescription.text.toString(),
                    downloadUrlTask.result.toString(),
                    System.currentTimeMillis(),
                    signedInUser)
                firestoredb.collection("posts").add(post)
            }.addOnCompleteListener{postCreationTask ->
                btnSubmit.isEnabled = true
                if (!postCreationTask.isSuccessful){
                    Log.e(TAG, "Exception during Post Creation", postCreationTask.exception)
                    Toast.makeText(this, "Failed to save Post", Toast.LENGTH_SHORT).show()
                }
                etDescription.text.clear()
                imageView.setImageResource(0)
                Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()
                val profileIntent = Intent(this, ProfileActivity::class.java)
                profileIntent.putExtra(EXTRA_USERNAME, signedInUser?.username)
                startActivity(profileIntent)
                finish()
            }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PIC_CODE){
            if(resultCode == Activity.RESULT_OK){
                PhotoUri = data?.data
                Log.i(TAG, "Photo $PhotoUri")
                imageView.setImageURI(PhotoUri)
            }else{
                Toast.makeText(this, "Photo not picked", Toast.LENGTH_SHORT).show()
            }
        }
    }
}