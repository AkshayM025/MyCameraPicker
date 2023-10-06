package com.example.mycamerapicker

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.net.Uri
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.intuit.sdp.BuildConfig
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale


class Tools {
    fun changeTimeFormat(givenDateString: String?): String {
        val sdf = SimpleDateFormat("HH:mm")
        val new_for = SimpleDateFormat("HH:mm a")
        return try {
            val mDate = sdf.parse(givenDateString)
            new_for.format(mDate)
        } catch (e: ParseException) {
            e.printStackTrace()
            ""
        }
    }

    fun shareApp(context: Context) {
        try {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.app_name))
            var shareMessage = "\nLet me recommend you this application\n\n"
            shareMessage =
                """
                ${shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID}
                
                
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            context.startActivity(Intent.createChooser(shareIntent, "choose one"))
        } catch (e: Exception) {
            //e.toString();
        }
    }

    fun launchMarket(context: Context) {
        val uri = Uri.parse("market://details?id=" + BuildConfig.APPLICATION_ID)
        val myAppLinkToMarket = Intent(Intent.ACTION_VIEW, uri)
        try {
            context.startActivity(myAppLinkToMarket)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(context, " unable to find market app", Toast.LENGTH_LONG).show()
        }
    }

    fun isLocationEnabled(mContext: Context, locationManager: LocationManager): Boolean {
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            val alertDialog = AlertDialog.Builder(mContext)
            alertDialog.setTitle("Enable Location")
            alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.")
            alertDialog.setPositiveButton(
                "Location Settings"
            ) { _, _ ->
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                mContext.startActivity(intent)
            }
            alertDialog.setNegativeButton(
                "Cancel"
            ) { dialog, _ -> dialog.cancel() }
            val alert = alertDialog.create()
            alert.show()
            return false
        }
        return true
    }

    fun openGallery(context: Activity, code: Int) {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        context.startActivityForResult(galleryIntent, code)
    }

    val year: ArrayList<String>
        get() {
            val list = ArrayList<String>()
            for (i in 1990..2019) {
                list.add("" + i)
            }
            return list
        }

    enum class Type {
        DATE, TIME
    }

    companion object {
        fun get(): Tools {
            return Tools()
        }

        fun getCompleteAddressString(
            context: Context?,
            LATITUDE: Double,
            LONGITUDE: Double
        ): String {
            var strAdd = "getting address..."
            if (context != null) {
                val geocoder = Geocoder(context.applicationContext, Locale.getDefault())
                try {
                    val addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1)
                    if (addresses != null) {
                        val returnedAddress = addresses[0]
                        val strReturnedAddress = StringBuilder()
                        for (i in 0..returnedAddress.maxAddressLineIndex) {
                            strReturnedAddress.append(returnedAddress.getAddressLine(i))
                                .append("\n")
                        }
                        strAdd = strReturnedAddress.toString()
                        Log.w("My Current address", strReturnedAddress.toString())
                    } else {
                        strAdd = "No Address Found"
                        Log.w("My Current address", "No Address returned!")
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    strAdd = "Cant get Address"
                    Log.w("My Current address", "Canont get Address!")
                }
            }
            return strAdd
        }

        fun isValidEmail(target: CharSequence?): Boolean {
            return !TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches()
        }

        fun changeDateFormat(format: String?, date: String?): String {
            val old_format = SimpleDateFormat("yyyy-MM-dd")
            val new_format = SimpleDateFormat(format)
            var newDate: Date? = null
            try {
                newDate = old_format.parse(date)
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return new_format.format(newDate)
        }

        fun datePicker(context: Context?, listener: OnDataListener) {
            val myCalendar = Calendar.getInstance()
            val date =
                OnDateSetListener { view, year, monthOfYear, dayOfMonth -> // TODO Auto-generated method stub
                    myCalendar[Calendar.YEAR] = year
                    myCalendar[Calendar.MONTH] = monthOfYear
                    myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                    val myFormat = "yyyy-MM-dd" // your format
                    val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
                    listener.selectedData(sdf.format(myCalendar.time))
                }
            DatePickerDialog(
                context!!, date,
                myCalendar[Calendar.YEAR],
                myCalendar[Calendar.MONTH], myCalendar[Calendar.DAY_OF_MONTH]
            ).show()
        }

        fun makeCall(context: Context, phone: String?) {
            val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", phone, null))
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            context.startActivity(intent)
        }

        @SuppressLint("SimpleDateFormat")
        fun getCurrent(type: Type): String {
            var cd = ""
            val c = Calendar.getInstance().time
            val df = SimpleDateFormat("dd-MMM-yyyy")
            val cf = SimpleDateFormat("HH:mm a")
            cd = if (type == Type.DATE) df.format(c) else cf.format(c)
            return cd
        }

        fun HideKeyboard(context: Context, view: View) {
            val inputManager =
                context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }

        fun getTimeAgo(crdate: String?): String? {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
            // get current date time with Calendar()
            val cal = Calendar.getInstance()
            val currenttime = dateFormat.format(cal.time)
            var CreatedAt: Date? = null
            var current: Date? = null
            try {
                CreatedAt = dateFormat.parse(crdate)
                current = dateFormat.parse(currenttime)
            } catch (e: ParseException) {
                // TODO Auto-generated catch block
                e.printStackTrace()
            }

            // Get msec from each, and subtract.
            val diff = current!!.time - CreatedAt!!.time
            val diffSeconds = diff / 1000
            val diffMinutes = diff / (60 * 1000) % 60
            val diffHours = diff / (60 * 60 * 1000) % 24
            val diffDays = diff / (24 * 60 * 60 * 1000)
            var time: String? = null
            if (diffDays > 0) {
                time = if (diffDays == 1L) {
                    "$diffDays day ago "
                } else {
                    "$diffDays days ago "
                }
            } else {
                if (diffHours > 0) {
                    time = if (diffHours == 1L) {
                        "$diffHours hr ago"
                    } else {
                        "$diffHours hrs ago"
                    }
                } else {
                    if (diffMinutes > 0) {
                        time = if (diffMinutes == 1L) {
                            "$diffMinutes min ago"
                        } else {
                            "$diffMinutes mins ago"
                        }
                    } else {
                        if (diffSeconds > 0) {
                            time = "$diffSeconds secs ago"
                        }
                    }
                }
            }
            return time
        }

        fun TimePicker(context: Context?, listene: OnDataListener) {
            val mcurrentTime = Calendar.getInstance()
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                context,
                { timePicker, selectedHour, selectedMinute ->
                    listene.selectedData(
                        getTimeInMillSec(
                            "$selectedHour:$selectedMinute"
                        )
                    )
                }, hour, minute, true
            )
            mTimePicker.setTitle("Set arrival time")
            mTimePicker.show()
        }

        fun TimePicker(
            context: Context?,
            listene: OnDataListener,
            is12hour: Boolean,
            previous_time: Boolean
        ) {
            val mcurrentTime = Calendar.getInstance()
            val c = Calendar.getInstance()
            val hour = mcurrentTime[Calendar.HOUR_OF_DAY]
            val minute = mcurrentTime[Calendar.MINUTE]
            val mTimePicker: TimePickerDialog
            mTimePicker = TimePickerDialog(
                context,
                { timePicker, selectedHour, selectedMinute ->
                    if (!previous_time) {
                        if (selectedHour > hour) {
                            val hours = selectedHour % 12
                            listene.selectedData(
                                String.format(
                                    "%02d:%02d %s", if (hours == 0) 12 else hours,
                                    selectedMinute, if (selectedHour < 12) "am" else "pm"
                                )
                            )
                        } else if (selectedHour == hour && selectedMinute >= minute) {
                            val hours = selectedHour % 12
                            listene.selectedData(
                                String.format(
                                    "%02d:%02d %s", if (hours == 0) 12 else hours,
                                    selectedMinute, if (selectedHour < 12) "am" else "pm"
                                )
                            )
                        } else {
                            listene.selectedData(
                                String.format(
                                    "%02d:%02d %s", if (hour == 0) 12 else hour,
                                    minute, if (hour < 12) "am" else "pm"
                                )
                            )
                        }
                    } else {
                        val hours = selectedHour % 12
                        listene.selectedData(
                            String.format(
                                "%02d:%02d %s", if (hours == 0) 12 else hours,
                                selectedMinute, if (selectedHour < 12) "am" else "pm"
                            )
                        )
                    }
                }, hour, minute, is12hour
            )
            mTimePicker.setTitle("Set arrival time")
            mTimePicker.show()
        }

        fun getTimeInMillSec(givenDateString: String?): String {
            var timeInMilliseconds: Long = 0
            val sdf = SimpleDateFormat("HH:mm")
            try {
                val mDate = sdf.parse(givenDateString)
                timeInMilliseconds = mDate.time
                println("Date in milli :: $timeInMilliseconds")
            } catch (e: ParseException) {
                e.printStackTrace()
            }
            return timeInMilliseconds.toString()
        }
    }
}