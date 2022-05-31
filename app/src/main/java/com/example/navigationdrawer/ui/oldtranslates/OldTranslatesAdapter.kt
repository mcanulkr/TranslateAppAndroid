package com.example.navigationdrawer.ui.oldtranslates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationdrawer.OldTranslatesModel
import com.example.navigationdrawer.TimeUtils
import kotlinx.android.synthetic.main.old_translates_item.view.*

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.Navigation.findNavController
import com.example.navigationdrawer.R
import com.example.navigationdrawer.ui.home.HomeFragment


class OldTranslatesAdapter(val context : Context): RecyclerView.Adapter<OldTranslatesAdapter.ViewHolder>() {

    var list : List<OldTranslatesModel> = ArrayList<OldTranslatesModel>()

    class ViewHolder(view:View) : RecyclerView.ViewHolder(view) {
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val infalate = LayoutInflater.from(context).inflate(R.layout.old_translates_item,parent,false)
        return ViewHolder((infalate))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val translate = list[position]

        val createdAt = translate.date!!.time.div(1000L).let {
            TimeUtils.getTimeAgo(it.toInt())
        }
        holder.itemView.oldTranslateDate.setText(createdAt)
        holder.itemView.oldTranslateText.setText(translate.text)

        holder.itemView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
            }
        })
    }

    override fun getItemCount(): Int {
        return list.size
    }

}