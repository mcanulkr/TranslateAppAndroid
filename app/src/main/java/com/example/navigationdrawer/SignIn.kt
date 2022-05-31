package com.example.navigationdrawer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignIn : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        auth = FirebaseAuth.getInstance()

        /* Önce kullanıcı varlığı kontrol edilerek yönlendirme yapılır eğer mevcut
            kullanıcı daha önce giriş yapmış ise ana ekrana yönlendirilir */
        if (FirebaseAuth.getInstance().currentUser != null){
            startActivity(Intent(this,MainActivity::class.java))
        }

        //Giriş butonuna tıklayınca
        // 1- Edit textlerin boş olup olmadığı kontrol edilir
        // 2- Kayıt işlemi yapılır
        // 3- Eğer kayıt işlemi tamamlandıysa ana ekrana gider yoksa hata mesajı gösterilir
        logInButton.setOnClickListener {
            if(logInMail.text.toString() != "" || logInPassword.text.toString() != ""){
                auth.signInWithEmailAndPassword(logInMail.text.toString(), logInPassword.text.toString())
                    .addOnCompleteListener {
                    if (it.isSuccessful){
                        val intent = Intent(this,MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }else{
                        Toast.makeText(this, "Hata", Toast.LENGTH_SHORT).show()
                    }
                }
            }else{
                Snackbar.make(it,"Lütfen bilgileri boş bırakmayın", Snackbar.LENGTH_LONG).show()
            }
        }

        //Kayıt sayfasına gider
        logInSignUp.setOnClickListener {
            val intent = Intent(this,SignUp::class.java)
            startActivity(intent)
        }
    }
}