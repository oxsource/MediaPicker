package pizzk.media.picker.utils

import android.app.Application
import android.os.Environment
import android.util.Log
import pizzk.media.picker.arch.MimeType
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    private const val STAMP_FORMAT = "yyyyMMdd_HHmmss"
    private val sdf: SimpleDateFormat = SimpleDateFormat(STAMP_FORMAT, Locale.SIMPLIFIED_CHINESE)

    fun createPhoto(application: Application, prefix: String = ""): File? {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return null
        }
        val child = "${Environment.DIRECTORY_DCIM}${File.separator}${application.packageName}"
        val path = File(Environment.getExternalStorageDirectory(), child)
        if (!path.exists()) {
            if (!path.mkdirs()) {
                Log.d("FileUtils", "create directory failed that path is ${path.absolutePath}")
                return null
            }
        }
        val fileName = "$prefix${sdf.format(Date())}${MimeType.JPEG.extensions[0]}"
        return File(path, fileName)
    }
}