package pizzk.media.picker.view

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.AttributeSet
import android.view.ViewGroup
import pizzk.media.picker.adapter.PickListAdapter
import pizzk.media.picker.adapter.PickPhotoGroupAdapter
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.PhotoItem
import pizzk.media.picker.utils.PickUtils

/**
 * 选取一组照片的视图
 */
class PickPhotoGroupView : RecyclerView {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    fun build(special: Special, history: List<String>? = null, changed: (PickPhotoGroupAdapter) -> Unit = {}) {
        val manager = object : GridLayoutManager(context, special.column) {
            override fun isAutoMeasureEnabled(): Boolean = true
        }
        this.layoutManager = manager
        val pAdapter = PickPhotoGroupAdapter(context, special.fixed, special.lp)
        pAdapter.setChangeBlock(changed)
        if (null != history && history.isNotEmpty()) {
            pAdapter.update(history, special.limit)
        }
        this.adapter = pAdapter
        //配置Adapter
        PickControl.authority(special.authority)
        pAdapter.setTapBlock { _, index ->
            val el: PhotoItem = pAdapter.getList()[index]
            val selects: List<Uri> = pAdapter.selects().mapNotNull(PickUtils::parsePath)
            if (el.path.isEmpty()) {
                //选择图片
                showPickPhoto(special.activity, false, selects, special.limit, pAdapter)
            } else {
                //预览
                showPreview(special.activity, selects, index)
            }
        }
        pAdapter.setTapChildBlock { _, _, index, what ->
            if (PickListAdapter.WHAT0 != what) return@setTapChildBlock
            //移除图片
            pAdapter.delete(index)
        }
    }

    //跳转至预览
    private fun showPreview(activity: Activity, selects: List<Uri>, index: Int) {
        PickControl.obtain(true)
                .action(PickControl.ACTION_PREVIEW)
                .selects(selects)
                .index(index)
                .done(activity)
    }

    //跳转至选择图片
    private fun showPickPhoto(activity: Activity, camera: Boolean,
                              selects: List<Uri>, limit: Int,
                              adapter: PickPhotoGroupAdapter) {
        val action: Int = if (camera) PickControl.ACTION_CAMERA else PickControl.ACTION_ALBUM
        PickControl.obtain(true).action(action)
                .selects(selects)
                .limit(limit)
                .callback {
                    adapter.update(it.map(Uri::toString), limit)
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