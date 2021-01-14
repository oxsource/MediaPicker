package pizzk.media.picker.adapter

import android.content.Context
import androidx.viewpager.widget.PagerAdapter
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.utils.PickUtils

class PreviewPhotoAdapter(private val context: Context, private val list: List<String>) : PagerAdapter() {
    private val views: MutableList<PhotoView> = ArrayList(5)
    private val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    private var currentItem: PhotoView? = null
    private var clickBlock: (View) -> Unit = { _ -> }
    private var scaleBlock: () -> Unit = { }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun getCount(): Int = list.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: PhotoView = if (views.isEmpty()) {
            PhotoView(context)
        } else {
            views.removeAt(0)
        }
        view.setOnScaleChangeListener { _, _, _ -> scaleBlock() }
        container.addView(view, lp)
        view.setOnClickListener(clickBlock)
        val path: String = list[position]
        val mime: String = PickUtils.getImageMime(context, path)
        PickControl.imageLoad().load(view, path, mime)
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

    fun getList(): List<String> = list
}