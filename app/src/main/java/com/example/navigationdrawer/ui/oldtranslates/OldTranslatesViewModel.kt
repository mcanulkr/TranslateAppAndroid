package com.example.navigationdrawer.ui.oldtranslates

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.navigationdrawer.OldTranslatesModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class OldTranslatesViewModel : ViewModel() {

    private val oldTranslates : MutableLiveData<List<OldTranslatesModel>> = MutableLiveData()
    private val myUid = FirebaseAuth.getInstance().currentUser!!.uid

    // Firestore kaydedilen eski aramaları - tarihe göre - getirir
    fun getTranslates() : LiveData<List<OldTranslatesModel>>{
        if (oldTranslates.value == null){
            FirebaseFirestore.getInstance().collection("users")
                .document(myUid).collection("Translates")
                .orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { task, error ->
                    if (task != null){
                        val listOfTranslates : MutableList<OldTranslatesModel> = mutableListOf()
                        for (document in task.documents){
                            val text = document.getString("text")
                            val date = document.getTimestamp("date")!!.toDate()
                            listOfTranslates.add(OldTranslatesModel(text,date))
                        }
                        oldTranslates.postValue(listOfTranslates)
                    }
                }
        }
        return oldTranslates
    }
}