package pizzk.media.picker.arch

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.support.annotation.IntDef
import pizzk.media.picker.utils.ImageLoadImpl
import pizzk.media.picker.view.PickActivity
import java.io.File

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
        private val dCallback: (action: Int, List<Uri>) -> Unit = { _, _ -> Unit }

        private val picker: PickControl = PickControl()

        fun obtain(clean: Boolean): PickControl = if (clean) picker.clean() else picker

        fun authority(value: String) {
            this.authority = value
        }

        fun authority() = authority

        fun imageLoad() = imageLoad
    }

    private var action: Int = ACTION_NONE
    private var filter: (Uri?, String) -> Boolean = dFilter
    private var limit: Int = 1
    private var callback: (action: Int, List<Uri>) -> Unit = dCallback
    //裁剪
    private var crop: CropParams? = null
    private var cropFile: File? = null

    //选中数据
    private var selects: List<String> = emptyList()
    private var index: Int = 0
    //拍照
    private var cameraFile: File? = null

    /**
     * 重置配置属性
     */
    fun clean(): PickControl {
        action = ACTION_NONE
        filter = dFilter
        crop = null
        limit = 1
        callback = dCallback
        selects = emptyList()
        index = 0
        cropFile = null
        cameraFile = null
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
     * 设置已经选择的数据
     */
    fun selects(values: List<String>): PickControl {
        this.selects = values
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
     * 设置选中图片索引
     */
    fun index(index: Int): PickControl {
        this.index = index
        return this
    }

    /**
     * 设置拾取回调
     */
    fun callback(block: (action: Int, uris: List<Uri>) -> Unit): PickControl {
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
    internal fun callbacks(): (action: Int, uris: List<Uri>) -> Unit = callback

    /**
     * 获预选择图片路径
     */
    internal fun selects(): List<String> = selects

    /**
     * 获取图片默认索引
     */
    internal fun index(): Int = index

    /**
     * 获取拍照File
     */
    internal fun cameraFile(): File? = cameraFile

    /**
     * 暂存拍照File
     */
    internal fun cameraFile(file: File?) {
        cameraFile = file
    }

    /**
     * 获取裁切File
     */
    internal fun cropFile(): File? = cropFile

    /**
     * 暂存裁切File
     */
    internal fun cropFile(file: File?) {
        cropFile = file
    }
}