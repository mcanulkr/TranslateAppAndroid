package com.example.navigationdrawer.ui.message

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.navigationdrawer.UserModel
import com.example.navigationdrawer.databinding.FragmentMessagesBinding
import com.google.firebase.firestore.*

class ChatSearchUserFragment : Fragment() {

    private var _binding: FragmentMessagesBinding? = null
    private var sUser : MutableList<UserModel>? = null
    private var searchUserAdapter : ChatSearchUserAdapter? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentMessagesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recyclerView = binding.searchUserRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        sUser = ArrayList()
        searchUserAdapter = ChatSearchUserAdapter(context!!, sUser as ArrayList<UserModel>)
        recyclerView.adapter = searchUserAdapter

        binding.searchUser.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                SearchUser(p0.toString())
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        return root
    }

    private fun SearchUser(input: String) {
        val userRef = FirebaseFirestore.getInstance().collection("users").orderBy("name")
            .startAt(input).endAt(input+"\uf8ff")
        userRef.addSnapshotListener(object : EventListener<QuerySnapshot> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onEvent(
                value: QuerySnapshot?,
                error: FirebaseFirestoreException?) {
                if (error != null){

                }else{
                    sUser!!.clear()
                    for (document in value!!.documents){
                        val name = document.getString("name")
                        val image = document.get("image")
                        val userId = document.getString("userId")

                        if (image != null) {
                            val user = UserModel(userId,name,null,image.toString())
                            sUser!!.add(user)
                        }else{
                            val user = UserModel(userId,name,null,null)
                            sUser!!.add(user)
                        }

                    }
                    searchUserAdapter!!.notifyDataSetChanged()
                }
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}