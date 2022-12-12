package com.example.yourtrip.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.yourtrip.MainActivity
import com.example.yourtrip.R
import com.example.yourtrip.CreatePostActivity
import com.example.yourtrip.data.Post
import com.example.yourtrip.databinding.PostRowBinding
import com.google.firebase.firestore.FirebaseFirestore

class PostsAdapter(var context: Context, uid: String) :
    RecyclerView.Adapter<PostsAdapter.ViewHolder>() {

    var currentUid: String = uid
    private var  postsList = mutableListOf<Post>()
    var  postKeys = mutableListOf<String>()
    var id: String = ""

    companion object {
        const val DOC_ID = "DOC_ID"
        const val AUTHOR = "AUTHOR"
        const val TITLE = "TITLE"
        const val BODY = "BODY"
        const val LOCATION = "LOCATION"
        const val IMG_URL = "IMG_URL"
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
        val post = postsList[holder.adapterPosition]
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

    inner class ViewHolder(private val binding: PostRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(post: Post) {
            binding.tvAuthor.text = post.author
            binding.tvTitle.text = post.title
            binding.tvBody.text = post.body

            if (post.imgUrl.isNotBlank()){
                Glide
                    .with(binding.root)
                    .load(post.imgUrl)
                    .centerCrop()
                    .placeholder(R.drawable.spinner)
                    .into(binding.ivPhoto)
            }

            val colorsList = listOf(R.color.myColor1, R.color.myColor2, R.color.myColor3,
                R.color.myColor4, R.color.myColor5, R.color.myColor6)

            binding.cardView.setCardBackgroundColor(ContextCompat.getColor(context, colorsList[adapterPosition%6]))


            if (currentUid == post.uid){
                binding.btnDelete.visibility = View.VISIBLE
                binding.btnEdit.visibility = View.VISIBLE
            } else {
                binding.btnDelete.visibility = View.INVISIBLE
                binding.btnEdit.visibility = View.INVISIBLE
            }


            // edit code goes to createPostActivity with some intent parameters
            binding.btnEdit.setOnClickListener {
                id = FirebaseFirestore.getInstance().collection(
                    CreatePostActivity.COLLECTION_POSTS
                ).document(
                    postKeys[adapterPosition]
                ).id
                val intentMain = Intent()
                intentMain.setClass(
                    context, CreatePostActivity::class.java
                )
                intentMain.putExtra(DOC_ID, id)
                intentMain.putExtra(AUTHOR, post.author)
                intentMain.putExtra(TITLE, post.title)
                intentMain.putExtra(BODY, post.body)
                intentMain.putExtra(LOCATION, post.location)
                intentMain.putExtra(IMG_URL, post.imgUrl)

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