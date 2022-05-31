package com.example.navigationdrawer.ui.message

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.navigationdrawer.R
import com.example.navigationdrawer.UserModel
import kotlinx.android.synthetic.main.search_user_item.view.*

class ChatSearchUserAdapter(val context:Context,val list:ArrayList<UserModel>):RecyclerView.Adapter<ChatSearchUserAdapter.ViewHolder>(){

    class ViewHolder (view: View) : RecyclerView.ViewHolder(view) {}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(context).inflate(R.layout.search_user_item,parent,false)
        return ViewHolder(inflater)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val user = list[position]
        if (user.image != null){
            Glide.with(context).load(user.image).into(holder.itemView.searchUserProfilePhoto)
        }
        holder.itemView.searchUserName.setText(user.name)
        holder.itemView.setOnClickListener {
            val intent = Intent(context, Chat::class.java)
            intent.putExtra("userId",user.userId)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}