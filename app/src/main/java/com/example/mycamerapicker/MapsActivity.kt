package com.example.mycamerapicker

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.LocationManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mycamerapicker.databinding.ActivityMapsBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.RectangularBounds
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient
import java.io.IOException


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding
    private lateinit var googleMap: GoogleMap
    private lateinit var placesClient: PlacesClient

    private var currentLocation: LatLng? = null
    private var currentAddress: String? = null

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                // Permission granted, initialize map
                initializeMap()
            } else {
                // Permission denied, handle accordingly (e.g., show a message)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.mapView.onCreate(savedInstanceState)
        binding.mapView.getMapAsync(this)

        binding.searchView.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    performSearch(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Handle search query as it changes (e.g., show autocomplete suggestions)
                return true
            }
        })

        // Initialize the Places API client
        Places.initialize(applicationContext, getString(R.string.google_maps_api_key))
        placesClient = Places.createClient(this)

        // Check and request location permission if needed
        if (checkLocationPermission()) {
            initializeMap()
        } else {
            requestLocationPermission()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.map_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_ok -> {
                // User tapped the "OK" button
                if (currentLocation != null) {
                    val intent = Intent()
                    intent.putExtra("latitude", currentLocation?.latitude)
                    intent.putExtra("longitude", currentLocation?.longitude)
                    intent.putExtra("address", currentAddress)
                    setResult(RESULT_OK, intent)
                    finish()
                } else {
                    Toast.makeText(this, "No location selected", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onBackPressed() {
        if (currentLocation != null) {
            val intent = Intent()
            intent.putExtra("latitude", currentLocation?.latitude)
            intent.putExtra("longitude", currentLocation?.longitude)
            intent.putExtra("address", currentAddress)
            setResult(RESULT_OK, intent)
        }
        super.onBackPressed()
    }

    override fun onMapReady(gMap: GoogleMap) {
        googleMap = gMap ?: return

        // Initialize a location manager to get the user's current location
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        // Check if location permissions are granted
        if (checkLocationPermission()) {
            // Get the last known location
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (location != null) {
                currentLocation = LatLng(location.latitude, location.longitude)

                // Add a marker for the user's current location
                googleMap.addMarker(
                    MarkerOptions().position(currentLocation!!).title("Your Location")
                )
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation!!, 14f))

                // Fetch the address for the current location
                currentAddress = getAddressFromLocation(this, currentLocation!!)
            }
        } else {
            requestLocationPermission()
        }
    }

    private fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
    }

    private fun initializeMap() {
        binding.mapView.onResume() // Initialize the map when permission is granted

        // Continue with map initialization as before
    }

    private fun performSearch(query: String) {
        // Define search bounds based on the current map viewport
        val bounds = googleMap.projection.visibleRegion.latLngBounds

        // Create a request for location predictions based on the query
        val request = FindAutocompletePredictionsRequest.builder()
            .setQuery(query)
            .setLocationBias(RectangularBounds.newInstance(bounds))
            .setTypeFilter(TypeFilter.CITIES)
            .build()

        placesClient.findAutocompletePredictions(request)
            .addOnSuccessListener { response ->
                // Handle the list of prediction results (e.g., display in a dropdown)
                val predictions = response.autocompletePredictions
                for (prediction in predictions) {
                    // Retrieve place details based on prediction ID
                    val placeRequest = FetchPlaceRequest.newInstance(
                        prediction.placeId,
                        listOf(Place.Field.LAT_LNG)
                    )

                    placesClient.fetchPlace(placeRequest)
                        .addOnSuccessListener { fetchPlaceResponse ->
                            val place = fetchPlaceResponse.place
                            val location = place.latLng

                            // Add a marker at the selected location
                            location?.let {
                                MarkerOptions().position(it)
                                    .title(prediction.getPrimaryText(null).toString())
                            }?.let {
                                googleMap.addMarker(
                                    it
                                )
                            }
                            location?.let {
                                CameraUpdateFactory.newLatLngZoom(
                                    it, 14f
                                )
                            }?.let { googleMap.moveCamera(it) }

                            // Set the current location and address
                            currentLocation = location
                            currentAddress = location?.let { getAddressFromLocation(this, it) }
                        }
                        .addOnFailureListener { exception ->
                            // Handle error
                            exception.printStackTrace()
                        }
                }
            }
            .addOnFailureListener { exception ->
                // Handle error
                exception.printStackTrace()
            }
    }

    // Implement the getAddressFromLocation function to fetch the address from coordinates

    // ...

    private fun getAddressFromLocation(context: Context, location: LatLng): String {
        val geocoder = Geocoder(context)
        val addresses: List<Address>
        var addressText = ""

        try {
            addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)!!

            if (addresses.isNotEmpty()) {
                val address = addresses[0]
                val addressParts = mutableListOf<String>()

                // Add address components to the list
                if (address.maxAddressLineIndex >= 0) {
                    for (i in 0..address.maxAddressLineIndex) {
                        addressParts.add(address.getAddressLine(i))
                    }
                }

                // Combine address components into a single string
                addressText = addressParts.joinToString(separator = ", ")
            }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        }

        return addressText
    }

}


