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
                val transOptions: DrawableTransitionOptions = DrawableTransitionOptions.withCrossFade()
                Glide.with(context).asGif().load(value).transition(transOptions).into(view)
            }
            MimeType.BMP.mime -> {
                val transOptions: BitmapTransitionOptions = BitmapTransitionOptions.withCrossFade()
                Glide.with(context).asBitmap().load(value).transition(transOptions).into(view)
            }
            else -> {
                val transOptions: DrawableTransitionOptions = DrawableTransitionOptions.withCrossFade()
                Glide.with(context).load(value).transition(transOptions).into(view)
            }
        }
    }
}