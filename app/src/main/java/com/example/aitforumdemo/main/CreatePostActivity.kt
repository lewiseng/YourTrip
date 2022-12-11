package com.example.aitforumdemo.main

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.location.Address
import android.location.Geocoder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.aitforumdemo.adapters.PostsAdapter
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.ActivityCreatePostBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.IOException

class CreatePostActivity : AppCompatActivity() {

    companion object {
        const val COLLECTION_POSTS = "posts"
        const val REQUEST_CAMERA_PERMISSION = 1001
    }

    private lateinit var binding: ActivityCreatePostBinding
    private var isEditMode = false
    var doubleLat: Double = 0.0
    var doubleLong: Double = 0.0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityCreatePostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (intent.hasExtra(PostsAdapter.DOC_ID))
        {
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

        binding.btnSend.setOnClickListener {
            getCoordinates(this)
            uploadPost()
        }

        binding.btnAttach.setOnClickListener {
            openCamera()
        }

        requestNeededPermission()
    }

    var uploadBitmap: Bitmap? = null

    var resultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result ->
        if (result.resultCode == Activity.RESULT_OK){
            val data: Intent? = result.data
            uploadBitmap = data!!.extras!!.get("data") as Bitmap
            binding.imgAttach.setImageBitmap(uploadBitmap)
            binding.imgAttach.visibility = View.VISIBLE
        }
    }

    fun openCamera() {
        val intentPhoto = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        resultLauncher.launch(intentPhoto)
    }

    private fun requestNeededPermission() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.CAMERA)) {
                Toast.makeText(this,
                    "I need it for camera", Toast.LENGTH_SHORT).show()
            }

            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.CAMERA),
                REQUEST_CAMERA_PERMISSION
            )

        } else {
            // we already have permission
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            REQUEST_CAMERA_PERMISSION -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "CAMERA perm granted", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "CAMERA perm NOT granted", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getCoordinates(context: Context) {
        val geocoder = Geocoder(this)
        val addressList: List<Address>
        try {
            addressList = geocoder.getFromLocationName(binding.etAddress.text.toString(), 1)
            Log.d("myTagAddressList", addressList.toString())
            if (addressList != null) {
                doubleLat = addressList[0].getLatitude()
                doubleLong= addressList[0].getLongitude()
                Log.d("myTagLat", doubleLat.toString())
                Log.d("myTagLong", doubleLong.toString())
            }
        } catch (e: IOException) {
            Log.d("addressError", e.toString())
        }
    }

    private fun uploadPost() {
        if (isEditMode) {
            // update firebase values here
            var doc = FirebaseFirestore.getInstance().collection(CreatePostActivity.COLLECTION_POSTS)
            doc.document(intent.getStringExtra(PostsAdapter.DOC_ID).toString())
//            doc.document("RtAiVgBqJDcdD9FznkoQ")
                .update(
                "title", binding.etTitle.text.toString(),
                    "location", binding.etAddress.text.toString(),
                "body", binding.etBody.text.toString(),
                "latitude", doubleLat.toString(),
                "longitude", doubleLong.toString())
                .addOnSuccessListener {
                    Toast.makeText(this@CreatePostActivity,
                        "Post UPDATED", Toast.LENGTH_LONG).show()
                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@CreatePostActivity,
                        "Error ${it.message}", Toast.LENGTH_LONG).show()
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
                ""
            )

            // "connect" to posts collection (table)
            val postsCollection =
                FirebaseFirestore.getInstance().collection(
                    COLLECTION_POSTS
                )
            postsCollection.add(newPost)
                .addOnSuccessListener {
                    Toast.makeText(this@CreatePostActivity,
                        "Post SAVED", Toast.LENGTH_LONG).show()

                    finish()
                }
                .addOnFailureListener {
                    Toast.makeText(this@CreatePostActivity,
                        "Error ${it.message}", Toast.LENGTH_LONG).show()
                }
        }

    }

}


