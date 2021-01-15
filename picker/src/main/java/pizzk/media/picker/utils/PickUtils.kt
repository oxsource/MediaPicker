package pizzk.media.picker.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.View
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.arch.CropParams
import pizzk.media.picker.arch.MimeType
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem
import pizzk.media.picker.entity.AlbumSection
import pizzk.media.picker.view.AlbumActivity
import pizzk.media.picker.view.PreviewActivity
import java.io.File


/**
 * 系统工具类
 */
object PickUtils {
    const val REQUEST_CODE_CAMERA: Int = 100
    const val REQUEST_CODE_PREVIEW: Int = 101
    const val REQUEST_CODE_ALBUM: Int = 102
    const val REQUEST_CODE_CROP: Int = 103

    //返回结果标志
    private const val KEY_RESULT_DATA: String = "key_result_data"
    private const val KEY_FINISH_FLAG: String = "key_finish_flag"
    private const val KEY_NAVIGATION_FLAG: String = "key_navigation_flag"

    private val cameraPermission: Array<String> = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    private val externalPermission: Array<String> = arrayOf(
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    )

    /**
     * 检查权限
     */
    private fun checkPermission(activity: Activity, ps: Array<String>, requestCode: Int): Boolean {
        val notGrant: MutableList<String> = ArrayList()
        for (i: Int in 0 until ps.size) {
            val p: String = ps[i]
            val auth: Boolean = ActivityCompat.checkSelfPermission(activity, p) == PackageManager.PERMISSION_GRANTED
            if (auth) continue
            notGrant.add(p)
        }
        if (notGrant.isEmpty()) return true
        ActivityCompat.requestPermissions(activity, notGrant.toTypedArray(), requestCode)
        return false
    }

