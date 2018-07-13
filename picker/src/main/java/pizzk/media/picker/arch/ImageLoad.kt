package pizzk.media.picker.arch

import android.widget.ImageView

interface ImageLoad {
    fun <T> load(view: ImageView, value: T, mime: String)
}