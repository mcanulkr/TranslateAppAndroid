package com.example.navigationdrawer

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import java.io.ByteArrayOutputStream

class ProfileFragment : Fragment() {

    private var myUid = ""
    private var profilePhotoUri : Uri? = null
    private var imgUrl = ""
    private var reducedProfilePhoto: ByteArray? = null
    private lateinit var storageProfilePicRef : StorageReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        myUid = FirebaseAuth.getInstance().currentUser!!.uid
        storageProfilePicRef =  FirebaseStorage.getInstance().reference.child("Profile Pictures")

        getProfileInfo()

        view.profileImage.setOnClickListener {
            context?.let { it1 ->
                CropImage.activity()
                    .setAspectRatio(1,1)
                    .start(it1,this)
            }
        }

        view.save.setOnClickListener {
            uploadProfile()
        }

        view.deleteHistory.setOnClickListener {
            FirebaseFirestore.getInstance().collection("users")
                .document(myUid).collection("Translates")
                .get().addOnSuccessListener { task->
                    for (document in task.documents){
                        document.reference.delete()
                    }
                }
        }

        return view
    }

    companion object {
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
            }
    }

    private fun getProfileInfo(){
        FirebaseFirestore.getInstance().collection("users")
            .document(myUid).get().addOnSuccessListener { task->
                if (task.exists()){
                    val name = task.getString("name")
                    val mail = task.getString("mail")
                    val image = task.get("image")

                    profileName.setText(name)
                    profileMail.setText(mail)

                    if (image != null){
                        Glide.with(context!!).load(image).thumbnail(0.05f).into(profileImage)
                    }
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK  && data != null){
            val result = CropImage.getActivityResult(data)
            profilePhotoUri = result.uri

            val bitmap = MediaStore.Images.Media.getBitmap(context!!.contentResolver,profilePhotoUri)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 25, byteArrayOutputStream)
            reducedProfilePhoto = byteArrayOutputStream.toByteArray()

            profileImage.setImageBitmap(bitmap)
        }
    }

    private fun uploadProfile() {
        if (reducedProfilePhoto != null){
            val progressDialog = ProgressDialog(context)
            progressDialog.setTitle("Hesap Ayarlanıyor")
            progressDialog.setMessage("Lütfen bekleyin")
            progressDialog.show()

            val fileRef = storageProfilePicRef.child(myUid+".jpg")

            val uploadTask : StorageTask<*>
            uploadTask = fileRef.putBytes(reducedProfilePhoto!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>>{ task->
                if (!task.isSuccessful){
                    task.exception?.let {
                        throw it
                        progressDialog.dismiss()
                    }
                }

                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener ( OnCompleteListener<Uri>{ task ->
                if (task.isSuccessful){
                    val downloadUrl = task.result
                    imgUrl = downloadUrl.toString()

                    val newUserInfoHashMap = hashMapOf<String,Any>()
                    newUserInfoHashMap.put("image",imgUrl)
                    newUserInfoHashMap.put("name",profileName.text.toString())
                    newUserInfoHashMap.put("surname",profileSurname.text.toString())
                    newUserInfoHashMap.put("mail",profileMail.text.toString())

                    FirebaseFirestore.getInstance().collection("users")
                        .document(myUid).update(newUserInfoHashMap)
                }else{
                    progressDialog.dismiss()
                }
            })
        }
    }
}