package pizzk.media.picker.adapter

import android.content.Context
import android.net.Uri
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.view.ViewGroup
import com.github.chrisbanes.photoview.PhotoView
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.utils.PickUtils

class PreviewPhotoAdapter(private val context: Context, private val list: List<Uri>) : PagerAdapter() {
    private val views: MutableList<PhotoView> = ArrayList(5)
    private val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
    private var currentItem: PhotoView? = null
    private var clickBlock: (View) -> Unit = { _ -> }

    override fun isViewFromObject(view: View, obj: Any): Boolean = view === obj

    override fun getCount(): Int = list.size

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val view: PhotoView = if (views.isEmpty()) {
            PhotoView(context)
        } else {
            views.removeAt(0)
        }
        container.addView(view, lp)
        view.setOnClickListener(clickBlock)
        val uri: Uri = list[position]
        val mime: String = PickUtils.getImageMime(context, uri)
        PickControl.imageLoad().load(view, uri, mime)
        return view
    }

    fun setClickListener(block: (view: View) -> Unit) {
        clickBlock = block
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

    fun getList(): List<Uri> = list
}