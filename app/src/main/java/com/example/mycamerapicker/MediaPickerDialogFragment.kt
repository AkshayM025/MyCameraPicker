package com.example.mycamerapicker

import MediaPicker
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment

class MediaPickerDialogFragment : DialogFragment() {

    private lateinit var mediaPicker: MediaPicker

    fun setMediaPicker(mediaPicker: MediaPicker) {
        this.mediaPicker = mediaPicker
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val builder = AlertDialog.Builder(requireActivity())
        builder.setTitle("Select Media Options")
        val options = arrayOf("Single Selection", "Multiple Selection")

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // Single Selection
                    mediaPicker.showMediaPickerDialog(singleSelection = true)
                }
                1 -> {
                    // Multiple Selection
                    mediaPicker.showMediaPickerDialog(singleSelection = false)
                }
            }
        }

        builder.setNegativeButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        return builder.create()
    }
}