    /**
     * 检查权限回调
     */
    fun onRequestPermissionResult(activity: Activity, ps: Array<out String>, grants: IntArray): Boolean {
        for (i: Int in 0 until ps.size) {
            if (PackageManager.PERMISSION_GRANTED == grants[i]) continue
            val permission: String = ps[i]
            val rationale: Boolean = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
            val refuseHint: String = activity.getString(R.string.permission_refuse)
            val message: String = when (permission) {
                Manifest.permission.CAMERA -> {
                    if (rationale) refuseHint else activity.getString(R.string.open_camera_permission)
                }
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE -> {
                    if (rationale) refuseHint else activity.getString(R.string.open_external_storage_permission)
                }
                else -> refuseHint
            }
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    /**
     * 启动相册
     */
    fun launchAlbum(activity: AppCompatActivity) {
        val access: Boolean = checkPermission(activity, externalPermission, REQUEST_CODE_ALBUM)
        if (!access) return
        val picker: PickControl = PickControl.obtain(false)
        val uris: List<Uri> = picker.selects().mapNotNull(PickUtils::path2Uri)
        val invalidCount: Int = picker.selects().size - uris.size
        //去除非本地的图片计数
        val limit: Int = picker.limit() - invalidCount
        AlbumActivity.show(activity, limit, uris)
    }

    /**
     * 启动相机
     */
    fun launchCamera(activity: AppCompatActivity): Uri? {
        //权限确认
        val access: Boolean = checkPermission(activity, cameraPermission, REQUEST_CODE_CAMERA)
        if (!access) return null
        //创建文件
        val optionalFile: File? = FileUtils.createPhoto(activity.application, MimeType.JPEG.extensions[0])
        if (null == optionalFile) {
            Toast.makeText(activity, activity.getString(R.string.pick_media_fail_to_create_file), Toast.LENGTH_SHORT).show()
            activity.finish()
        }
        //启动相机
        val file: File = optionalFile ?: return null
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val uri: Uri = FileProvider.getUriForFile(activity, PickControl.authority(), file)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        activity.startActivityForResult(intent, REQUEST_CODE_CAMERA)
        PickControl.obtain(false).cameraFile(file)
        return uri
    }

    /**
     * 启动预览
     */
    fun launchPreview(activity: AppCompatActivity) {
        val picker: PickControl = PickControl.obtain(false)
        val uris: List<String> = picker.selects()
        val index: Int = picker.index()
        val selectLimit = 0
        PreviewActivity.show(activity, uris, uris, index, selectLimit)
    }

    /**
     * 启动裁剪
     */
    fun launchCrop(activity: AppCompatActivity): Uri? {
        val cropParams: CropParams? = PickControl.obtain(false).crop()
        //检查裁切参数
        if (null == cropParams) {
            Log.d(activity::class.java.simpleName, "please special crop params")
            activity.finish()
        }
        val params: CropParams = cropParams ?: return null
        //检查裁切图像路径
        val uri: Uri? = params.uri
        if (null == uri) {
            Log.d(activity::class.java.simpleName, "please special crop uri")
            activity.finish()
        }
        val srcUri: Uri = uri ?: return null
        //检查裁切图像存储权限
        val access: Boolean = checkPermission(activity, externalPermission, REQUEST_CODE_CROP)
        if (!access) return null
        //创建文件
        val optionalFile: File? = FileUtils.createPhoto(activity.application, params.getFormatExt(), "crop")
        if (null == optionalFile) {
            Toast.makeText(activity, activity.getString(R.string.pick_media_fail_to_create_file), Toast.LENGTH_SHORT).show()
            activity.finish()
        }
        val file: File = optionalFile ?: return null
        //开始裁剪
        val destUri: Uri = FileProvider.getUriForFile(activity, PickControl.authority(), file)
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(srcUri, "image/*")
        intent.putExtra("crop", "true")
        if (params.aspectX > 0) {
            intent.putExtra("aspectX", params.aspectX)
        }
        if (params.aspectY > 0) {
            intent.putExtra("aspectY", params.aspectY)
        }
        if (params.outputX > 0) {
            intent.putExtra("outputX", params.outputX)
        }
        if (params.outputY > 0) {
            intent.putExtra("outputY", params.outputY)
        }
        intent.putExtra("return-data", false)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, destUri)
        intent.putExtra("outputFormat", params.getFormatPlain())
        //相关应用授权
        val resolves: List<ResolveInfo> = activity.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val grantFlag: Int = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        for (info: ResolveInfo in resolves) {
            val packageName: String = info.activityInfo.packageName
            activity.grantUriPermission(packageName, destUri, grantFlag)
        }
        activity.startActivityForResult(intent, REQUEST_CODE_CROP)
        PickControl.obtain(false).cropFile(file)
        return destUri
    }

    /**
     * 加载图片资源图标
     */
    fun loadImages(context: Context): List<AlbumSection> {
        val sections: MutableList<AlbumSection> = ArrayList(1)
        sections.add(AlbumSection("", emptyList(), true))
        val resolver: ContentResolver = context.contentResolver
        val uri: Uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        val projection: Array<String> = arrayOf(
                MediaStore.Files.FileColumns._ID,
                MediaStore.MediaColumns.MIME_TYPE,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME
        )
        val sortOrder = "${MediaStore.Images.Media.DATE_TAKEN} DESC"
        var cursor: Cursor? = null
        try {
            cursor = resolver.query(uri, projection, null, null, sortOrder)
            cursor ?: return emptyList()
            val items: MutableList<AlbumItem> = ArrayList(cursor.count)
            for (i: Int in 0 until cursor.count) {
                if (!cursor.moveToPosition(i)) break
                val item: AlbumItem = AlbumItem.obtain(cursor)
                val access: Boolean = PickControl.obtain(false).filter().invoke(item.getUri(), item.getMime())
                if (!access) {
                    continue
                }
                items.add(item)
            }
            val others: List<AlbumSection> = items.groupBy {
                it.getBucket()
            }.map {
                AlbumSection(it.key, it.value, false)
            }
            sections.addAll(others)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            cursor?.close()
        }
        return sections
    }

    //将图像保存至系统相册
    fun saveToAlbum(context: Context, file: File): Uri {
        val fileUri: Uri = Uri.fromFile(file)
        return try {
            val values = ContentValues()
            val mime: String = getImageMime(context, file.absolutePath)
            values.put(MediaStore.Images.Media.DATA, file.absolutePath)
            values.put(MediaStore.Images.Media.MIME_TYPE, mime)
            val uri: Uri? = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            uri ?: return fileUri
            context.sendBroadcast(Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, fileUri))
            uri
        } catch (e: Exception) {
            e.printStackTrace()
            fileUri
        }
    }

