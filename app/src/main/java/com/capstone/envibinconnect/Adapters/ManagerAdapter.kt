package com.capstone.envibinconnect.Adapters

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.capstone.envibinconnect.DataClasses.NotificationItem
import com.capstone.envibinconnect.Interfaces.onRequestAccept
import com.capstone.envibinconnect.R
import com.google.android.material.button.MaterialButton
import java.util.Locale

class ManagerAdapter(
    context: Context,
    notificationList: MutableList<NotificationItem>,
    onRequestAccept: onRequestAccept)
    : RecyclerView.Adapter<ManagerAdapter.ViewHolder>(){

    var context : Context = context
    private var orc : onRequestAccept

    private val list = mutableListOf<NotificationItem>()

    init {
        list.addAll(notificationList)
        orc = onRequestAccept
    }

    fun updateData(newList : List<NotificationItem>){
        list.clear()
        list.addAll(newList)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.requests_recyclerview_manager_activity,parent,false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.emailTextView.text = getUser(list[holder.adapterPosition].email)
        val address = getAddressInfo(list[holder.adapterPosition].latitude,list[holder.adapterPosition].longitude)

        if(address!=null)
            holder.addressTextView.text = "${address}"
        else
            holder.addressTextView.text = "${list[holder.adapterPosition].latitude} & ${list[holder.adapterPosition].longitude}"

        holder.acceptRequestMB.setOnClickListener {
            list.removeAt(holder.adapterPosition)
            notifyDataSetChanged()
        }

    }


    fun getUser(email: String): String {
        val atIndex = email.indexOf('@')
        return if (atIndex != -1) {
            email.substring(0, atIndex)
        }
        else  email
    }

    private fun getAddressInfo(latitude:Double, longitude:Double) : String?{
        try{
            val geocoder = Geocoder(this.context, Locale.getDefault())
            val addresses: MutableList<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            val address: String? = addresses?.get(0)?.getAddressLine(0)
            return address
        }
        catch (e : Exception){

        }
        return "${latitude} & ${longitude}"
    }


    inner class ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
         val emailTextView = itemView.findViewById(R.id.clientEmail) as TextView
         val acceptRequestMB = itemView.findViewById(R.id.acceptRequestRecyclerView) as MaterialButton
         val addressTextView = itemView.findViewById(R.id.clientAddress) as TextView
     }

}