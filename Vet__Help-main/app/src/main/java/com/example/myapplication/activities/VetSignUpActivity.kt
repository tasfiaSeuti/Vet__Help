package com.example.myapplication.activities

import android.content.Intent
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.view.View.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*


class VetSignUpActivity : AppCompatActivity() {
    lateinit var vetFirstName: EditText
    lateinit var vetLastName: EditText
    lateinit var vetEmail: EditText
    lateinit var vetPassword: EditText
    lateinit var vetPhone: EditText
    var vetCity: EditText? = null
    lateinit var register: Button
    var fAuth: FirebaseAuth? = null
    lateinit var progressBar: ProgressBar
    var fStore: FirebaseFirestore? = null
    var userID: String? = null

    data class User(val username: String? = null, val email: String? = null) {}
    data class UserAppointment(val emailPet: String? = null, val emailVet: String? = null, val approved:String?=null) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vet_signup)
        supportActionBar!!.title = Html.fromHtml("<font color=\"black\">" + "Vet Details"+"</font>")
        vetFirstName = findViewById(R.id.enterFirstNameVet)
        vetLastName = findViewById(R.id.enterLastNameVet)
        vetCity = findViewById(R.id.enterCity)
        vetEmail = findViewById(R.id.enterEmailAddressVet)
        vetPassword = findViewById(R.id.enterPasswordVet)
        vetPhone = findViewById(R.id.editTextPhone)
        register = findViewById(R.id.register)
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBar)

        register.setOnClickListener(OnClickListener {
            val emailVet = vetEmail.getText().toString().trim { it <= ' ' }
            val passwordVet = vetPassword.getText().toString().trim { it <= ' ' }
            val firstNameVet = vetFirstName.getText().toString()
            val lastNameVet = vetLastName.getText().toString()
            val phoneVet = vetPhone.getText().toString()
            if (TextUtils.isEmpty(emailVet)) {
                vetEmail.setError("Email is Required.")
                return@OnClickListener
            }
            if (TextUtils.isEmpty(passwordVet)) {
                vetPassword.setError("Password is Required.")
                return@OnClickListener
            }
            if (passwordVet.length < 6) {
                vetPassword.setError("Password Must be >= 6 Characters")
                return@OnClickListener
            }
            progressBar.setVisibility(VISIBLE)

            fAuth!!.createUserWithEmailAndPassword(emailVet, passwordVet)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val fuser = fAuth!!.currentUser
                            fuser!!.sendEmailVerification().addOnSuccessListener {
                                Toast.makeText(
                                        this,
                                        "Verification Email Has been Sent.",
                                        Toast.LENGTH_SHORT
                                ).show()
                                task.getResult()?.getUser()?.let { it1 -> onAuthSuccess(it1) }
                            }
                                    .addOnFailureListener { e ->
                                        Log.d(
                                                TAG,
                                                "onFailure: Email not sent " + e.message
                                        )
                                    }
                            Toast.makeText(this, "User Created.", Toast.LENGTH_SHORT).show()
                            userID = fAuth!!.currentUser!!.uid
                            val documentReference = fStore!!.collection("users").document(
                                    userID!!
                            )
                            val user: MutableMap<String, Any> = HashMap()
                            user["fName"] = firstNameVet
                            user["lName"] = lastNameVet
                            user["email"] = emailVet
                            user["phone"] = phoneVet
                            documentReference.set(user).addOnSuccessListener {
                                Log.d(
                                        TAG,
                                        "onSuccess: user Profile is created for $userID"
                                )
                            }
                                    .addOnFailureListener { e -> Log.d(TAG, "onFailure: $e") }
                            startActivity(Intent(applicationContext, VetHomePageActivity::class.java))
                        } else {
                            Toast.makeText(
                                    this,
                                    "Error ! " + task.exception!!.message,
                                    Toast.LENGTH_SHORT
                            ).show()
                            progressBar.setVisibility(GONE)
                        }
                    }
        })
    }

    private fun onAuthSuccess(firebaseUser: FirebaseUser) {
        val email: String? = firebaseUser.getEmail()
        var username = email
        if (email != null && email.contains("@")) {
            username = email.split("@").toTypedArray()[0]
        }
        val user = User(username, email)
        val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("vets").child(firebaseUser.getUid()).setValue(user)
        startActivity(Intent(this, VetHomePageActivity::class.java))
    }
    companion object {
        const val TAG = "TAG"
    }
}