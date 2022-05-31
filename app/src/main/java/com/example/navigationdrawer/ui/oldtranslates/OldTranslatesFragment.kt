package com.example.navigationdrawer.ui.oldtranslates

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.navigationdrawer.databinding.FragmentOldTranslatesBinding

class OldTranslatesFragment : Fragment() {

    private lateinit var oldTranslatesViewModel: OldTranslatesViewModel
    private var _binding: FragmentOldTranslatesBinding? = null
    private lateinit var oldTranslatesAdapter: OldTranslatesAdapter

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        oldTranslatesViewModel = ViewModelProvider(this).get(OldTranslatesViewModel::class.java)

        _binding = FragmentOldTranslatesBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val recylcerView : RecyclerView = binding.oldTranslatesRecyclerView
        recylcerView.layoutManager = LinearLayoutManager(context)

        oldTranslatesAdapter = OldTranslatesAdapter(context!!)
        recylcerView.adapter = oldTranslatesAdapter

        //View Model dinlenir
        oldTranslatesViewModel.getTranslates().observe(this, Observer { list ->
            oldTranslatesAdapter.list = list
            oldTranslatesAdapter.notifyDataSetChanged()
            if (list.size == 0){
                binding.noTranslateText.visibility = View.VISIBLE
            }else{
                binding.noTranslateText.visibility = View.GONE
            }
        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}