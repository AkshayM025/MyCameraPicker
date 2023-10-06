package com.example.mycamerapicker

import MediaPicker
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class MainActivity2 : AppCompatActivity() {

    private lateinit var mediaPicker: MediaPicker
    private val AUTOCOMPLETE_REQUEST_CODE = 111 // Replace with your desired request code


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main2)

        val pickMediaButton: Button = findViewById(R.id.pickMediaButton)

        mediaPicker = MediaPicker(this)

        pickMediaButton.setOnClickListener {
//            showMediaPickerDialog()
            openMap()

        }
        mediaPicker.setOnMediaPickedListener { selectedMediaFiles ->
            // Handle the selected media files here
            for (file in selectedMediaFiles) {
                Log.d(
                    "com.example.mycamerapicker.MediaPicker",
                    "Selected File: ${file.absolutePath}"
                )
            }
        }


    }

    private val mapLauncher =

        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.e("mapDatas>>>>>>>>.", result.resultCode.toString())

            if (result.resultCode == RESULT_OK) {
                val data = result.data
                if (data != null) {
                    val receivedDataLat = data.extras?.getDouble("lat")
                    val receivedDataLng = data.extras?.getDouble("lng")
                    val receivedDataAddress = data.extras?.getDouble("lng")
                    Log.e("mapDatas>>>>>>>>.", receivedDataLat.toString())
                    Log.e("mapDatas>>>>>>>>.", receivedDataLng.toString())

                    // Handle the received data here
                }
            } else {

            }
            // The user canceled the MapActivity
//            }
//            if (result.resultCode == AUTOCOMPLETE_REQUEST_CODE) {
//                val uri = result.data?.data
////                    lat = data.getExtras().getDouble("lat")
////                    lng = data.getExtras().getDouble("lng")
////                    et_location.setText(Tools.getCompleteAddressString(this, lat, lng))
//            }
        }

    private fun openMap() {

// Create an Intent and add the request code as an extra
        val intent = Intent(this, PinLocationPicker::class.java)
        intent.putExtra("requestCode", RESULT_OK)

// Launch the activity using the launcher
        mapLauncher.launch(intent)
    }


    private fun showMediaPickerDialog() {
        val dialog = MediaPickerDialogFragment()
        dialog.setMediaPicker(mediaPicker)
        dialog.show(supportFragmentManager, "com.example.mycamerapicker.MediaPickerDialogFragment")
    }
}
