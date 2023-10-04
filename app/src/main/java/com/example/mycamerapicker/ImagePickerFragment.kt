import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.mycamerapicker.R
import com.example.mycamerapicker.Utils
import com.example.mycamerapicker.databinding.FragmentImagePickerBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ImagePickerFragment : BottomSheetDialogFragment() {

    private  var binding : FragmentImagePickerBinding? =null
    private val _binding get() = binding!!



    private val cameraRequestCode = 101
    private val galleryRequestCode = 102

    private var imageUri: Uri? = null
    private var listener: ImagePickerListener? = null

    interface ImagePickerListener {
        fun onImageSelected(imageFile: File)
    }

    fun setImageListener(listeners: ImagePickerListener){
        listener=listeners
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
//        return inflater.inflate(R.layout.fragment_image_picker, container, false)
        binding=FragmentImagePickerBinding.inflate(inflater,container,false)
        return binding!!.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

       binding?.buttonCapture?.setOnClickListener { checkCameraPermissionAndCaptureImage() }
       binding?. buttonPickGallery?.setOnClickListener { pickImageFromGallery() }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (targetFragment is ImagePickerListener) {
            listener = targetFragment as ImagePickerListener
        }
    }

    private fun checkCameraPermissionAndCaptureImage() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            captureImage()
        } else {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            cameraRequestCode
        )
    }

    private fun captureImage() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile: File? = try {
            createImageFile()
        } catch (ex: IOException) {
            null
        }
        photoFile?.also {
            val photoURI: Uri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.mycamerapicker.fileprovider",
                it
            )
            imageUri = photoURI
            intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
            startActivityForResult(intent, cameraRequestCode)
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireActivity().getExternalFilesDir(null)
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, galleryRequestCode)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                cameraRequestCode -> {
                    imageUri?.let {
                      val img_file= Utils.uriToFile(requireContext(),it)
                        listener?.onImageSelected(img_file!!)
                        dismiss()
                    }
                }
                galleryRequestCode -> {
                    val selectedImageUri = data?.data
                    selectedImageUri?.let {
                        val img_file= Utils.uriToFile(requireContext(),it)
                        listener?.onImageSelected(img_file!!)
                        dismiss()
                    }
                }
            }
        }
    }

    companion object {
        fun newInstance(): ImagePickerFragment {
            return ImagePickerFragment()
        }
    }
}
