package com.example.myapplication.activities

import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.MainActivity
import com.example.myapplication.R
import com.example.myapplication.adapters.MyAppointmentAdapter
import com.example.myapplication.fragment.CalendarFragment
import com.example.myapplication.models.AppointmentDataModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class VetHomePageActivity: AppCompatActivity(){
    lateinit var recyclerView: RecyclerView
    val myCalendar: Calendar = Calendar.getInstance()
    val database = FirebaseDatabase.getInstance()
    val myReference = database.getReference("appointments")
    private var adapter: MyAppointmentAdapter? = null
    private var list: ArrayList<AppointmentDataModel>? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vet_homepage)

        recyclerView = findViewById(R.id.recycleviewVet)
        recyclerView.setHasFixedSize(true)
        recyclerView.setLayoutManager(LinearLayoutManager(this))

        list = ArrayList()
        adapter = MyAppointmentAdapter(this, list!!) { appointmentData -> itemClicked() }

        recyclerView.setAdapter(adapter)
        myReference.addValueEventListener(object : ValueEventListener {
            val bundle: Bundle? = intent.extras
            val emailVet = bundle?.get("emailVet")
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dataSnapshot in snapshot.children) {
                    val appointmentData = dataSnapshot.getValue(AppointmentDataModel::class.java)
                    if (appointmentData != null && appointmentData.emailVet?.equals(emailVet) == true) {
                        list!!.add(appointmentData)
                    }
                }
                adapter!!.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun itemClicked() {
        val ft: FragmentTransaction = supportFragmentManager.beginTransaction()
        ft.replace(R.id.your_placeholder, CalendarFragment())
        ft.commit()
        recyclerView.visibility=View.GONE
        val date = OnDateSetListener { View, year, monthOfYear, dayOfMonth ->
            myCalendar.set(Calendar.YEAR, year)
            myCalendar.set(Calendar.MONTH, monthOfYear)
            myCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel()
        }
        DatePickerDialog(this, date, myCalendar[Calendar.YEAR], myCalendar[Calendar.MONTH],
                myCalendar[Calendar.DAY_OF_MONTH]).show()
    }
    private fun updateLabel() {
        val appointmentDate = findViewById<View>(R.id.AppointmentDate) as EditText
        val myFormat = "MM/dd/yy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        appointmentDate.setText(sdf.format(myCalendar.getTime()))
        val bundle2: Bundle? = intent.extras
        val userId= bundle2?.getString("userID")
        val confirmBtn:Button?=findViewById(R.id.confirmBtn)
        confirmBtn?.setOnClickListener(View.OnClickListener {
            intent = Intent(applicationContext, MainActivity::class.java)
            intent.putExtra("time", myCalendar.getTime())
            userId?.let { it1 -> myReference.child(it1).child("approved").setValue(myCalendar.getTime().toString()) }
            startActivity(intent)
        })
    }
    fun logout(view: View?) {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}