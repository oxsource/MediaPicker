package pizzk.media.picker.arch

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.annotation.IntDef
import pizzk.media.picker.utils.ImageLoadImpl
import pizzk.media.picker.view.PickActivity

/**
 * 拾取控制器
 */
class PickControl private constructor() {
    companion object {
        const val ACTION_NONE: Int = -1
        const val ACTION_PREVIEW: Int = 0
        const val ACTION_ALBUM: Int = 1
        const val ACTION_CAMERA: Int = 2
        const val ACTION_CROP: Int = 3

        @IntDef(ACTION_PREVIEW, ACTION_ALBUM, ACTION_CAMERA)
        annotation class Action

        //文件权限
        private var authority: String = ""
        private var imageLoad: ImageLoad = ImageLoadImpl

        //默认函数块
        private val dFilter: (Uri?, String) -> Boolean = { _, _ -> true }
        private val dCallback: (List<Uri>) -> Unit = { _ -> Unit }

        private val picker: PickControl = PickControl()

        fun obtain(): PickControl = picker

        fun authority(value: String) {
            this.authority = value
        }

        fun authority() = authority

        fun imageLoad() = imageLoad
    }

    private var action: Int = ACTION_NONE
    private var filter: (Uri?, String) -> Boolean = dFilter
    private var limit: Int = 1
    private var callback: (List<Uri>) -> Unit = dCallback
    //裁剪
    private var crop: CropParams? = null
    //预览
    private var previews: List<Uri> = emptyList()
    private var previewsIndex: Int = 0
    //拍照
    private var cameraUri: Uri? = null

    /**
     * 重置配置属性
     */
    fun clean(): PickControl {
        action = ACTION_NONE
        filter = dFilter
        crop = null
        limit = 1
        callback = dCallback
        this.previews = emptyList()
        return this
    }

    /**
     * 设置操作意图码
     */
    fun action(@Companion.Action value: Int): PickControl {
        this.action = value
        return this
    }

    /**
     * 设置最多可以选取几张图片(至少一张)
     */
    fun limit(value: Int): PickControl {
        this.limit = if (value < 1) 1 else value
        return this
    }

    /**
     * 通过URI和MIME过滤
     */
    fun filter(block: (uri: Uri?, mime: String) -> Boolean): PickControl {
        this.filter = block
        return this
    }

    /**
     * 设置裁剪参数，选择多张图片时不支持裁剪
     */
    fun crop(crop: CropParams?): PickControl {
        this.crop = crop
        return this
    }

    /**
     * 设置预览图片数据
     */
    fun previews(values: List<Uri>, index: Int): PickControl {
        this.previews = values
        this.previewsIndex = index
        return this
    }

    /**
     * 设置拾取回调
     */
    fun callback(block: (uris: List<Uri>) -> Unit): PickControl {
        this.callback = block
        return this
    }

    fun done(activity: Activity) {
        if (ACTION_NONE == action) return
        val intent = Intent(activity, PickActivity::class.java)
        activity.startActivity(intent)
    }

    /**
     * 获取操作码
     */
    internal fun action(): Int = action

    /**
     * 获取过滤器
     */
    internal fun filter(): (Uri?, String) -> Boolean = filter

    /**
     * 获取裁切参数
     */
    internal fun crop(): CropParams? = crop

    /**
     * 获取拾取图片数量限制
     */
    internal fun limit(): Int = limit

    /**
     * 获取回调函数
     */
    internal fun callbacks(): (List<Uri>) -> Unit = callback

    /**
     * 获预览图片路径
     */
    internal fun previews(): List<Uri> = previews

    /**
     * 获预览图片默认索引
     */
    internal fun previewsIndex(): Int = previewsIndex

    /**
     * 获取拍照Uri
     */
    internal fun cameraUri(): Uri? = cameraUri

    /**
     * 暂存拍照Uri
     */
    internal fun cameraUri(uri: Uri?) {
        cameraUri = uri
    }
}