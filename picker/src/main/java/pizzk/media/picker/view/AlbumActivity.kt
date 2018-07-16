package pizzk.media.picker.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.*
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import pizzk.media.picker.R
import pizzk.media.picker.adapter.AlbumPhotoAdapter
import pizzk.media.picker.adapter.AlbumSectionAdapter
import pizzk.media.picker.entity.AlbumItem
import pizzk.media.picker.entity.AlbumSection
import pizzk.media.picker.utils.PickUtils

/**
 * 相册Activity
 */
class AlbumActivity : AppCompatActivity() {
    companion object {
        private const val KEY_SELECT_DATA: String = "key_select_data"
        private const val KEY_SELECT_LIMIT: String = "key_select_limit"

        fun show(activity: Activity, limit: Int, selects: List<Uri>) {
            val intent = Intent(activity, AlbumActivity::class.java)
            intent.putExtra(KEY_SELECT_LIMIT, limit)
            intent.putParcelableArrayListExtra(KEY_SELECT_DATA, ArrayList(selects))
            activity.startActivityForResult(intent, PickUtils.REQUEST_CODE_ALBUM)
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
    private lateinit var sectionAdapter: AlbumSectionAdapter
    //标志位
    private var useOriginPhoto: Boolean = false
    private var finishFlag: Boolean = false
    private var selectLimit: Int = 0
    //动画
    private var animHideSection: AnimatorSet? = null
    private var animShowSection: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_album)
        setupAdapter()
        initViews()
    }

    //初始化适配器
    private fun setupAdapter() {
        selectLimit = intent.getIntExtra(KEY_SELECT_LIMIT, selectLimit)
        val selectUris: List<Uri> = intent.getParcelableArrayListExtra(KEY_SELECT_DATA)
        //图片适配器
        photoAdapter = AlbumPhotoAdapter(baseContext)
        photoAdapter.setTapBlock { _, index ->
            val uris: List<Uri> = photoAdapter.getList().mapNotNull(AlbumItem::getUri)
            val selects: List<Uri> = photoAdapter.getSelectList().mapNotNull(AlbumItem::getUri)
            PreviewActivity.show(this@AlbumActivity, uris, selects, index, selectLimit)
        }
        //目录适配器
        sectionAdapter = AlbumSectionAdapter(baseContext)
        sectionAdapter.setTapBlock { _, index ->
            showSectionView(false)
            val section: AlbumSection = sectionAdapter.getList()[index]
            val selectSection: AlbumSection? = sectionAdapter.getSelectSection()
            if (section == selectSection) return@setTapBlock
            sectionAdapter.notifyDataSetChanged()
            selectSection?.select = false
            section.select = true
            val name: String = section.name
            tvSection.text = name
            //更新数据
            photoAdapter.append(sectionAdapter.getAlbumsBySectionIndex(index), true)
            photoAdapter.notifyDataSetChanged()
        }
        //初始化数据
        val allItems: List<AlbumItem> = sectionAdapter.getAlbumsBySectionIndex(0)
        photoAdapter.append(allItems, true)
        val selects: List<AlbumItem> = selectUris.map {
            val uri: Uri = it
            return@map allItems.findLast { it.getUri() == uri }
        }.filterNotNull()
        photoAdapter.updateSelectList(selects)
    }


    //初始化视图控件
    private fun initViews() {
        //标题栏
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.pick_media_select_picture)
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
        val layoutManager = GridLayoutManager(baseContext, resources.getInteger(R.integer.album_span_count))
        photosView.layoutManager = layoutManager
        photosView.adapter = photoAdapter
        //底部操作栏
        tvSection = findViewById(R.id.tvSection)
        tvSection.setOnClickListener(::onWidgetClick)
        vCenter = findViewById(R.id.vCenter)
        changeOriginState()
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
        sectionsView.adapter = sectionAdapter
        //选中状态调整
        photoAdapter.setSelectBlock(::onSelectChanged)
        onSelectChanged(photoAdapter.getSelectList())
    }

    //控件点击事件
    private fun onWidgetClick(view: View) {
        when (view) {
            tvSection -> {
                showSectionView(View.VISIBLE != sectionMask.visibility)
            }
            llCenter -> {
                useOriginPhoto = !useOriginPhoto
                changeOriginState()
            }
            tvPreview -> {
                val uris: List<Uri> = photoAdapter.getSelectList().mapNotNull(AlbumItem::getUri)
                if (uris.isNotEmpty()) {
                    val selects: List<Uri> = photoAdapter.getSelectList().mapNotNull(AlbumItem::getUri)
                    PreviewActivity.show(this@AlbumActivity, uris, selects, 0, selectLimit)
                }
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
            val translationShow: ObjectAnimator = ObjectAnimator.ofFloat(sectionsView, tansY, bottomHeight, 0f)
            val alphaShow: ObjectAnimator = ObjectAnimator.ofFloat(sectionMask, alpha, 0.0f, 1.0f)
            translationShow.duration = 300
            val animShow = AnimatorSet()
            animShow.interpolator = AccelerateDecelerateInterpolator()
            animShow.play(translationShow).with(alphaShow)
            animShowSection = animShow
            //隐藏动画
            val translationHide: ObjectAnimator = ObjectAnimator.ofFloat(sectionsView, tansY, 0f, bottomHeight)
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
    private fun onSelectChanged(list: List<AlbumItem>) {
        if (list.isEmpty()) {
            tvPreview.setText(R.string.pick_media_preview)
            doneButton.item().setTitle(R.string.pick_media_finish)
            doneButton.enable(false)
        } else {
            tvPreview.text = String.format(getString(R.string.pick_media_preview_format), list.size)
            doneButton.item().title = String.format(getString(R.string.pick_media_finish_format), list.size, selectLimit)
            doneButton.enable(true)
        }
    }

    //调整原图选中状态
    private fun changeOriginState(check: Boolean = useOriginPhoto) {
        val res: Int = if (check) R.drawable.pick_radio_checked else R.drawable.pick_radio_normal
        vCenter.background = ContextCompat.getDrawable(baseContext, res)
    }

    override fun finish() {
        val uri: List<Uri> = photoAdapter.getSelectList().mapNotNull { it.getUri() }
        PickUtils.setResult(this@AlbumActivity, uri, finishFlag, true)
        super.finish()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        when (requestCode) {
            PickUtils.REQUEST_CODE_PREVIEW -> {
                val selectUris: List<Uri> = PickUtils.obtainResultUris(data)
                finishFlag = PickUtils.isResultFinish(data)
                val all: List<AlbumItem> = sectionAdapter.getAlbumsBySectionIndex(0)
                val selectItems: List<AlbumItem> = all.filter { selectUris.contains(it.getUri()) }
                photoAdapter.updateSelectList(selectItems)
                if (finishFlag) {
                    finish()
                    return
                }
            }
        }
    }
}