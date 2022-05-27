package com.example.myapplication.activities

import android.content.Intent
import android.view.View.*
import android.os.Bundle
import android.text.Html
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import java.util.HashMap

class PetOwnerSignUpActivity :  AppCompatActivity(){
    lateinit var petOwnerFirstName: EditText
    lateinit var petOwnerLastName: EditText
    lateinit var petOwnerEmail: EditText
    lateinit var petOwnerPassword: EditText
    lateinit var petOwnerCity: EditText
    lateinit var register: Button
    var fAuth: FirebaseAuth? = null
    lateinit var progressBar: ProgressBar
    var fStore: FirebaseFirestore? = null
    private var userID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_petowner_signup)
        supportActionBar?.title = Html.fromHtml("<font color=\"black\">" + "Pet Details"+"</font>")

        petOwnerFirstName = findViewById(R.id.enterFirstNamePO)
        petOwnerLastName = findViewById(R.id.enterLastNamePO)
        petOwnerCity = findViewById(R.id.enterAddressPO)
        petOwnerEmail = findViewById(R.id.enterEmailAddressPO)
        petOwnerPassword = findViewById(R.id.enterPasswordPO)
        register = findViewById(R.id.registerPO)
        fAuth = FirebaseAuth.getInstance()
        fStore = FirebaseFirestore.getInstance()
        progressBar = findViewById(R.id.progressBarPO)

        register.setOnClickListener(OnClickListener {
            val petOwnerEmail = petOwnerEmail.text.toString().trim { it <= ' ' }
            val petOwnerPassword = petOwnerPassword.text.toString().trim { it <= ' ' }
            val pwtOwnerFirstName = petOwnerFirstName.text.toString()
            val petOwnerLastName = petOwnerLastName.text.toString()
            val petOwnersCity = petOwnerCity.text.toString()
            if (TextUtils.isEmpty(petOwnerEmail)) {
                this.petOwnerEmail.error = "Email is Required."
                return@OnClickListener
            }
            if (TextUtils.isEmpty(petOwnerPassword)) {
                this.petOwnerPassword.error = "Password is Required."
                return@OnClickListener
            }
            if (petOwnerPassword.length < 6) {
                this.petOwnerPassword.error = "Password Must be >= 6 Characters"
                return@OnClickListener
            }
            progressBar.visibility = VISIBLE

            fAuth!!.createUserWithEmailAndPassword(petOwnerEmail, petOwnerPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user_ = fAuth?.currentUser
                            user_?.sendEmailVerification()?.addOnSuccessListener {
                                Toast.makeText(
                                    this,
                                    "Verification Email Has been Sent.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                task.result?.user?.let { it1 -> onAuthSuccess(it1) }
                            }
                                ?.addOnFailureListener { e ->
                                    Log.d(
                                        VetSignUpActivity.TAG,
                                        "onFailure: Email not sent " + e.message
                                    )
                                }
                            Toast.makeText(this, "User Created.", Toast.LENGTH_SHORT).show()
                            userID = fAuth!!.currentUser!!.uid
                            val documentReference = fStore!!.collection("users").document(
                                    userID!!
                            )
                            val user: MutableMap<String, Any> = HashMap()
                            user["fName"] = pwtOwnerFirstName
                            user["lName"] = petOwnerLastName
                            user["email"] = petOwnerEmail
                            user["city"] = petOwnersCity
                            documentReference.set(user).addOnSuccessListener {
                                Log.d(
                                        VetSignUpActivity.TAG,
                                        "onSuccess: user Profile is created for $userID"
                                )
                            }
                                    .addOnFailureListener { e -> Log.d(VetSignUpActivity.TAG, "onFailure: $e") }
                            startActivity(Intent(applicationContext, VetHomePageActivity::class.java))
                        } else {
                            Toast.makeText(
                                    this,
                                    "Error ! " + task.exception!!.message,
                                    Toast.LENGTH_SHORT
                            ).show()
                            progressBar.visibility = GONE
                        }
                    }
        })

}

    private fun onAuthSuccess(firebaseUser: FirebaseUser) {
        val email: String? = firebaseUser.email
        var username = email
        if (email != null && email.contains("@")) {
            username = email.split("@").toTypedArray()[0]
        }
        val user = VetSignUpActivity.User(username, email)
        val mDatabase: DatabaseReference = FirebaseDatabase.getInstance().getReference()
        mDatabase.child("pets").child(firebaseUser.getUid()).setValue(user)
        startActivity(Intent(this, MainActivity::class.java))
    }
}