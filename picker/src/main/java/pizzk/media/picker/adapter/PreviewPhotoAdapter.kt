package pizzk.media.picker.adapter

import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.PhotoView
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.source.IMedia
import pizzk.media.picker.source.IMediaSource
import pizzk.media.picker.utils.PickUtils

class PreviewPhotoAdapter(private val context: Context, private val source: IMediaSource) :
    PagerAdapter() {
    private val views: MutableList<View> = ArrayList(5)
    private val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    private var vCurrent: View? = null
    private var clickBlock: (View) -> Unit = { _ -> }
    private var scaleBlock: () -> Unit = { }
    private var vPlaying: View? = null

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun getCount(): Int = source.count()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: View = inflate(context)
        container.addView(view, lp)
        val holder: ViewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)
        val media: IMedia? = source[position]
        val video = media?.mediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        holder.show(video)
        holder.vPhoto.setOnScaleChangeListener { _, _, _ -> scaleBlock() }
        holder.vPhoto.setOnClickListener(clickBlock)
        view.setOnClickListener(if (video) clickBlock else null)
        holder.load(media)
        holder.listenPlaying(video) { vPlaying = view }
        return view
    }

    fun setClickListener(block: (view: View) -> Unit) {
        clickBlock = block
    }

    fun setScaleBlock(block: () -> Unit) {
        scaleBlock = block
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val view: View = obj as View
        if (views.size < 5) views.add(view)
        container.removeView(view)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        vCurrent = obj as? View
    }

    fun resetScale() {
        val view = vCurrent ?: return
        val vPhoto = view.findViewById<PhotoView>(R.id.vPhoto)
        vPhoto.scale = 1.0f
    }

    fun stopPlay() {
        val view = vPlaying ?: return
        val holder: ViewHolder = (view.tag as? ViewHolder) ?: ViewHolder(view)
        holder.vVideo.stopPlayback()
        holder.setPlaying(false)
        vPlaying = null
    }

    fun getPath(position: Int): String = source[position]?.uri()?.toString() ?: ""

    fun get(position: Int): IMedia? = source[position]

    fun indexOf(path: String): Int {
        val uri = PickUtils.path2Uri(path) ?: return -1
        for (index: Int in 0..count) {
            val media = source[index] ?: continue
            if (uri.toString() != media.uri()?.toString()) continue
            return if (index >= count) -1 else index
        }
        return -1
    }

    private fun inflate(context: Context): View {
        if (views.isNotEmpty()) return views.removeAt(0)
        return LayoutInflater.from(context).inflate(R.layout.preview_photo_page_item, null)
    }

    private class ViewHolder(view: View) {
        val vPhoto: PhotoView = view.findViewById(R.id.vPhoto)
        val vVideo: VideoView = view.findViewById(R.id.video)
        val vCover: ImageView = view.findViewById(R.id.vCover)
        val vPause: View = view.findViewById(R.id.vPause)

        init {
            view.tag = this
        }

        fun show(video: Boolean) {
            vPhoto.visibility = if (video) View.GONE else View.VISIBLE
            vVideo.visibility = View.GONE
            arrayOf(vCover, vPause).forEach {
                it.visibility = if (video) View.VISIBLE else View.GONE
            }
        }

        fun load(media: IMedia?) {
            val iml = PickControl.imageLoad()
            val uri = media?.uri() ?: return
            iml.load(vPhoto, uri, media.mimeType())
            iml.load(vCover, uri, media.mimeType())
            vVideo.setVideoURI(uri)
        }

        fun listenPlaying(video: Boolean, callback: () -> Unit) {
            if (!video) return
            vVideo.setOnCompletionListener { setPlaying(false) }
            vVideo.setOnErrorListener { _, _, _ ->
                setPlaying(false)
                return@setOnErrorListener true
            }
            vPause.setOnClickListener {
                callback()
                vVideo.stopPlayback()
                vVideo.start()
                vVideo.resume()
                setPlaying(true)
            }
        }

        fun setPlaying(value: Boolean) {
            vPhoto.visibility = View.GONE
            vVideo.visibility = if (value) View.VISIBLE else View.GONE
            vCover.visibility = if (value) View.GONE else View.VISIBLE
            vPause.visibility = if (value) View.GONE else View.VISIBLE
        }
    }
}