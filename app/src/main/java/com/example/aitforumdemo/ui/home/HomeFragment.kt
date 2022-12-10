package com.example.aitforumdemo.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aitforumdemo.databinding.FragmentHomeBinding
import com.example.aitforumdemo.main.CreatePostActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
//    private var numOfPosts: Int = 0

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val dashboardViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnAdd.setOnClickListener {
            val intentMain = Intent()
            intentMain.setClass(
                requireContext(), CreatePostActivity::class.java
            )
            startActivity(intentMain)
        }

        val textView: TextView = binding.textDashboard
//        binding.textDashboard.text = "Your number of posts:"  + numOfPosts.toString()
//        dashboardViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}