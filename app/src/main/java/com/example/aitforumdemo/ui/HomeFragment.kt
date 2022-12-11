package com.example.aitforumdemo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.aitforumdemo.databinding.FragmentHomeBinding
import com.example.aitforumdemo.main.CreatePostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.AggregateSource
import com.google.firebase.firestore.FirebaseFirestore

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

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        binding.btnAdd.setOnClickListener {
            val intentMain = Intent()
            intentMain.setClass(
                requireContext(), CreatePostActivity::class.java
            )
            startActivity(intentMain)
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        val numOfPosts = FirebaseFirestore.getInstance().collection(
            CreatePostActivity.COLLECTION_POSTS
        ).whereEqualTo("uid", FirebaseAuth.getInstance().currentUser!!.uid).count()
        numOfPosts.get(AggregateSource.SERVER).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                binding.textDashboard.text = "Your number of posts: ${task.result.count}"
            } else {
                Log.d("TAG", "Count failed: ", task.getException())
            }
        }
    }
}