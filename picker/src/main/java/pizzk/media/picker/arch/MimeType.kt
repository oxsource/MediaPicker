package pizzk.media.picker.arch

import android.net.Uri
import android.provider.MediaStore

/**
 * 参考：
 * https://developer.android.com/guide/topics/media/media-formats
 * http://www.w3school.com.cn/media/media_mimeref.asp
 */
enum class MimeType(val mime: String, val extensions: Array<String>) {
    //常用图片格式
    JPEG("image/jpeg", arrayOf("jpg", "jpeg")),
    PNG("image/png", arrayOf("png")),
    GIF("image/gif", arrayOf("gif")),
    BMP("image/x-ms-bmp", arrayOf("bmp")),
    WEBP("image/webp", arrayOf("webp")),

    //常用音频格式
    MP3("audio/mpeg", arrayOf("mp3")),
    WAV("audio/x-wav", arrayOf("wav")),
    OGG("audio/ogg", arrayOf("ogg")),

    //常用视频格式
    MPEG("video/mpeg", arrayOf("mpeg", "mpg")),
    MP4("video/mp4", arrayOf("mp4", "m4v")),
    MOV("video/quicktime", arrayOf("mov")),
    GPP("video/3gpp", arrayOf("3gp", "3gpp")),
    GPP2("video/3gpp2", arrayOf("3g2", "3gpp2")),
    MKV("video/x-matroska", arrayOf("mkv")),
    WEBM("video/webm", arrayOf("webm")),
    TS("video/mp2ts", arrayOf("ts")),
    AVI("video/avi", arrayOf("avi"));

    companion object {
        fun ofImage(): Array<MimeType> = arrayOf(JPEG, PNG, GIF, BMP, WEBP)

        fun ofAudio(): Array<MimeType> = arrayOf(MP3, WAV, OGG)

        fun ofVideo(): Array<MimeType> = arrayOf(MPEG, MP4, MOV, GPP, GPP2, MKV, WEBM, TS, AVI)

        /**
         * 获取不同类型内容的uri
         */
        fun getContentUri(mime: String): Uri {
            if (mime in ofImage().map { it.mime }) {
                return MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
            if (mime in ofAudio().map { it.mime }) {
                return MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
            if (mime in ofVideo().map { it.mime }) {
                return MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }
            val name = "external"
            return MediaStore.Files.getContentUri(name)
        }
    }
}
