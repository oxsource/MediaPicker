package pizzk.media.picker.widget

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Rect
import android.net.Uri
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import pizzk.media.picker.R
import pizzk.media.picker.adapter.CommonListAdapter
import pizzk.media.picker.adapter.PhotoGroupAdapter
import pizzk.media.picker.arch.CropParams
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.PhotoItem
import pizzk.media.picker.utils.PickUtils
import pizzk.media.picker.view.PickChoseActivity

/**
 * 选取一组照片的视图
 */
class PhotoGroupView : RecyclerView {
    private var spacing: Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, androidx.recyclerview.R.attr.recyclerViewStyle)

    @SuppressLint("Recycle")
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
        val attrId = R.styleable.PhotoGroupView
        val ta: TypedArray = context.obtainStyledAttributes(attrs, attrId) ?: return
        spacing = ta.getDimensionPixelOffset(R.styleable.PhotoGroupView_space, spacing)
        ta.recycle()
    }

    private val choiceList: List<String> = listOf(
            context.getString(R.string.pick_chose_camera),
            context.getString(R.string.pick_chose_album)
    )

    fun setup(special: Special,
              exists: List<String>? = null,
              readOnly: Boolean,
              appendText: String = "",
              changed: (PhotoGroupAdapter, Int) -> Unit = { _, _ -> }) {
        post {
            val vWidth = measuredWidth - paddingStart - paddingEnd
            val minWidth = context.resources.getDimensionPixelOffset(R.dimen.pick_photo_min_size)
            val lp: ViewGroup.LayoutParams = ViewGroup.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT)
            do {
                lp.width = (vWidth - (special.column - 1) * spacing) / special.column
                if (lp.width >= minWidth || special.column <= 1) break
                special.column -= 1
            } while (true)
            val manager = object : GridLayoutManager(context, special.column) {
                override fun isAutoMeasureEnabled(): Boolean = true
            }
            this.layoutManager = manager
            addItemDecoration(GridSpacingItemDecoration(special.column, spacing))
            val pAdapter = PhotoGroupAdapter(context, special.fixed, lp)
            pAdapter.setReadOnly(readOnly)
            pAdapter.setChangeBlock(changed)
            pAdapter.setAppendText(appendText)
            if (!pAdapter.update(exists, special.limit, index = -1)) {
                changed(pAdapter, -1)
            }
            this.adapter = pAdapter
            //配置Adapter
            pAdapter.setTapBlock { _, index ->
                val el: PhotoItem = pAdapter.getList()[index]
                if (el.path.isEmpty()) {
                    if (pAdapter.isReadOnly()) return@setTapBlock
                    //选择图片
                    val selects: List<String> = if (pAdapter.isAppend) pAdapter.selectPaths() else emptyList()
                    PickChoseActivity.show(special.activity, choiceList) { key ->
                        showPickPhoto(special.activity, key, selects, special.limit, pAdapter, index, special.crop)
                    }
                } else {
                    //预览
                    val selects: List<String> = if (pAdapter.isAppend) pAdapter.selectPaths() else arrayListOf(el.path)
                    showPreview(special.activity, selects, if (pAdapter.isAppend) index else 0)
                }
            }
            pAdapter.setTapChildBlock { _, _, index, what ->
                if (CommonListAdapter.WHAT0 != what) return@setTapChildBlock
                //移除图片
                pAdapter.delete(index)
            }
        }
    }

    //跳转至预览
    private fun showPreview(activity: Activity, selects: List<String>, index: Int) {
        PickControl.obtain(true)
                .action(PickControl.ACTION_PREVIEW)
                .selects(selects)
                .index(index)
                .done(activity)
    }

    //跳转至选择图片
    private fun showPickPhoto(activity: Activity,
                              key: String,
                              selects: List<String>,
                              limit: Int,
                              adapter: PhotoGroupAdapter,
                              index: Int,
                              crop: CropParams?) {
        val action: Int = when (key) {
            choiceList[0] -> {
                PickControl.ACTION_CAMERA
            }
            choiceList[1] -> {
                PickControl.ACTION_ALBUM
            }
            else -> -1
        }
        if (action < 0) return
        val block: (Int, List<Uri>) -> Unit = { code, list ->
            if (code == PickControl.ACTION_CAMERA) {
                if (adapter.isAppend) {
                    val allOf: MutableList<String> = ArrayList(adapter.selectCount() + 1)
                    allOf.addAll(adapter.selectPaths())
                    allOf.addAll(list.map(Uri::toString))
                    adapter.update(allOf, limit, index)
                } else {
                    adapter.update(list.map(Uri::toString), limit, index)
                }
            } else {
                if (adapter.isAppend) {
                    val remotes: List<String> = selects.filter { null == PickUtils.path2Uri(it) }
                    val allOf: MutableList<String> = ArrayList(limit)
                    allOf.addAll(remotes)
                    allOf.addAll(list.map(Uri::toString))
                    adapter.update(allOf, limit, index)
                } else {
                    adapter.update(list.map(Uri::toString), limit, index)
                }
            }
        }
        PickControl.obtain(clean = true).action(action)
                .selects(selects)
                .limit(limit)
                .crop(crop)
                .callback(block)
                .done(activity)
    }

    //指定参数
    class Special(
            var activity: Activity,
            var limit: Int = 1,
            var column: Int = 4,
            var fixed: MutableList<PhotoItem>? = null,
            var crop: CropParams? = null
    )

    internal class GridSpacingItemDecoration(private val spanCount: Int, private val spacing: Int) : ItemDecoration() {

        override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: State) {
            val position = parent.getChildAdapterPosition(view)
            val column = position % spanCount
            outRect.left = column * spacing / spanCount
            outRect.right = spacing - (column + 1) * spacing / spanCount
            if (position < spanCount) return
            outRect.top = spacing
        }
    }
}