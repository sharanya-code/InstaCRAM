package com.example.instacram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.instacram.models.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_register.*

private const val TAG = "Register Activity"
class RegisterActivity : AppCompatActivity() {
    private lateinit var firestoredatabase: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)
        val mauth = FirebaseAuth.getInstance()
        firestoredatabase = FirebaseFirestore.getInstance()
        btnRegister.setOnClickListener{
            val newEmail = etNewEmail.text.toString()
            val newPassword = etNewPassword.text.toString()
            val newUsername = etNewUsername.text.toString()
            val newAge = etNewAge.text.toString()

            if (newAge.isBlank() || newEmail.isBlank() || newUsername.isBlank() || newPassword.isBlank()){
                Toast.makeText(this, "Please Fill all the Fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val newAgeInt = newAge.toInt()
            if (newAgeInt < 13){
                Toast.makeText(this, "Not old enough", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(newPassword.length < 6){
                Toast.makeText(this, "Please use a longer password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            mauth.createUserWithEmailAndPassword(newEmail, newPassword)
                .addOnCompleteListener{task->
                    val user = User(username = newUsername, age = newAgeInt)
                    firestoredatabase.collection("users").document(FirebaseAuth.getInstance().currentUser?.uid as String).set(user)
                }.addOnCompleteListener { userCreationTask->
                    if( !userCreationTask.isSuccessful){
                        Log.e(TAG, "Exception during Post Creation", userCreationTask.exception)
                        Toast.makeText(this, "Failed to save User", Toast.LENGTH_SHORT).show()
                    }
                    etNewAge.text.clear()
                    etNewEmail.text.clear()
                    etNewPassword.text.clear()
                    etNewUsername.text.clear()
                    Toast.makeText(this, "Success!", Toast.LENGTH_SHORT).show()

                    val loginIntent = Intent(this, LoginActivity::class.java)
                    startActivity(loginIntent)
                    finish()
                }

        }
    }
}