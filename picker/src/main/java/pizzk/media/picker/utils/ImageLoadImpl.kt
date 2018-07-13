package pizzk.media.picker.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import pizzk.media.picker.arch.ImageLoad
import pizzk.media.picker.arch.MimeType

object ImageLoadImpl : ImageLoad {

    override fun <T> load(view: ImageView, value: T, mime: String) {
        val context: Context = view.context
        when (mime) {
            MimeType.GIF.mime -> {
                Glide.with(context).asGif().load(value).transition(DrawableTransitionOptions.withCrossFade()).into(view)
            }
            MimeType.BMP.mime -> {
                Glide.with(context).asBitmap().load(value).transition(BitmapTransitionOptions.withCrossFade()).into(view)
            }
            else -> {
                Glide.with(context).load(value).transition(DrawableTransitionOptions.withCrossFade()).into(view)
            }
        }
    }
}