package com.example.navigationdrawer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_sign_up.*

class SignUp : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_up)

        signUpLoading.visibility = View.GONE

        auth = FirebaseAuth.getInstance()

        //Butona basıldıktan sonra önce EditTextlerin boş olup olmadığı kontrolü yapılır
        signUpButton.setOnClickListener {
            when{
                signUpName.text.isEmpty() == true -> {
                    Snackbar.make(it,"İsim boş bırakılamaz",Snackbar.LENGTH_SHORT).show()
                }signUpSurname.text.isEmpty() == true -> {
                    Snackbar.make(it,"Soy isim boş bırakılamaz",Snackbar.LENGTH_SHORT).show()
                }signUpMail.text.isEmpty() == true ->{
                    Snackbar.make(it,"Mail boş bırakılamaz",Snackbar.LENGTH_SHORT).show()
                }signUpPassword.text.isEmpty() == true ->{
                    Snackbar.make(it,"Şifre boş bırakılamaz",Snackbar.LENGTH_SHORT).show()
                }signUpPasswordAgain.text.isEmpty() == true -> {
                    Snackbar.make(it,"Şifre tekrar boş bırakılamaz",Snackbar.LENGTH_SHORT).show()
                }signUpPassword.text.toString() != signUpPasswordAgain.text.toString() ->{
                    Snackbar.make(it,"Şifreler uyuşmuyor",Snackbar.LENGTH_SHORT).show()
                }else -> {
                    signUpLoading.visibility = View.VISIBLE
                    //Kullanıcı oluşturmak için oluşturulan fonksiyon
                    createUser()
                }
            }
        }
    }

    private fun createUser(){
        //Önce kullanıcı oluştrulur
        auth.createUserWithEmailAndPassword(
            signUpMail.text.toString(),
            signUpPassword.text.toString()
        ).addOnSuccessListener {
            if (it != null){
                //Kullanıcı oluşturma işlemi başarılı olduysa veritabanına bilgileri kaydedilir
                addUserInfoSaveToFirestore(FirebaseAuth.getInstance().currentUser!!.uid)
            }
        }
    }

    private fun addUserInfoSaveToFirestore(userId : String){
        val userHashMap = hashMapOf<String,Any>()
        userHashMap.put("userId",userId)
        userHashMap.put("name",signUpName.text.toString())
        userHashMap.put("surname",signUpSurname.text.toString())
        userHashMap.put("mail",signUpMail.text.toString())
        userHashMap.put("date",Timestamp.now())

        FirebaseFirestore.getInstance().collection("users").document(userId)
            .set(userHashMap).addOnCompleteListener { task->
                if (task.isSuccessful == true){
                    /*Kayıttan sonra firebase otomatik giriş yapar onun için önce kullanıcıyı
                        signOut yapıp giriş sayfasına dönülmeli*/
                    startActivity(Intent(this,SignIn::class.java))
                    auth.signOut()
                    finish()
                }
            }
    }

}