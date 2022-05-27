package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.activities.PetHomePageActivity
import com.example.myapplication.activities.PetOwnerSignUpActivity
import com.example.myapplication.activities.VetHomePageActivity
import com.example.myapplication.activities.VetSignUpActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class MainActivity : AppCompatActivity() {
    lateinit var signUpAsVet: Button
    lateinit var signUpAsPetOwner: Button
    lateinit var logInEmail: EditText
    lateinit var logInPassword: EditText
    lateinit var loginBtn: Button
    lateinit var progressBar: ProgressBar
    lateinit var forgotTextLink: TextView
    var fAuth: FirebaseAuth? = null
    var flag:Boolean?=null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        signUpAsVet=findViewById(R.id.signUpAsVet)
        signUpAsPetOwner=findViewById(R.id.signUpAsPetOwner)
        logInEmail = findViewById(R.id.email)
        logInPassword = findViewById(R.id.password)
        progressBar = findViewById(R.id.progressBarMain)
        fAuth = FirebaseAuth.getInstance()
        loginBtn = findViewById(R.id.login)
        forgotTextLink = findViewById(R.id.forgotPassword)

        loginBtn.setOnClickListener(View.OnClickListener {
            val logInEmail = logInEmail.getText().toString().trim { it <= ' ' }
            val logInPassword = logInPassword.text.toString().trim { it <= ' ' }

            if (TextUtils.isEmpty(logInEmail)) {
                this.logInEmail.setError("Email is Required.")
                return@OnClickListener
            }
            if (TextUtils.isEmpty(logInPassword)) {
                this.logInPassword.setError("Password is Required.")
                return@OnClickListener
            }
            if (logInPassword.length < 6) {
                this.logInPassword.setError("Password Must be >= 6 Characters")
                return@OnClickListener
            }
            progressBar.setVisibility(View.VISIBLE)

            fAuth!!.signInWithEmailAndPassword(logInEmail, logInPassword).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Logged in Successfully", Toast.LENGTH_SHORT).show()
                    val database = FirebaseDatabase.getInstance()
                    val myRef = database.getReference()
                    task.getResult()?.user?.let { it1 ->
                        myRef.child("vets").child(it1.uid).addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (snapshot.exists()) {
                                    val userId: String? = task.getResult()?.user?.uid
                                    intent = Intent(applicationContext, VetHomePageActivity::class.java)
                                    intent.putExtra("emailVet", logInEmail)
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                } else {
                                    val userId: String? = task.getResult()?.user?.uid
                                    intent= Intent(applicationContext, PetHomePageActivity::class.java)
                                    intent.putExtra("emailPet", logInEmail)
                                    intent.putExtra("userId", userId)
                                    startActivity(intent)
                                }
                            }
                            override fun onCancelled(error: DatabaseError) {}
                        })
                    }
                } else {
                    Toast.makeText(
                            this,
                            "Error ! " + task.exception!!.message,
                            Toast.LENGTH_SHORT
                    ).show()
                    progressBar.setVisibility(View.GONE)
                }
            }
        })
        signUpAsVet.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, VetSignUpActivity::class.java)
            flag = true
            intent.putExtra("flag", flag)
            startActivity(intent)
        })

        signUpAsPetOwner.setOnClickListener(View.OnClickListener {
            val intent = Intent(this, PetOwnerSignUpActivity::class.java)
            startActivity(intent)
        })

        forgotTextLink.setOnClickListener(View.OnClickListener { v ->
            val resetMail = EditText(v.context)
            val passwordResetDialog = AlertDialog.Builder(v.context)
            passwordResetDialog.setTitle("Reset Password ?")
            passwordResetDialog.setMessage("Enter Your Email To Received Reset Link.")
            passwordResetDialog.setView(resetMail)
            passwordResetDialog.setPositiveButton("Yes") { dialog, which ->
                val mail = resetMail.text.toString()
                fAuth!!.sendPasswordResetEmail(mail).addOnSuccessListener {
                    Toast.makeText(
                            this,
                            "Reset Link Sent To Your Email.",
                            Toast.LENGTH_SHORT
                    ).show()
                }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                    this,
                                    "Error ! Reset Link is Not Sent" + e.message,
                                    Toast.LENGTH_SHORT
                            ).show()
                        }
            }
            passwordResetDialog.setNegativeButton("No") { dialog, which ->
            }
            passwordResetDialog.create().show()
        })
    }
    fun logOut(view: View) {
    }
}


