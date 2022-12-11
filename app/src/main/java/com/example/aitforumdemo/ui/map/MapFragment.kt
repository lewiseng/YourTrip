package com.example.aitforumdemo.ui.map


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.aitforumdemo.R
import com.example.aitforumdemo.databinding.FragmentMapBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.MarkerOptions


class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private lateinit var googleMap: GoogleMap
    private var mapView: MapView? = null


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val mapViewModel =
            ViewModelProvider(this).get(MapViewModel::class.java)

//        _binding = FragmentMapBinding.inflate(inflater, container, false)
//        val root: View = binding.root

//        val textView: TextView = binding.textNotifications
//        mapViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

//        //new
        val view: View = inflater.inflate(R.layout.fragment_map, container, false)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            // When map is loaded
            googleMap.setOnMapClickListener { latLng -> // When clicked on map
                // Initialize marker options
                val markerOptions = MarkerOptions()
                // Set position of marker
                markerOptions.position(latLng)
                // Set title of marker
                markerOptions.title("Title")
                // Remove all marker
                googleMap.clear()
                // Animating to zoom the marker
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
                // Add marker on map
                googleMap.addMarker(markerOptions)
            }
        };



        return view
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}