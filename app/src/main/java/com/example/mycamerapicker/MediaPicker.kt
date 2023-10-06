import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MediaPicker(private val context: AppCompatActivity) {

    private var onMediaPickedListener: ((List<File>) -> Unit)? = null

    fun setOnMediaPickedListener(listener: (List<File>) -> Unit) {
        onMediaPickedListener = listener
    }

    private val imageVideoPicker =
        context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val selectedMediaFiles = mutableListOf<File>()
                val selectedMediaClipData = result.data?.clipData

                if (selectedMediaClipData != null) {
                    // Multiple items were selected
                    for (i in 0 until selectedMediaClipData.itemCount) {
                        val uri = selectedMediaClipData.getItemAt(i).uri
                        val file = uriToFile(uri)
                        if (file != null) {
                            selectedMediaFiles.add(file)
                        }
                    }
                } else {
                    // Single item was selected
                    val uri = result.data?.data
                    if (uri != null) {
                        val file = uriToFile(uri)
                        if (file != null) {
                            selectedMediaFiles.add(file)
                        }
                    }
                }

                onMediaPickedListener?.invoke(selectedMediaFiles)
            }
        }

    private val cameraPicker =

        context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val uri = result.data?.data
                Log.e("cameraPath", uri.toString())

                if (uri != null) {
                    val file = uriToFile(uri)
                    if (file != null) {
                        Log.e("cameraPath", file.path)
                        onMediaPickedListener?.invoke(listOf(file))
                    }
                }else{
                    Log.e("cameraPath","null")

                }
            }
        }

    private fun uriToFile(uri: Uri): File? {
        val cursor = context.contentResolver.query(uri, null, null, null, null)
        return cursor?.use {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            it.moveToFirst()
            val name = it.getString(nameIndex)
            val file = File(context.cacheDir, name)
            val outputStream = file.outputStream()

            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                inputStream?.copyTo(outputStream)
                inputStream?.close()
                outputStream.close()
                file
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    fun showMediaPickerDialog(singleSelection: Boolean) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Select Media Source")
        val options = arrayOf("Camera", "Gallery")

        builder.setItems(options) { _, which ->
            when (which) {
                0 -> {
                    // Camera
                    if (checkCameraPermission()) {
                        openCamera()
                    } else {
                        requestCameraPermission()
                    }
                }

                1 -> {
                    // Gallery
                    val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "image/* video/*"
                        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, !singleSelection)
                    }
                    imageVideoPicker.launch(intent)
                }
            }
        }

        builder.setPositiveButton("Cancel") { dialog: DialogInterface, _: Int ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun checkCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            context as? Activity ?: throw IllegalArgumentException("Context must be an Activity"),
            arrayOf(android.Manifest.permission.CAMERA),
            CAMERA_PERMISSION_REQUEST_CODE
        )
    }

    fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == CAMERA_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                // Handle the case where camera permission is denied
                Log.e("Camera Permission", "Permission denied")
            }
        }
    }

    private fun saveMediaToFile(uri: Uri): File? {
        val contentResolver = context.contentResolver
        val fileName = "media_${System.currentTimeMillis()}"
        val fileExtension = getFileExtension(contentResolver, uri)

        val outputDir = context.externalCacheDir
        val outputFile = File(outputDir, "$fileName$fileExtension")

        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                outputFile.outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            return outputFile
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String =
            SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = context.getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun getFileExtension(
        contentResolver: android.content.ContentResolver,
        uri: Uri
    ): String {
        val mimeType = contentResolver.getType(uri)
        return when (mimeType) {
            "image/jpeg" -> ".jpg"
            "image/png" -> ".png"
            "video/mp4" -> ".mp4"
            // Add more supported MIME types and file extensions as needed
            else -> ""
        }
    }

    private fun openCamera() {
        val captureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                it
            )
            var imageUri = photoURI
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            cameraPicker.launch(captureIntent)
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 1001
    }
}
