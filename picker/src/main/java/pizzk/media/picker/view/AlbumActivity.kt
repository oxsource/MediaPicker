package pizzk.media.picker.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.*
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import pizzk.media.picker.R
import pizzk.media.picker.adapter.AlbumBucketAdapter
import pizzk.media.picker.adapter.AlbumPhotoAdapter
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.arch.PickLiveSource
import pizzk.media.picker.entity.AlbumBucket
import pizzk.media.picker.source.IMedia
import pizzk.media.picker.utils.PickUtils

/**
 * 相册Activity
 */
@SuppressLint("NotifyDataSetChanged")
class AlbumActivity : AppCompatActivity() {
    companion object {
        private const val KEY_SELECT_DATA: String = "key_select_data"
        private const val KEY_SELECT_LIMIT: String = "key_select_limit"

        fun show(activity: Activity, limit: Int, selects: List<Uri>) {
            val intent = Intent(activity, AlbumActivity::class.java)
            intent.putExtra(KEY_SELECT_LIMIT, limit)
            intent.putParcelableArrayListExtra(KEY_SELECT_DATA, ArrayList(selects))
            activity.startActivityForResult(intent, PickUtils.REQUEST_CODE_ALBUM)
            activity.overridePendingTransition(0, 0)
        }
    }

    private lateinit var toolbar: Toolbar
    private lateinit var photosView: RecyclerView
    private lateinit var tvSection: TextView
    private lateinit var vCenter: View
    private lateinit var llCenter: View
    private lateinit var tvPreview: TextView
    private lateinit var rlBottom: View
    private lateinit var sectionMask: View
    private lateinit var sectionsView: RecyclerView
    private lateinit var doneButton: PickActionMenu

    //适配器
    private lateinit var photoAdapter: AlbumPhotoAdapter
    private lateinit var bucketAdapter: AlbumBucketAdapter

    //标志位
    private var finishFlag: Boolean = false

