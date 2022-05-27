package com.example.myapplication.activities


import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class AppointmentResponseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.response_of_appointment_activity)

        val response:TextView=findViewById(R.id.response)
       val firebaseAuth:FirebaseAuth = FirebaseAuth.getInstance()

        val database = FirebaseDatabase.getInstance()
        val myRef = firebaseAuth.getUid()?.let { database.getReference("appointments").child(it) }
        myRef?.child("approved")?.addListenerForSingleValueEvent(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.value=="false") {
                    response.setText("Not Approved Yet")
                }
                else
                {
                    response.setText(snapshot.value.toString())
                }
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }
}
