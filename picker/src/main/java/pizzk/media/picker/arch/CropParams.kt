package pizzk.media.picker.arch

import android.graphics.Bitmap
import android.net.Uri
import androidx.annotation.IntDef

/**
 * 裁剪参数
 */
class CropParams(var uri: Uri? = null,
                 var aspectX: Int = 0,
                 var aspectY: Int = 0,
                 var outputX: Int = 0,
                 var outputY: Int = 0,
                 @Formats
                 var format: Int = FORMAT_JPEG) {

    companion object {
        const val FORMAT_JPEG: Int = 1
        const val FORMAT_PNG: Int = 2
        const val FORMAT_WEBP: Int = 3

        @IntDef(FORMAT_JPEG, FORMAT_PNG, FORMAT_WEBP)
        annotation class Formats
    }

    //获取格式
    fun getFormatPlain(): String {
        return when (format) {
            FORMAT_JPEG -> Bitmap.CompressFormat.JPEG.toString()
            FORMAT_PNG -> Bitmap.CompressFormat.PNG.toString()
            FORMAT_WEBP -> Bitmap.CompressFormat.WEBP.toString()
            else -> Bitmap.CompressFormat.JPEG.toString()
        }
    }

    //获取后缀
    fun getFormatExt(): String {
        return when (format) {
            FORMAT_JPEG -> MimeType.JPEG.extensions[0]
            FORMAT_PNG -> MimeType.PNG.extensions[0]
            FORMAT_WEBP -> MimeType.WEBP.extensions[0]
            else -> MimeType.JPEG.extensions[0]
        }
    }
}