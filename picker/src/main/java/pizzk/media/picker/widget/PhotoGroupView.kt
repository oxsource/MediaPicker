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
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.PhotoItem
import pizzk.media.picker.view.PickChoseActivity

/**
 * 选取一组照片的视图
 */
class PhotoGroupView : RecyclerView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    init {
        isVerticalScrollBarEnabled = false
        isHorizontalScrollBarEnabled = false
        overScrollMode = OVER_SCROLL_NEVER
    }

    private val choiceList: List<String> = listOf(
            context.getString(R.string.pick_chose_camera),
            context.getString(R.string.pick_chose_album)
    )

    fun build(special: Special, history: List<String>? = null, changed: (PhotoGroupAdapter) -> Unit = {}) {
        val manager = object : GridLayoutManager(context, special.column) {
            override fun isAutoMeasureEnabled(): Boolean = true
        }
        this.layoutManager = manager
        val pAdapter = PhotoGroupAdapter(context, special.fixed, special.lp)
        pAdapter.setChangeBlock(changed)
        if (null != history && history.isNotEmpty()) {
            pAdapter.update(history, special.limit)
        }
        this.adapter = pAdapter
        //配置Adapter
        PickControl.authority(special.authority)
        pAdapter.setTapBlock { _, index ->
            val el: PhotoItem = pAdapter.getList()[index]
            val selects: List<String> = pAdapter.selects()
            if (el.path.isEmpty()) {
                //选择图片
                PickChoseActivity.show(special.activity, choiceList) { key ->
                    showPickPhoto(special.activity, key, selects, special.limit, pAdapter)
                }
            } else {
                //预览
                showPreview(special.activity, selects, index)
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
                              adapter: PhotoGroupAdapter) {
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
        PickControl.obtain(true).action(action)
                .selects(selects)
                .limit(limit)
                .callback { code, list ->
                    if (code == PickControl.ACTION_CAMERA) {
                        val allOf: MutableList<String> = ArrayList(adapter.selectCount() + 1)
                        allOf.addAll(adapter.selects())
                        allOf.addAll(list.map(Uri::toString))
                        adapter.update(allOf, limit)
                    } else {
                        adapter.update(list.map(Uri::toString), limit)
                    }
                }.done(activity)
    }

    //指定参数
    class Special(
            var activity: Activity,
            var lp: ViewGroup.LayoutParams,
            var limit: Int = 1,
            var authority: String = "",
            var column: Int = 4,
            var fixed: MutableList<PhotoItem>? = null
    )
}