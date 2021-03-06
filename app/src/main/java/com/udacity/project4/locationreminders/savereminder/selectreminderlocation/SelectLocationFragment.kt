package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private lateinit var map: GoogleMap
    private var selectedPoi: PointOfInterest? = null

    // For test purpose
    private val defaultPoi: List<PointOfInterest> by inject()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)


        if (!anyPermissionsGranted(LOCATION_FOREGROUND_PERMISSIONS)) {
            requestMissingPermissions(LOCATION_FOREGROUND_PERMISSIONS)
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.savePoi.setOnClickListener {
            onLocationSelected()
        }

        if (defaultPoi.isNotEmpty()) {
            selectedPoi = defaultPoi.first()
        }

        return binding.root
    }

    private fun onLocationSelected() {
        if (selectedPoi != null) {
            _viewModel.selectedPOI.value = selectedPoi
            _viewModel.latitude.value = selectedPoi?.latLng?.latitude
            _viewModel.longitude.value = selectedPoi?.latLng?.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedPoi?.name

            _viewModel.navigationCommand.value =
                NavigationCommand.Back
        } else {
            _viewModel.showSnackBarInt.value = R.string.err_select_location
        }

    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.hybrid_map -> {
            map.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.satellite_map -> {
            map.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        R.id.terrain_map -> {
            map.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style))
        map.setOnPoiClickListener { poi ->
            map.clear()
            val marker = map.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            marker.showInfoWindow()
            selectedPoi = poi
        }

        map.setOnMapClickListener { latLng ->
            map.clear()
            val marker = map.addMarker(MarkerOptions().position(latLng).title(latLng.toString()))
            marker.showInfoWindow()
            selectedPoi = PointOfInterest(latLng, latLng.toString(), latLng.toString())
        }

        map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(37.421944, -122.084444), 10f))

        enableMyLocationIfAllowed()

    }

    @SuppressLint("MissingPermission")
    fun enableMyLocationIfAllowed() {

        if (anyPermissionsGranted(LOCATION_FOREGROUND_PERMISSIONS)) {

            map.isMyLocationEnabled = true

            val fusedClient =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            val location = fusedClient.lastLocation
            location.addOnCompleteListener {
                if (it.isSuccessful) it.result?.let { loc ->
                    map.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(loc.latitude, loc.longitude), 15f
                        )
                    )
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_REQUEST_CODE) {
            if (permissions.filter { LOCATION_FOREGROUND_PERMISSIONS.contains(it) }
                    .any { grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED }) {
                enableMyLocationIfAllowed()
            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }
}
