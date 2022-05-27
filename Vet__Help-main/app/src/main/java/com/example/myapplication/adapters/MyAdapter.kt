package com.example.myapplication.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.R
import com.example.myapplication.adapters.MyAdapter.MyViewHolder
import com.example.myapplication.models.VetDataModel
import java.util.*

class MyAdapter(
    var context: Context?,
    var mList: ArrayList<VetDataModel>,
    var param: (VetDataModel)->Unit
) :
    RecyclerView.Adapter<MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val v = LayoutInflater.from(context).inflate(R.layout.vet_details, parent, false)
        return MyViewHolder(v)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val model = mList[position]
        holder.username.text = model.username
        holder.email.text = model.email
        holder.bind(model)
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var username: TextView
        var email: TextView
        var button: Button
        var model: VetDataModel
        fun bind(model: VetDataModel) {
            this.model = model
        }

        init {
            username = itemView.findViewById(R.id.dummyNameItem)
            email = itemView.findViewById(R.id.dummyEmailItem)
            button = itemView.findViewById(R.id.button_)
            model= VetDataModel()
            button.setOnClickListener { param(model) }
        }

    }
}