package com.example.aitforumdemo.main

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.aitforumdemo.R
import com.example.aitforumdemo.adapters.PostsAdapter
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.ActivityScrollingBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*

class ScrollingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityScrollingBinding
    private lateinit var adapter: PostsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityScrollingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = PostsAdapter(this,
        FirebaseAuth.getInstance().currentUser!!.uid)
        binding.recyclerPosts.adapter = adapter

        setSupportActionBar(findViewById(R.id.toolbar))
        binding.toolbarLayout.title = title
        binding.fab.setOnClickListener { view ->
            startActivity(Intent(this, CreatePostActivity::class.java))
        }
        
        queryPosts()
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
                        this@ScrollingActivity, "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                    return
                }

                for (docChange in querySnapshot?.getDocumentChanges()!!) {
                    when (docChange.type) {
                        DocumentChange.Type.ADDED -> {
                            val post = docChange.document.toObject(Post::class.java)
                            adapter.addPost(post, docChange.document.id)
                        }
                        DocumentChange.Type.REMOVED -> {
                            adapter.removePostByKey(docChange.document.id)
                        }
                        DocumentChange.Type.MODIFIED -> {

                        }
                    }
                }
            }
        }

        snapshotListener = queryPosts.addSnapshotListener(eventListener)
    }

    override fun onDestroy() {
        super.onDestroy()
        snapshotListener?.remove()
    }
}