package com.example.aitforumdemo.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aitforumdemo.LoginActivity
import com.example.aitforumdemo.MainActivity
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.FragmentHomeBinding
import com.example.aitforumdemo.main.CreatePostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

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

        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            val intentMain = Intent()
            intentMain.setClass(
                requireContext(), LoginActivity::class.java
            )
            startActivity(intentMain)
            (context as MainActivity).finish()
        }

        queryNumOfPosts()

        return root
    }

    var snapshotListener: ListenerRegistration? = null

    fun queryNumOfPosts() {
        val numOfPosts = FirebaseFirestore.getInstance().collection(
            CreatePostActivity.COLLECTION_POSTS
        ).whereEqualTo("uid", FirebaseAuth.getInstance().currentUser!!.uid)

        snapshotListener = numOfPosts.addSnapshotListener { value, e ->
            if (e != null) {
                Log.w("TAG", "Listen failed.", e)
                return@addSnapshotListener
            }
            binding.textDashboard.text = "Your number of posts: ${value!!.count()}"

        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        snapshotListener?.remove()
    }

}