    //动画
    private var animHideSection: AnimatorSet? = null
    private var animShowSection: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        setupAdapter()
        initViews()
        PickLiveSource.observe(this, sourceObserver)
        PickLiveSource.load(this)
    }

    private val sourceObserver = Observer<PickLiveSource.Data> {
        val selectUris: List<Uri> = intent.getParcelableArrayListExtra(KEY_SELECT_DATA)
        val selects = photoAdapter.getMedias(selectUris)
        photoAdapter.updateSelectList(selects)
        onSelectChanged(photoAdapter.getSelectList())
        bucketAdapter.append(it.buckets, clean = true)
        bucketAdapter.notifyDataSetChanged()
    }

    //初始化适配器
    private fun setupAdapter() {
        //图片适配器
        photoAdapter = AlbumPhotoAdapter(baseContext)
        photoAdapter.setSelectLimit(intent.getIntExtra(KEY_SELECT_LIMIT, 0))
        photoAdapter.setTapBlock { _, index ->
            val selects: List<String> =
                photoAdapter.getSelectList().mapNotNull(IMedia::uri).map(Uri::toString)
            PreviewActivity.show(
                this@AlbumActivity,
                emptyList(),
                selects,
                index,
                photoAdapter.getSelectLimit()
            )
        }
        //目录适配器
        bucketAdapter = AlbumBucketAdapter(baseContext)
        bucketAdapter.setTapBlock { _, index ->
            showSectionView(false)
            val section: AlbumBucket = bucketAdapter.getList()[index]
            val selectSection: AlbumBucket? = bucketAdapter.getSelectBucket()
            if (section == selectSection) return@setTapBlock
            bucketAdapter.notifyItemChanged(index)
            val oldIndex = bucketAdapter.getList().indexOf(selectSection)
            if (oldIndex >= 0) bucketAdapter.notifyItemChanged(oldIndex)
            selectSection?.select = false
            section.select = true
            val name: String = section.name
            tvSection.text = name
            //更新相册
            PickLiveSource.source()?.use(section.id)
            photoAdapter.notifyDataSetChanged()
        }
    }


    //初始化视图控件
    private fun initViews() {
        //标题栏
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = PickControl.obtain().title()
        if (toolbar.title.isNullOrEmpty()) {
            toolbar.title = getString(R.string.pick_media_select_picture)
        }
        toolbar.setNavigationOnClickListener { finish() }
        //提交按钮
        doneButton = PickActionMenu(toolbar) {
            finishFlag = true
            finish()
        }
        doneButton.item().setTitle(R.string.pick_media_finish)
        //图像列表
        photosView = findViewById(R.id.photoRecycleView)
        (photosView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        val layoutManager =
            GridLayoutManager(baseContext, resources.getInteger(R.integer.album_span_count))
        photosView.layoutManager = layoutManager
        photosView.adapter = photoAdapter
        //底部操作栏
        tvSection = findViewById(R.id.tvSection)
        tvSection.setOnClickListener(::onWidgetClick)
        vCenter = findViewById(R.id.vCenter)
        updateOriginState()
        llCenter = findViewById(R.id.llCenter)
        llCenter.setOnClickListener(::onWidgetClick)
        tvPreview = findViewById(R.id.tvPreview)
        tvPreview.setOnClickListener(::onWidgetClick)
        rlBottom = findViewById(R.id.rlBottom)
        //相册目录
        sectionMask = findViewById(R.id.sectionMask)
        sectionMask.setOnClickListener(::onWidgetClick)
        sectionsView = findViewById(R.id.sectionRecycleView)
        sectionsView.layoutManager = LinearLayoutManager(baseContext)
        sectionsView.adapter = bucketAdapter
        //选中状态调整
        photoAdapter.setSelectBlock(::onSelectChanged)
    }

    //控件点击事件
    private fun onWidgetClick(view: View) {
        when (view) {
            tvSection -> {
                showSectionView(View.VISIBLE != sectionMask.visibility)
            }
            llCenter -> {
                PickControl.setOriginQuality(!PickControl.originQuality())
                updateOriginState()
            }
            tvPreview -> {
                val medias = photoAdapter.getSelectList()
                val selects: List<String> = medias.mapNotNull(IMedia::uri).map(Uri::toString)
                if (selects.isEmpty()) return
                PreviewActivity.show(
                    this@AlbumActivity,
                    selects,
                    selects,
                    0,
                    photoAdapter.getSelectLimit()
                )
            }
            sectionMask -> {
                showSectionView(false)
            }
        }
    }

    //控制图片目录界面显示与隐藏
    private fun showSectionView(shown: Boolean) {
        if (null == animShowSection) {
            val tansY = "translationY"
            val bottomHeight: Float = rlBottom.top.toFloat()
            val alpha = "alpha"
            val translationShow: ObjectAnimator =
                ObjectAnimator.ofFloat(sectionsView, tansY, bottomHeight, 0f)
            val alphaShow: ObjectAnimator = ObjectAnimator.ofFloat(sectionMask, alpha, 0.0f, 1.0f)
            translationShow.duration = 300
            val animShow = AnimatorSet()
            animShow.interpolator = AccelerateDecelerateInterpolator()
            animShow.play(translationShow).with(alphaShow)
            animShowSection = animShow
            //隐藏动画
            val translationHide: ObjectAnimator =
                ObjectAnimator.ofFloat(sectionsView, tansY, 0f, bottomHeight)
            val alphaHide: ObjectAnimator = ObjectAnimator.ofFloat(sectionMask, alpha, 1.0f, 0.0f)
            translationHide.duration = 240
            val animHide = AnimatorSet()
            animHide.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    sectionMask.visibility = View.GONE
                }
            })
            animHide.interpolator = AccelerateInterpolator()
            animHide.play(translationHide).with(alphaHide)
            animHideSection = animHide
        }
        if (shown) {
            val anim: AnimatorSet = animShowSection ?: return
            if (anim.isStarted) return
            sectionMask.visibility = View.VISIBLE
            anim.start()
        } else {
            val anim: AnimatorSet = animHideSection ?: return
            if (anim.isStarted) return
            anim.start()
        }
    }

    //选择发生变化回调
    private fun onSelectChanged(list: List<IMedia>) {
        if (list.isEmpty()) {
            tvPreview.setText(R.string.pick_media_preview)
            doneButton.item().setTitle(R.string.pick_media_finish)
            doneButton.enable(false)
        } else {
            tvPreview.text = String.format(getString(R.string.pick_media_preview_format), list.size)
            doneButton.item().title = String.format(
                getString(R.string.pick_media_finish_format),
                list.size,
                photoAdapter.getSelectLimit()
            )
            doneButton.enable(true)
        }
    }

    //调整原图选中状态
    private fun updateOriginState() {
        val check = PickControl.originQuality()
        val res: Int = if (check) R.drawable.pick_radio_checked else R.drawable.pick_radio_normal
        vCenter.background = ContextCompat.getDrawable(baseContext, res)
    }

    override fun finish() {
        val uri: List<Uri> = photoAdapter.getSelectList().mapNotNull { it.uri() }
        PickUtils.setResult(this@AlbumActivity, uri, finishFlag, true)
        super.finish()
        overridePendingTransition(0, 0)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            PickUtils.REQUEST_CODE_PREVIEW -> {
                val selectUris: List<Uri> = PickUtils.obtainResultUris(data)
                finishFlag = PickUtils.isResultFinish(data)
                PickLiveSource.source()?.use(null)
                val selects = photoAdapter.getMedias(selectUris)
                photoAdapter.updateSelectList(selects)
                //recover
                PickLiveSource.source()?.use(bucketAdapter.getSelectBucket()?.id)
                if (finishFlag) {
                    finish()
                    return
                }
            }
        }
    }

    override fun onDestroy() {
        PickLiveSource.removeObserver(sourceObserver)
        PickLiveSource.source()?.close()
        super.onDestroy()
    }
}