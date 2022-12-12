package com.example.aitforumdemo.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aitforumdemo.adapters.PostsAdapter
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.FragmentIdeasBinding
import com.example.aitforumdemo.main.CreatePostActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class IdeasFragment : Fragment() {

    private lateinit var adapter: PostsAdapter

    private var _binding: FragmentIdeasBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIdeasBinding.inflate(inflater, container, false)
        val root: View = binding.root

        adapter = PostsAdapter(requireActivity(),
            FirebaseAuth.getInstance().currentUser!!.uid)
        binding.recyclerPosts.adapter = adapter

        queryPosts()

        return root
    }

    var snapshotListener: ListenerRegistration? = null

    fun queryPosts(){
        val queryPosts = FirebaseFirestore.getInstance().collection(
            CreatePostActivity.COLLECTION_POSTS
        )

        val eventListener = object : EventListener<QuerySnapshot> {
            override fun onEvent(querySnapshot: QuerySnapshot?,
                                 e: FirebaseFirestoreException?) {
                if (e != null) {
                    Toast.makeText(
                        requireActivity(), "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                for (docChange in querySnapshot?.documentChanges!!) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val post = docChange.document.toObject(Post::class.java)
                            adapter.addPost(post, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            adapter.removePostByKey(docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {
                            Log.d("LALA", "CALLED")
                            adapter.editPostByKey(docChange.document.id)
                        }
                    }
                }
            }
        }

        snapshotListener = queryPosts.addSnapshotListener(eventListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        snapshotListener?.remove()
    }
}