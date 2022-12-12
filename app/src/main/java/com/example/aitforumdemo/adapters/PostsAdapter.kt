package com.example.aitforumdemo.adapters

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.aitforumdemo.MainActivity
import com.example.aitforumdemo.R
import com.example.aitforumdemo.main.CreatePostActivity
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.PostRowBinding
import com.google.firebase.firestore.FirebaseFirestore

class PostsAdapter : RecyclerView.Adapter<PostsAdapter.ViewHolder>{

    var context: Context
    var currentUid: String
    var  postsList = mutableListOf<Post>()
    var  postKeys = mutableListOf<String>()
    var ID: String = ""

    companion object {
        const val DOC_ID = "DOC_ID"
        const val AUTHOR = "AUTHOR"
        const val TITLE = "TITLE"
        const val BODY = "BODY"
        const val LOCATION = "LOCATION"
//        const val ID_SIX = "ID_SIX"
    }

    constructor(context: Context, uid: String) : super() {
        this.context = context
        this.currentUid = uid
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = PostRowBinding
            .inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return postsList.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        var post = postsList[holder.adapterPosition]
        holder.itemView.startAnimation(AnimationUtils.loadAnimation(holder.itemView.context, R.anim.anim_one))
//        var post = postsList[0]
        holder.bind(post)
    }

    fun addPost(post: Post, key: String) {
        postsList.add(post)
        postKeys.add(key)
        //notifyDataSetChanged()
        notifyItemInserted(postsList.lastIndex)
    }

    // when I remove the post object
    private fun removePost(index: Int) {
        FirebaseFirestore.getInstance().collection(
            CreatePostActivity.COLLECTION_POSTS).document(
            postKeys[index]
        ).delete()

        postsList.removeAt(index)
        postKeys.removeAt(index)
        notifyItemRemoved(index)
    }

    // when somebody else removes an object
    fun removePostByKey(key: String) {
        val index = postKeys.indexOf(key)
        if (index != -1) {
            postsList.removeAt(index)
            postKeys.removeAt(index)
            notifyItemRemoved(index)
        }
    }

    fun editPostByKey(editedPost: Post, key: String){
        val index = postKeys.indexOf(key)
        if (index != -1) {
            postsList[index] = editedPost
            notifyItemChanged(index)
        }
    }

    inner class ViewHolder(val binding: PostRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(post: Post) {
            binding.tvAuthor.text = post.author
            binding.tvTitle.text = post.title
            binding.tvBody.text = post.body

            if (currentUid == post.uid){
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnEdit.visibility = View.VISIBLE
            } else {
                binding.btnDelete.visibility = View.GONE
                binding.btnEdit.visibility = View.GONE
            }


            // edit code goes to createpostactivity with some intent parameters
            binding.btnEdit.setOnClickListener {
                ID = FirebaseFirestore.getInstance().collection(
                    CreatePostActivity.COLLECTION_POSTS
                ).document(
                    postKeys[adapterPosition]
                ).id
                val intentMain = Intent()
                intentMain.setClass(
                    context, CreatePostActivity::class.java
                )
                intentMain.putExtra(DOC_ID, ID)
                intentMain.putExtra(AUTHOR, post.author)
                intentMain.putExtra(TITLE, post.title)
                intentMain.putExtra(BODY, post.body)
                intentMain.putExtra(LOCATION, post.location)

                (context as MainActivity).startActivity(intentMain)
            }

            binding.btnDelete.setOnClickListener{
                FirebaseFirestore.getInstance().collection(
                    CreatePostActivity.COLLECTION_POSTS
                ).document(
                    postKeys[adapterPosition]
                ).delete()
            }
        }
    }
}