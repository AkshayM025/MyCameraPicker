package com.example.mycamerapicker

import android.Manifest.permission
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.mycamerapicker.databinding.ActivityPinLocationBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.io.IOException


class PinLocationPicker : AppCompatActivity(), LocationListener {
    private var binding: ActivityPinLocationBinding? = null
    private var locationManager: LocationManager? = null
    private var location: Location? = null
    private var mMap: GoogleMap? = null
    private var lat = 0.0
    private var lng = 0.0
    private var addressLoc: String? = ""
    private var tvAddress: TextView? = null
    private var myAnim: Animation? = null
    private var imgmarker: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPinLocationBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title=getString(R.string.select_location)
        tvAddress = findViewById<TextView>(R.id.tv_address1)
        imgmarker = findViewById<ImageView>(R.id.img_marker)
        initLocation()
        bindMap()
    }

    private fun initLocation() {
        myAnim = AnimationUtils.loadAnimation(this, R.anim.bounce)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            val arr = arrayOf(permission.ACCESS_FINE_LOCATION, permission.ACCESS_COARSE_LOCATION)
            requestPermissions(arr, 100)
            return
        }
        locationManager!!.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, this)
        location = locationManager!!.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        onLocationChanged(location!!)
        if (Tools.get().isLocationEnabled(this, locationManager!!) && location != null) {
            lat = location!!.latitude
            lng = location!!.longitude
            tvAddress?.text=Tools.getCompleteAddressString(this, location!!.latitude, location!!.longitude

            )
        }
    }

    private fun bindMap() {
        val mapFragment = supportFragmentManager.findFragmentById(R.id.frg) as SupportMapFragment?
        mapFragment!!.getMapAsync { map ->
            mMap = map
            mMap!!.mapType = GoogleMap.MAP_TYPE_NORMAL
            if (location != null) {
                mMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            location!!.latitude, location!!.longitude
                        ), 18.0f
                    )
                )
            }
            mMap!!.setOnCameraIdleListener {
                lat = mMap!!.cameraPosition.target.latitude
                lng = mMap!!.cameraPosition.target.longitude
                tvAddress?.text=Tools.getCompleteAddressString(this, lat, lng)
                imgmarker!!.startAnimation(myAnim)
                imgmarker!!.startAnimation(myAnim)
            }
        }
    }

    fun searchLocation(view: View?) {
        val locationSearch = findViewById<EditText>(R.id.tv_address)
        val location = locationSearch.text.toString()
        var addressList: List<Address>? = null
            val geocoder = Geocoder(this)
            try {
                addressList = geocoder.getFromLocationName(location, 1)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            try {
                if (addressList!!.isNotEmpty()) {
                    val address = addressList[0]
                    val latLng = LatLng(address.latitude, address.longitude)
                    mMap!!.addMarker(MarkerOptions().position(latLng).title(location))
                    lat = address.latitude
                    lng = address.longitude
                    addressLoc = location
                    mMap!!.animateCamera(CameraUpdateFactory.newLatLng(latLng))
                }
            } catch (e: Exception) {
                Log.e("searchLocation:", e.message!!)
            }

    }

    override fun onLocationChanged(loc: Location) {
        location = loc
    }

    override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}
    override fun onProviderEnabled(s: String) {}
    override fun onProviderDisabled(s: String) {}
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        onBackPressed()
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.pin_manu, menu)
        return true
    }

    override fun onBackPressed() {
        if (lat != 0.0) {
            val intent = Intent()
            intent.putExtra("lat", lat)
            intent.putExtra("lng", lng)
            setResult(RESULT_OK, intent)
            finish()
        } else {
            Toast.makeText(this, "Location Not selected", Toast.LENGTH_SHORT).show()
        }
    }
}