package com.example.mycamerapicker

import ImagePickerFragment
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import java.io.File

class MainActivity : AppCompatActivity(), ImagePickerFragment.ImagePickerListener {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val buttonShowBottomSheet =findViewById<Button>(R.id.buttonShowBottomSheet)

        buttonShowBottomSheet.setOnClickListener {
            val imagePickerFragment = ImagePickerFragment.newInstance()
            imagePickerFragment.setImageListener(this)
            imagePickerFragment.show(supportFragmentManager, "imagePicker")

        }

    }

    override fun onImageSelected(imageFile: File) {
        Log.e("MainActivity","File-Path $imageFile")
    }
}