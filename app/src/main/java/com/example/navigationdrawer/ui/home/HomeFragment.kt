package com.example.navigationdrawer.ui.home

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.speech.RecognizerIntent
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.navigationdrawer.R
import com.example.navigationdrawer.databinding.FragmentHomeBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import kotlinx.android.synthetic.main.bottom_sheet_languages.*
import kotlinx.android.synthetic.main.fragment_home.*
import java.util.*

class HomeFragment : Fragment() {

    private lateinit var homeViewModel: HomeViewModel
    private var _binding: FragmentHomeBinding? = null

    private lateinit var mainLanguageSpinner : Spinner // Çevrilen dil seçimi
    private lateinit var toLanguageSpinner : Spinner // Çevrilecek dil seçimi
    private lateinit var resultText : EditText // Çevrilecek metin
    private lateinit var sourceText : EditText // Çevrilen Metin

    //Seçilecek dillerin listesi
    private val fromLanguageList = arrayListOf<String>("Türkçe","İngilizce","Fransızca","Almanca","Arapça","İspanyolca","Rusça","Hintçe")
    private val toLanguageList = arrayListOf<String>("Türkçe","İngilizce","Fransızca","Almanca","Arapça","İspanyolca","Rusça","Hintçe")

    //Firebase dilleri kodları ile tanır işlem yapmak için değişkene atarız
    private val REQUEST_PERMISSION_CODE = 1
    private var mainLanguageCode = 0
    private var toLanguageCode = 0

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.resultText.visibility = View.GONE

        // Xml dosyasındaki id'ler eşleştirilir
        val microphone = binding.mic
        val translateButton = binding.translate
        resultText = binding.resultText
        sourceText = binding.sourceText

        binding.changeLanguage.setOnClickListener {
            val firstLanguage = binding.mainLanguage.text.toString()
            val secondLanguage = binding.toLanguage.text.toString()

            binding.mainLanguage.setText(secondLanguage)
            binding.toLanguage.setText(firstLanguage)

            mainLanguageCode = getLanguageCode(binding.mainLanguage.text.toString())
            toLanguageCode = getLanguageCode(binding.toLanguage.text.toString())

            if (binding.sourceText.text.toString() != ""){
                binding.sourceText.setText(resultText.text.toString())
                translate(mainLanguageCode,toLanguageCode, resultText.text.toString())
                binding.resultText.visibility = View.VISIBLE
            }

        }

        translateButton.setOnClickListener {
            if(sourceText.text.toString().isEmpty()){
                Snackbar.make(it,"Lütfen Metin Girin",Snackbar.LENGTH_LONG).show()
            }else if(mainLanguageCode == 0){
                Toast.makeText(context,"Çevrilecek dili seçin!",Toast.LENGTH_SHORT).show()
            }else if(toLanguageCode == 0){
                Toast.makeText(context,"Çevirmek istediğiniz dili seçin!",Toast.LENGTH_SHORT).show()
            }else{
                //Çeviri işlemi yapılır
                translate(mainLanguageCode,toLanguageCode, sourceText.text.toString())
            }
        }

        binding.sourceText.addTextChangedListener(object : TextWatcher{

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            //Edit Text içinde değişiklik oldukça çalışır
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                //Çeviri işlemi yapılır
                if (binding.sourceText.text.toString() != ""){

                    mainLanguageCode = getLanguageCode(binding.mainLanguage.text.toString())
                    toLanguageCode = getLanguageCode(binding.toLanguage.text.toString())

                    translate(mainLanguageCode,toLanguageCode, p0.toString())
                    binding.resultText.visibility = View.VISIBLE
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        //Mikrofon ile text oluşturur
        binding.mic.setOnClickListener{
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Konuş ...")
            try {
                startActivityForResult(intent,REQUEST_PERMISSION_CODE)
            }catch (e : Exception){
                e.printStackTrace()
            }
        }

        binding.mainLanguage.setOnClickListener {
            getBottomSheetLayout(binding.mainLanguage)
        }

        binding.toLanguage.setOnClickListener {
            getBottomSheetLayout(binding.toLanguage)
        }

        return root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PERMISSION_CODE){
            if (resultCode == AppCompatActivity.RESULT_OK && data != null){
                val result : ArrayList<String> = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)!!
                sourceText.setText(result.get(0))
            }
        }
    }

    private fun translate(mainLanguageCode:Int,toLanguageCode:Int,Text:String){

        resultText.setText("Çeviri yapılıyor ...")
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(mainLanguageCode)
            .setTargetLanguage(toLanguageCode)
            .build()

        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().build()

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener { task ->
            resultText.setText("Çevriliyor")
            translator.translate(Text).addOnSuccessListener { task1->
                resultText.setText(task1)
                saveToFirestore()
            }.addOnFailureListener { error ->
                Toast.makeText(context,"Hata",Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(context,"Hata",Toast.LENGTH_SHORT).show()
        }
    }

    private fun getLanguageCode(language : String): Int {
        var languageCode = 0
        if (language == "Türkçe") {
            languageCode = FirebaseTranslateLanguage.TR
        }else if(language == "English"){
            languageCode = FirebaseTranslateLanguage.EN
        }else if(language == "Français"){
            languageCode = FirebaseTranslateLanguage.FR
        }else if(language == "Deutsch"){
            languageCode = FirebaseTranslateLanguage.DE
        }else if(language == "Español"){
            languageCode = FirebaseTranslateLanguage.ES
        }else if(language == "Русский"){
            languageCode = FirebaseTranslateLanguage.RU
        }else{
            languageCode = FirebaseTranslateLanguage.HI
        }
        return languageCode
    }

    private fun saveToFirestore(){
        val infoHashMap = hashMapOf<String,Any>()
        infoHashMap.put("text",binding.sourceText.text.toString())
        infoHashMap.put("date",Timestamp.now())

        FirebaseFirestore.getInstance().collection("users").document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("Translates").document().set(infoHashMap)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getBottomSheetLayout( textView: TextView ){
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.bottom_sheet_languages)

        val turkey = dialog.findViewById<LinearLayout>(R.id.turkey)
        val english = dialog.findViewById<LinearLayout>(R.id.england)
        val spain = dialog.findViewById<LinearLayout>(R.id.spain)
        val russia = dialog.findViewById<LinearLayout>(R.id.russia)
        val france = dialog.findViewById<LinearLayout>(R.id.france)
        val germany = dialog.findViewById<LinearLayout>(R.id.germany)
        val chinese = dialog.findViewById<LinearLayout>(R.id.chinese)

        english.setOnClickListener {
            textView.setText("English")
            dialog.hide()
        }

        turkey.setOnClickListener {
            textView.setText("Türkçe")
            dialog.hide()
        }

        spain.setOnClickListener {
            textView.setText("Español")
            dialog.hide()
        }

        russia.setOnClickListener {
            textView.setText("Русский")
            dialog.hide()
        }

        france.setOnClickListener {
            textView.setText("Français")
            dialog.hide()
        }

        germany.setOnClickListener {
            textView.setText("Deutsch")
            dialog.hide()
        }

        chinese.setOnClickListener {
            textView.setText("中国人")
            dialog.hide()
        }

        dialog.show()
        dialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.window!!.attributes.windowAnimations = R.style.DialogAnimation
        dialog.window!!.setGravity(Gravity.BOTTOM)
    }
}