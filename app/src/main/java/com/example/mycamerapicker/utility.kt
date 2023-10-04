package com.example.mycamerapicker
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
 class Utils {


companion object {
    fun uriToFile(context: Context, uri: Uri): File? {
        val contentResolver: ContentResolver = context.contentResolver
        val filePath = getRealPathFromURI(contentResolver, uri)

        if (filePath != null) {
            return File(filePath)
        }

        // If the above method fails, try copying the file to the app's cache directory
        val cacheDir = context.cacheDir
        val file = File(cacheDir, "temp_image_file")

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            val buffer = ByteArray(1024)
            var bytesRead: Int

            while (inputStream?.read(buffer).also { bytesRead = it!! } != -1) {
                outputStream.write(buffer, 0, bytesRead)
            }

            inputStream?.close()
            outputStream.close()
            return file
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }

    private fun getRealPathFromURI(contentResolver: ContentResolver, uri: Uri): String? {
        var realPath: String? = null
        try {
            val cursor = contentResolver.query(uri, null, null, null, null)
            if (cursor != null) {
                cursor.moveToFirst()
                val index = cursor.getColumnIndex("_data")
                if (index != -1) {
                    realPath = cursor.getString(index)
                }
                cursor.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return realPath
    }
}
}