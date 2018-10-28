package pizzk.media.picker.widget

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ViewGroup
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

    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
    }

    private val choiceList: List<String> = listOf(
            context.getString(R.string.pick_chose_camera),
            context.getString(R.string.pick_chose_album)
    )

    fun setup(special: Special, exists: List<String>? = null, readOnly: Boolean,
              changed: (PhotoGroupAdapter) -> Unit = {}) {
        val manager = object : GridLayoutManager(context, special.column) {
            override fun isAutoMeasureEnabled(): Boolean = true
        }
        this.layoutManager = manager
        val pAdapter = PhotoGroupAdapter(context, special.fixed, special.lp)
        pAdapter.setReadOnly(readOnly)
        pAdapter.setChangeBlock(changed)
        if (!pAdapter.update(exists, special.limit)) {
            changed(pAdapter)
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

    //跳转至预览
    private fun showPreview(activity: Activity, selects: List<String>, index: Int) {
        PickControl.obtain(true)
                .action(PickControl.ACTION_PREVIEW)
                .selects(selects)
                .index(index)
                .done(activity)
    }

    //跳转至选择图片
    private fun showPickPhoto(activity: Activity, key: String,
                              selects: List<String>, limit: Int,
                              adapter: PhotoGroupAdapter, index: Int,
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
        PickControl.obtain(clean = true).action(action)
                .selects(selects)
                .limit(limit)
                .crop(crop)
                .callback { code, list ->
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
                }.done(activity)
    }

    //指定参数
    class Special(
            var activity: Activity,
            var lp: ViewGroup.LayoutParams,
            var limit: Int = 1,
            var column: Int = 4,
            var fixed: MutableList<PhotoItem>? = null,
            var crop: CropParams? = null
    )
}