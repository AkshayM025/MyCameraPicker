package com.example.mycamerapicker

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment

class MediaPicker(private val fragment: Fragment) {

    private var onMediaPickedListener: ((List<Uri>) -> Unit)? = null

    fun setOnMediaPickedListener(listener: (List<Uri>) -> Unit) {
        onMediaPickedListener = listener
    }

    private val imageVideoPicker =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedMediaUris = mutableListOf<Uri>()
                val selectedMediaClipData = result.data?.clipData

                if (selectedMediaClipData != null) {
                    // Multiple items were selected
                    for (i in 0 until selectedMediaClipData.itemCount) {
                        val uri = selectedMediaClipData.getItemAt(i).uri
                        selectedMediaUris.add(uri)
                    }
                } else {
                    // Single item was selected
                    val uri = result.data?.data
                    if (uri != null) {
                        selectedMediaUris.add(uri)
                    }
                }

                onMediaPickedListener?.invoke(selectedMediaUris)
            }
        }

    fun pickImagesAndVideos() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/* video/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        imageVideoPicker.launch(intent)
    }
}
