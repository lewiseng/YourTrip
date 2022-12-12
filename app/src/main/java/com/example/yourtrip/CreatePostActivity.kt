package com.example.yourtrip

import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.yourtrip.R
import com.example.yourtrip.databinding.ActivityCreatePostBinding
import com.example.yourtrip.adapter.PostsAdapter
import com.example.yourtrip.data.Post
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.net.URLEncoder
import java.util.*

class CreatePostActivity : AppCompatActivity() {

    companion object {
        const val COLLECTION_POSTS = "posts"
    }

    private lateinit var binding: ActivityCreatePostBinding
    private var isEditMode = false
    private var doubleLat: Double = 0.0
    private var doubleLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(PostsAdapter.DOC_ID)) {
            isEditMode = true
        }

        if (intent.hasExtra(PostsAdapter.TITLE)) {
            binding.etTitle.setText(intent.getStringExtra(PostsAdapter.TITLE))
        }
        if (intent.hasExtra(PostsAdapter.LOCATION)) {
            binding.etAddress.setText(intent.getStringExtra(PostsAdapter.LOCATION))
        }
        if (intent.hasExtra(PostsAdapter.BODY)) {
            binding.etBody.setText(intent.getStringExtra(PostsAdapter.BODY))
        }
        if (intent.hasExtra(PostsAdapter.IMG_URL) &&
            intent.getStringExtra(PostsAdapter.IMG_URL)?.isNotBlank()!!
        ) {
            Glide
                .with(binding.root)
                .load(intent.getStringExtra(PostsAdapter.IMG_URL))
                .centerCrop()
                .placeholder(R.drawable.spinner)
                .into(binding.imgAttach)
            binding.imgAttach.visibility = View.VISIBLE
        }

        binding.btnSend.setOnClickListener {
            if (isFormValid()) {
                getCoordinates()
                Thread.sleep(1000)
                if (uploadBitmap != null) {
                    try {
                        uploadPostWithImage()
                    } catch (e: java.lang.Exception) {
                        e.printStackTrace()
                    }
                } else {
                    if (isEditMode) {
                        uploadPost(intent.getStringExtra(PostsAdapter.IMG_URL)!!)
                    } else {
                        uploadPost()
                    }
                }
            }
        }

        binding.btnAttach.setOnClickListener {
            photoLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private var uploadBitmap: Bitmap? = null

    private var photoLauncher =
        registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
            // Callback is invoked after the user selects a media item or closes the
            // photo picker.
            if (uri != null) {
                uploadBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                binding.imgAttach.setImageBitmap(uploadBitmap)
                binding.imgAttach.visibility = View.VISIBLE
            } else {
            }
        }


    private fun getCoordinates() {
        Toast.makeText(
            this@CreatePostActivity,
            getString(R.string.waitMsg), Toast.LENGTH_LONG
        ).show()
        val geocoder = Geocoder(this)
        val addressList: List<Address>?
        try {
            addressList = geocoder.getFromLocationName(binding.etAddress.text.toString(), 1)
            if (addressList != null) {
                doubleLat = addressList[0].latitude
                doubleLong = addressList[0].longitude
            }
        } catch (e: IOException) {
            Log.d(getString(R.string.catchErrorMsg), e.toString())
        }
    }


    private fun uploadPost(imgUrl: String = "") {
        if (isEditMode) {
            // update firebase values here
            val doc =
                FirebaseFirestore.getInstance().collection(COLLECTION_POSTS)
            doc.document(intent.getStringExtra(PostsAdapter.DOC_ID).toString())
                .update(
                    getString(R.string.title), binding.etTitle.text.toString(),
                    getString(R.string.location), binding.etAddress.text.toString(),
                    getString(R.string.body), binding.etBody.text.toString(),
                    getString(R.string.latitude), doubleLat.toString(),
                    getString(R.string.longitude), doubleLong.toString(),
                    getString(R.string.imgUrl), imgUrl
                )
                .addOnSuccessListener {
                    Toast.makeText(
                        this@CreatePostActivity,
                        getString(R.string.updateSuccessMsg), Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this@CreatePostActivity,
                        getString(R.string.toastErrorMsg, it.message), Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            val newPost = Post(
                FirebaseAuth.getInstance().currentUser!!.uid,
                FirebaseAuth.getInstance().currentUser!!.email!!,
                binding.etTitle.text.toString(),
                binding.etBody.text.toString(),
                binding.etAddress.text.toString(),
                doubleLat.toString(),
                doubleLong.toString(),
                imgUrl
            )

            // "connect" to posts collection (table)
            val postsCollection =
                FirebaseFirestore.getInstance().collection(
                    COLLECTION_POSTS
                )
            postsCollection.add(newPost)
                .addOnSuccessListener {
                    Toast.makeText(
                        this@CreatePostActivity,
                        getString(R.string.savedSuccessMsg), Toast.LENGTH_LONG
                    ).show()

                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(
                        this@CreatePostActivity,
                        getString(R.string.toastErrorMsg, it.message), Toast.LENGTH_LONG
                    ).show()
                }
        }
    }

    private fun uploadPostWithImage() {
        // Convert bitmap to JPEG and put it in a byte array
        val baos = ByteArrayOutputStream()
        uploadBitmap?.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val imageInBytes = baos.toByteArray()

        // prepare the empty file in the cloud
        val storageRef = FirebaseStorage.getInstance().reference
        val newImage = URLEncoder.encode(UUID.randomUUID().toString(), "UTF-8") + ".jpg"
        val newImagesRef = storageRef.child("images/$newImage")

        // upload the jpeg byte array to the created empty file
        newImagesRef.putBytes(imageInBytes)
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this@CreatePostActivity,
                    exception.message, Toast.LENGTH_SHORT
                ).show()
                exception.printStackTrace()
            }.addOnSuccessListener {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.

                newImagesRef.downloadUrl.addOnCompleteListener { task -> // the public URL of the image is: task.result.toString()
                    uploadPost(task.result.toString())
                }
            }
    }

    private fun isFormValid(): Boolean {
        return when {
            binding.etTitle.text.isBlank() -> {
                binding.etTitle.error = getString(R.string.nonEmptyErrorMsg)
                false
            }
            else -> true
        }
    }

}


