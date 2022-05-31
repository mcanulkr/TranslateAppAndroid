package com.example.navigationdrawer.ui.message

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationdrawer.R
import com.google.firebase.auth.FirebaseAuth

class ChatAdapter(val context: Context, val list: ArrayList<ChatModel>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val myUid = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
            val inflater = LayoutInflater.from(context).inflate(R.layout.my_message,parent,false)
            return myMessageViewHolder(inflater)
        }else{
            val inflater = LayoutInflater.from(context).inflate(R.layout.other_message,parent,false)
            return otherMessageViewHolder(inflater)
        }
    }

    class myMessageViewHolder(view: View):RecyclerView.ViewHolder(view){
        val text = view.findViewById<TextView>(R.id.myMessageTxt)
    }

    class otherMessageViewHolder(view: View):RecyclerView.ViewHolder(view){
        val text = view.findViewById<TextView>(R.id.otherMessageTxt)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = list[position]

        if (holder.javaClass == myMessageViewHolder::class.java) {
            val viewHolder = holder as myMessageViewHolder
            holder.text.setText(message.text)
        }else if (holder.javaClass == otherMessageViewHolder::class.java) {
            val viewHolder = holder as otherMessageViewHolder
            holder.text.setText(message.text)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun getItemViewType(position: Int): Int {
        val message = list[position]
        if (myUid == message.sender){
            return 1
        }else{
            return 2
        }
    }

}