    //获取图片的Mime
    fun getImageMime(context: Context, path: String): String {
        val mime: String = MimeType.getMimeByPath(path)?.mime ?: ""
        if (mime.isNotEmpty()) return mime
        if (!path.startsWith("content://")) return mime
        val uri: Uri = Uri.parse(path)
        val projection: Array<String> = arrayOf(MediaStore.Images.Media.MIME_TYPE)
        val resolver: ContentResolver = context.applicationContext.contentResolver
        var cursor: Cursor? = null
        return try {
            cursor = resolver.query(uri, projection, null, null, null)
            if (cursor.moveToFirst()) cursor.getString(0) else mime
        } catch (e: Exception) {
            e.printStackTrace()
            mime
        } finally {
            cursor?.close()
        }
    }

    //通过path获取Uri
    fun path2Uri(path: String): Uri? {
        return try {
            val prefix: Array<String> = arrayOf("content://", "file://")
            for (p: String in prefix) {
                if (path.startsWith(p)) {
                    return Uri.parse(path)
                }
            }
            val file = File(path)
            if (file.isFile) Uri.fromFile(file) else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    @SuppressLint("Recycle")
    fun filePath(context: Context?, path: String): String {
        context ?: return ""
        val prefix: Array<String> = arrayOf("content://", "file://")
        var isUri = false
        for (p: String in prefix) {
            if (path.startsWith(p)) {
                isUri = true
            }
        }
        if (!isUri) return path
        var cursor: Cursor? = null
        return try {
            val uri: Uri = Uri.parse(path)
            val projection: Array<String> = arrayOf(MediaStore.MediaColumns.DATA)
            val resolver: ContentResolver = context.applicationContext.contentResolver
            cursor = resolver.query(uri, projection, null, null, null) ?: return ""
            if (cursor.moveToFirst()) cursor.getString(0) else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        } finally {
            cursor?.close()
        }
    }

    //隐藏状态栏
    fun hideSystemStatusBar(activity: Activity?): Boolean {
        activity ?: return false
        val decorView: View = activity.window.decorView
        val flag: Boolean = 0 == (decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION)
        activity.intent.putExtra(KEY_NAVIGATION_FLAG, flag)
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        return true
    }

    //显示状态栏
    fun showSystemStatusBar(activity: Activity?): Boolean {
        activity ?: return false
        val decorView: View = activity.window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        if (!activity.intent.getBooleanExtra(KEY_NAVIGATION_FLAG, false)) {
            decorView.systemUiVisibility = decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        }
        return true
    }

    //获取状态栏高度
    fun getStatusBarHeight(context: Context): Int {
        val resources: Resources = context.resources
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            (23 * resources.displayMetrics.density + 0.5f).toInt()
        }
    }

    //获取结果中的uri列表
    fun obtainResultUris(data: Intent?): List<Uri> {
        val intent: Intent = data ?: return emptyList()
        return intent.getParcelableArrayListExtra(KEY_RESULT_DATA) ?: return emptyList()
    }

    //获取结果中是否关闭标志
    fun isResultFinish(data: Intent?): Boolean {
        val intent: Intent = data ?: return false
        return intent.getBooleanExtra(KEY_FINISH_FLAG, false)
    }

    fun setResult(activity: Activity, uri: List<Uri>?, finish: Boolean, notFinishCancel: Boolean) {
        if (!finish && notFinishCancel) {
            activity.setResult(Activity.RESULT_CANCELED)
            return
        }
        val intent = Intent()
        intent.putParcelableArrayListExtra(KEY_RESULT_DATA, ArrayList(uri ?: emptyList()))
        intent.putExtra(KEY_FINISH_FLAG, finish)
        activity.setResult(Activity.RESULT_OK, intent)
    }
}