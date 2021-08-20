package pizzk.media.picker.adapter

import android.content.Context
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val view: View = if (views.isEmpty()) {
            LayoutInflater.from(context).inflate(R.layout.preview_photo_page_item, null)
        } else {
            views.removeAt(0)
        }
        container.addView(view, lp)
        view.setOnClickListener(clickBlock)
        //
        val vPhoto = view.findViewById<PhotoView>(R.id.vPhoto)
        vPhoto.setOnScaleChangeListener { _, _, _ -> scaleBlock() }
        val vVideo = view.findViewById<VideoView>(R.id.video)
        val vPause = view.findViewById<View>(R.id.vPause)
        //
        val media: IMedia? = source[position]
        val isVideo = media?.mediaType() == MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO
        vVideo.visibility = View.GONE
        vPause.visibility = if (isVideo) View.VISIBLE else View.GONE
        //
        media ?: return view
        PickControl.imageLoad().load(vPhoto, media.uri(), media.mimeType())
        vVideo.setVideoURI(media.uri())
        vVideo.setOnCompletionListener {
            vVideo.visibility = View.GONE
        }
        vVideo.setOnErrorListener { _, _, _ ->
            vVideo.visibility = View.GONE
            return@setOnErrorListener true
        }
        vPause.setOnClickListener {
            vVideo.stopPlayback()
            vVideo.start()
            vVideo.resume()
            vPlaying = view
            vVideo.visibility = View.VISIBLE
        }
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
        if (vPhoto.scale != 1.0f) vPhoto.scale = 1.0f
    }

    fun stopPlay() {
        val view = vCurrent ?: vPlaying ?: return
        val vVideo = view.findViewById<VideoView>(R.id.video)
        vVideo.stopPlayback()
        vVideo.visibility = View.GONE
        vPlaying = null
    }

    fun getPath(position: Int): String = source[position]?.uri()?.toString() ?: ""

    fun indexOf(path: String): Int {
        val uri = PickUtils.path2Uri(path) ?: return -1
        val media = source.of(uri) ?: return -1
        return media.index()
    }
}