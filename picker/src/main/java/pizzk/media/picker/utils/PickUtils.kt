package pizzk.media.picker.utils

import android.Manifest
import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v4.content.FileProvider
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem
import pizzk.media.picker.entity.AlbumSection
import pizzk.media.picker.view.AlbumFragment
import java.io.File

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
     * 显示Fragment
     */
    fun showFragment(activity: AppCompatActivity, fragment: Fragment, bundle: Bundle, stack: Boolean = false) {
        val fm: FragmentManager = activity.supportFragmentManager
        val ft: FragmentTransaction = fm.beginTransaction()
        fragment.arguments = bundle
        ft.add(R.id.frameLayout, fragment)
        if (stack) {
            ft.addToBackStack(fragment::class.java.simpleName)
        }
        ft.commit()
    }

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
    fun launchAlbum(activity: AppCompatActivity) {
        val access: Boolean = checkPermission(activity, externalPermission) {
            Log.d(activity::class.java.simpleName, "permission refused:$it")
            activity.finish()
        }
        if (!access) return
        showFragment(activity, AlbumFragment(), activity.intent.extras)
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
            Toast.makeText(activity, activity.getString(R.string.fail_to_create_file), Toast.LENGTH_SHORT).show()
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
                    AlbumItem.recycle(item)
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
}