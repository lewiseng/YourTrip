package com.example.aitforumdemo.ui


import android.R.attr.bitmap
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.provider.MediaStore.Images.Media.getBitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.graphics.drawable.RoundedBitmapDrawable
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.aitforumdemo.R
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.FragmentMapBinding
import com.example.aitforumdemo.main.CreatePostActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*


class MapFragment : Fragment() {


    private var _binding: FragmentMapBinding? = null
    var snapshotListener: ListenerRegistration? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        queryPosts()

        return view
    }



    fun queryPosts(){
        val queryPosts = FirebaseFirestore.getInstance().collection(
            CreatePostActivity.COLLECTION_POSTS
        ).whereNotEqualTo("location", "")

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

                val mapFragment = childFragmentManager
                    .findFragmentById(R.id.map) as SupportMapFragment

                mapFragment.getMapAsync { googleMap ->
                    val budapest = LatLng(47.5070555,19.0450278)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( budapest, 5f))
                    for (docChange in querySnapshot?.documentChanges!!) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val markerInfo = docChange.document.toObject(Post::class.java)
                                val loc = LatLng(markerInfo.latitude.toDouble(), markerInfo.longitude.toDouble())
                                if (markerInfo.latitude.isNotBlank() && markerInfo.longitude.isNotBlank()){
                                    if (markerInfo.imgUrl.isNotBlank()){
                                        Glide.with(requireContext()).asBitmap()
                                            .load(markerInfo.imgUrl)
                                            .into(object : CustomTarget<Bitmap>(){
                                                override fun onResourceReady(result: Bitmap, transition: Transition<in Bitmap>?) {
                                                    val bitmap = Bitmap.createScaledBitmap(result, 150, 150, false)
                                                    val roundedBitmapDrawable: RoundedBitmapDrawable =
                                                        RoundedBitmapDrawableFactory.create(
                                                            resources, bitmap
                                                        )
                                                    val roundPx = bitmap.width * 1f
                                                    roundedBitmapDrawable.cornerRadius = roundPx
                                                    googleMap.addMarker(MarkerOptions().position(loc).title(markerInfo.location))
                                                    ?.setIcon(BitmapDescriptorFactory.fromBitmap(roundedBitmapDrawable.toBitmap()))

                                                }
                                                override fun onLoadCleared(placeholder: Drawable?) {

                                                }
                                            })
                                    }

                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                            }
                            DocumentChange.Type.MODIFIED -> {
                            }
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
