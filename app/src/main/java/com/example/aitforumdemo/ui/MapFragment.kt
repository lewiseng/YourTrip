package com.example.aitforumdemo.ui


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.aitforumdemo.R
import com.example.aitforumdemo.data.Post
import com.example.aitforumdemo.databinding.FragmentMapBinding
import com.example.aitforumdemo.main.CreatePostActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.*


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private lateinit var googleMap: GoogleMap
    private var mapView: MapView? = null
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
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom( budapest, 1f))
                    for (docChange in querySnapshot?.documentChanges!!) {
                        when (docChange.type) {
                            DocumentChange.Type.ADDED -> {
                                val marker = docChange.document.toObject(Post::class.java)
                                val sydney = LatLng(marker.latitude.toDouble(), marker.longitude.toDouble())
                                googleMap.addMarker(MarkerOptions().position(sydney).title(marker.location))
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