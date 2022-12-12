package com.example.yourtrip.fragments

import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
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
import com.example.yourtrip.data.Post
import com.example.yourtrip.databinding.FragmentMapBinding
import com.example.yourtrip.CreatePostActivity
import com.example.yourtrip.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*


class MapFragment : Fragment() {


    private var _binding: FragmentMapBinding? = null
    private var snapshotListener: ListenerRegistration? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        queryPosts()

        return view
    }


    private fun queryPosts() {
        val queryPosts = FirebaseFirestore.getInstance().collection(
            CreatePostActivity.COLLECTION_POSTS
        ).whereNotEqualTo(getString(R.string.location), "")

        val eventListener = object : EventListener<QuerySnapshot> {
            override fun onEvent(
                querySnapshot: QuerySnapshot?,
                e: FirebaseFirestoreException?
            ) {
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
                    //move map to europe
                    val budapest = LatLng(47.5070555, 19.0450278)
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(budapest, 5f))

                    for (docChange in querySnapshot?.documentChanges!!) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val markerInfo = docChange.document.toObject(Post::class.java)
                                addCustomMarker(markerInfo, googleMap)
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

    private fun addCustomMarker(markerInfo: Post, googleMap: GoogleMap) {
        val loc = LatLng(markerInfo.latitude.toDouble(), markerInfo.longitude.toDouble())
        if (markerInfo.latitude.isNotBlank() && markerInfo.longitude.isNotBlank()) {
            if (markerInfo.imgUrl.isNotBlank()) {
                Glide.with(requireContext()).asBitmap()
                    .load(markerInfo.imgUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            result: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            val roundedDescriptor = bitmapToRoundBitmapDescriptor(result)
                            googleMap.addMarker(
                                MarkerOptions().position(loc).title(markerInfo.location)
                            )
                                ?.setIcon(roundedDescriptor)
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                        }
                    })
            } else {
                googleMap.addMarker(MarkerOptions().position(loc).title(markerInfo.location))
            }
        }

    }

    private fun bitmapToRoundBitmapDescriptor(img: Bitmap): BitmapDescriptor {
        val bitmap = Bitmap.createScaledBitmap(img, 150, 150, false)
        val roundedBitmapDrawable: RoundedBitmapDrawable =
            RoundedBitmapDrawableFactory.create(
                resources, bitmap
            )
        val roundPx = bitmap.width * 1f
        roundedBitmapDrawable.cornerRadius = roundPx
        return BitmapDescriptorFactory.fromBitmap(roundedBitmapDrawable.toBitmap())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        snapshotListener?.remove()
    }


}
