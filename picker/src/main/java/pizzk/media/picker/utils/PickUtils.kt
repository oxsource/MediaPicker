package pizzk.media.picker.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.FileProvider
import android.util.Log
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem
import pizzk.media.picker.entity.AlbumSection
import java.io.File
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.view.View
import pizzk.media.picker.view.AlbumActivity

/**
 * 系统工具类
 */
object PickUtils {
    private val cameraPermission: Array<Pair<String, Int>> = arrayOf(
            Pair(Manifest.permission.CAMERA, R.string.open_camera_permission),
            Pair(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.open_external_storage_permission),
            Pair(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.open_external_storage_permission)
    )
    private val externalPermission: Array<Pair<String, Int>> = arrayOf(
            Pair(Manifest.permission.READ_EXTERNAL_STORAGE, R.string.open_external_storage_permission),
            Pair(Manifest.permission.WRITE_EXTERNAL_STORAGE, R.string.open_external_storage_permission)
    )

    /**
     * 检查权限
     */
    private fun checkPermission(activity: Activity, ps: Array<Pair<String, Int>>, refuse: (ps: String) -> Unit): Boolean {
        for (i: Int in 0 until ps.size) {
            val p: Pair<String, Int> = ps[i]
            val auth: Boolean = ActivityCompat.checkSelfPermission(activity, p.first) == PackageManager.PERMISSION_GRANTED
            if (auth) continue
            val shown: Boolean = ActivityCompat.shouldShowRequestPermissionRationale(activity, p.first)
            if (shown) {
                val requestCode: Int = i + 100
                ActivityCompat.requestPermissions(activity, arrayOf(p.first), requestCode)
            } else {
                Toast.makeText(activity, p.second, Toast.LENGTH_SHORT).show()
                refuse(p.first)
            }
            return false
        }
        return true
    }

    /**
     * 启动相册
     */
    fun launchAlbum(activity: AppCompatActivity, finish: Boolean) {
        val access: Boolean = checkPermission(activity, externalPermission) {
            Log.d(activity::class.java.simpleName, "permission refused:$it")
            activity.finish()
        }
        if (!access) return
        val intent = Intent(activity, AlbumActivity::class.java)
        activity.startActivity(intent)
        if (finish) activity.finish()
    }

    /**
     * 启动相机
     */
    fun launchCamera(activity: AppCompatActivity, requestCode: Int): Uri? {
        //权限确认
        val access: Boolean = PickUtils.checkPermission(activity, cameraPermission) {
            Log.d(activity::class.java.simpleName, "permission refused:$it")
            activity.finish()
        }
        if (!access) return null
        //创建文件
        val optionalFile: File? = FileUtils.createPhoto(activity.application)
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
        PickControl.obtain().cameraUri(uri)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        activity.startActivityForResult(intent, requestCode)
        return uri
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
        val cursor: Cursor = resolver.query(uri, projection, null, null, sortOrder)
        try {
            val items: MutableList<AlbumItem> = ArrayList(cursor.count)
            for (i: Int in 0 until cursor.count) {
                if (!cursor.moveToPosition(i)) break
                val item: AlbumItem = AlbumItem.obtain(cursor)
                val access: Boolean = PickControl.obtain().filter().invoke(item.getUri(), item.getMime())
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
            if (!cursor.isClosed) {
                cursor.close()
            }
        }
        return sections
    }

    //获取图片的Mime
    fun getImageMime(context: Context, uri: Uri): String {
        val projection: Array<String> = arrayOf(MediaStore.Images.Media.MIME_TYPE)
        val resolver: ContentResolver = context.contentResolver
        val cursor: Cursor = resolver.query(uri, projection, null, null, null)
        val value: String = if (cursor.moveToFirst()) cursor.getString(0) else ""
        cursor.close()
        return value
    }

    //隐藏状态栏
    fun hideSystemStatusBar(activity: Activity?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return false
        activity ?: return false
        val decorView: View = activity.window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                or View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                or View.SYSTEM_UI_FLAG_IMMERSIVE)
        return true
    }

    //显示状态栏
    fun showSystemStatusBar(activity: Activity?): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return false
        activity ?: return false
        val decorView: View = activity.window.decorView
        decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        return true
    }

    fun getStatusBarHeight(context: Context): Int {
        val resources: Resources = context.resources
        val resourceId: Int = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (resourceId > 0) {
            resources.getDimensionPixelSize(resourceId)
        } else {
            (23 * resources.displayMetrics.density + 0.5f).toInt()
        }
    }
}