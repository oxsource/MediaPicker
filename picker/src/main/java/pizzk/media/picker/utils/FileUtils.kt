package pizzk.media.picker.utils

import android.app.Application
import android.os.Environment
import android.util.Log
import pizzk.media.picker.arch.PickControl
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    private const val STAMP_FORMAT = "yyyyMMdd_HHmmss"
    private val sdf: SimpleDateFormat = SimpleDateFormat(STAMP_FORMAT, Locale.SIMPLIFIED_CHINESE)

    fun createPhoto(application: Application, ext: String, prefix: String = ""): File? {
        if (Environment.getExternalStorageState() != Environment.MEDIA_MOUNTED) {
            return null
        }
        val parent = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)
        val path = File(parent, application.packageName)
        if (!path.exists() && !path.mkdirs()) {
            Log.d(PickControl.TAG, "create directory failed that path is ${path.absolutePath}")
            return null
        }
        val fileName = "$prefix${sdf.format(Date())}.$ext"
        return File(path, fileName)
    }
}