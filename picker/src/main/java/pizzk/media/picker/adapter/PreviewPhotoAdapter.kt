package pizzk.media.picker.adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.github.chrisbanes.photoview.PhotoView
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.source.IMedia
import pizzk.media.picker.source.IMediaSource
import pizzk.media.picker.utils.PickUtils

class PreviewPhotoAdapter(private val context: Context, private val source: IMediaSource) :
    PagerAdapter() {
    private val views: MutableList<PhotoView> = ArrayList(5)
    private val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT
    )
    private var currentItem: PhotoView? = null
    private var clickBlock: (View) -> Unit = { _ -> }
    private var scaleBlock: () -> Unit = { }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun getCount(): Int = source.count()

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: PhotoView = if (views.isEmpty()) {
            PhotoView(context)
        } else {
            views.removeAt(0)
        }
        view.setOnScaleChangeListener { _, _, _ -> scaleBlock() }
        container.addView(view, lp)
        view.setOnClickListener(clickBlock)
        val media: IMedia = source[position] ?: return view
        PickControl.imageLoad().load(view, media.uri(), media.mimeType())
        return view
    }

    fun setClickListener(block: (view: View) -> Unit) {
        clickBlock = block
    }

    fun setScaleBlock(block: () -> Unit) {
        scaleBlock = block
    }

    override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
        val view: PhotoView = obj as PhotoView
        container.removeView(view)
        views.add(view)
    }

    override fun setPrimaryItem(container: ViewGroup, position: Int, obj: Any) {
        super.setPrimaryItem(container, position, obj)
        currentItem = obj as? PhotoView
    }

    fun getPrimaryItem(): PhotoView? = currentItem

    fun getPath(position: Int): String = source[position]?.uri()?.toString() ?: ""

    fun indexOf(path: String): Int {
        val uri = PickUtils.path2Uri(path) ?: return -1
        val media = source.of(uri) ?: return -1
        return media.index()
    }
}