package com.example.navigationdrawer.ui.message

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationdrawer.R
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import kotlinx.android.synthetic.main.activity_chat.*

class Chat : AppCompatActivity() {

    private var otherUserId = ""
    private var myUid = ""
    private var chatList : MutableList<ChatModel>? = null
    private lateinit var chatAdapter : ChatAdapter

    private val languageList = arrayListOf<String>("Türkçe","İngilizce","Fransızca","Almanca","Arapça","İspanyolca","Rusça","Hintçe")
    private var selectedLanguageCode = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        myUid = FirebaseAuth.getInstance().currentUser!!.uid
        otherUserId = intent.getStringExtra("userId")!!

        //Diğer kullanıcının ismini getirir
        FirebaseFirestore.getInstance().collection("users").document(otherUserId)
            .get().addOnSuccessListener { task->
                if (task.exists()){
                    val name = task.getString("name")
                    chatName.setText(name)
                }
            }

        val recyclerView = findViewById<RecyclerView>(R.id.chatRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        chatList = ArrayList()
        chatAdapter = ChatAdapter(this,chatList as ArrayList<ChatModel>)
        recyclerView.adapter = chatAdapter

        //Mesajları getirir
        getMessage()

        //Çevrilecek dili seçmek için
        chatSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedLanguageCode = getLanguageCode(languageList[p2])
            }
            override fun onNothingSelected(p0: AdapterView<*>?) {}
        }

        val languageAdapter = ArrayAdapter(this, R.layout.spinner_item,languageList)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        chatSpinner.adapter = languageAdapter

        sendMessage.setOnClickListener {
            translate(FirebaseTranslateLanguage.TR,selectedLanguageCode,messageTxt.text.toString())
        }

        chatBack.setOnClickListener {
            finish()
        }
    }

    private fun getLanguageCode(language : String): Int {
        var languageCode = 0
        if (language == "Türkçe") {
            languageCode = FirebaseTranslateLanguage.TR
        }else if(language == "İngilizce"){
            languageCode = FirebaseTranslateLanguage.EN
        }else if(language == "Arapça"){
            languageCode = FirebaseTranslateLanguage.AR
        }else if(language == "Fransızca"){
            languageCode = FirebaseTranslateLanguage.FR
        }else if(language == "Almanca"){
            languageCode = FirebaseTranslateLanguage.DE
        }else if(language == "İspanyolca"){
            languageCode = FirebaseTranslateLanguage.ES
        }else if(language == "Rusça"){
            languageCode = FirebaseTranslateLanguage.RU
        }else{
            languageCode = FirebaseTranslateLanguage.HI
        }
        return languageCode
    }

    private fun translate(mainLanguageCode:Int,toLanguageCode:Int,Text:String){

        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(mainLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()

        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener { task ->
            translator.translate(Text).addOnSuccessListener { task1->

                val messageHashMap = hashMapOf<String,Any>()
                messageHashMap.put("message",task1)
                messageHashMap.put("date",Timestamp.now())
                messageHashMap.put("sender",myUid)

                // Önce kendi dökümanına kaydeder
                FirebaseFirestore.getInstance().collection("messages").document(myUid)
                    .collection(otherUserId).document().set(messageHashMap)
                //Ardından diğer kullanıcının dökümanına kaydeder
                FirebaseFirestore.getInstance().collection("messages").document(otherUserId)
                    .collection(myUid).document().set(messageHashMap)

                messageTxt.setText("")

            }.addOnFailureListener { error ->
                Toast.makeText(this,"Hata", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this,"Hata", Toast.LENGTH_SHORT).show()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun getMessage(){
        FirebaseFirestore.getInstance().collection("messages").document(myUid)
            .collection(otherUserId).orderBy("date",Query.Direction.ASCENDING)
            .addSnapshotListener { value, error ->
                if (value != null){
                    chatList!!.clear()
                    for(document in value.documents){
                        val messagetxt = document.getString("message")
                        val sender = document.getString("sender")
                        val message = ChatModel(messagetxt,null,sender)
                        chatList!!.add(message)
                    }

                    chatAdapter.notifyDataSetChanged()
                }
            }
    }
}