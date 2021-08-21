package pizzk.media.picker.view

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager.widget.ViewPager
import pizzk.media.picker.R
import pizzk.media.picker.adapter.PreviewPhotoAdapter
import pizzk.media.picker.adapter.PreviewSelectAdapter
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.arch.PickLiveSource
import pizzk.media.picker.listener.PagerListener
import pizzk.media.picker.listener.SimpleAnimationListener
import pizzk.media.picker.source.IMediaSource
import pizzk.media.picker.source.PathMediaSource
import pizzk.media.picker.utils.PickUtils
import kotlin.math.min

@SuppressLint("NotifyDataSetChanged")
class PreviewActivity : AppCompatActivity() {
    companion object {
        private const val KEY_DATA: String = "key_data"
        private const val KEY_INDEX: String = "key_index"
        private const val KEY_SELECT_LIMIT: String = "key_select_limit"
        private const val KEY_SELECT_DATA: String = "key_select_data"

        internal fun show(
            activity: Activity?,
            all: List<String>,
            selects: List<String>,
            index: Int,
            selectLimit: Int = 0
        ) {
            val context: Activity = activity ?: return
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putStringArrayListExtra(KEY_DATA, ArrayList(all))
            intent.putExtra(KEY_INDEX, index)
            intent.putExtra(KEY_SELECT_LIMIT, selectLimit)
            intent.putStringArrayListExtra(KEY_SELECT_DATA, ArrayList(selects))
            context.startActivityForResult(intent, PickUtils.REQUEST_CODE_PREVIEW)
            activity.overridePendingTransition(0, 0)
        }
    }

    //控件
    private lateinit var toolbar: Toolbar
    private lateinit var photosView: ViewPager
    private lateinit var rlBottom: RelativeLayout
    private lateinit var selectedView: RecyclerView
    private lateinit var llSelect: LinearLayout
    private lateinit var checkBox: ImageView
    private lateinit var doneButton: PickActionMenu

    //动画
    private val hideOverlayAnim: AlphaAnimation = AlphaAnimation(1f, 0f)
    private val showOverlayAnim: AlphaAnimation = AlphaAnimation(0f, 1f)

    //适配器
    private lateinit var photoAdapter: PreviewPhotoAdapter
    private lateinit var selectAdapter: PreviewSelectAdapter

    //标志
    private var currentPageIndex: Int = 0
    private var selectLimit: Int = 0
    private var finishFlag: Boolean = false

    //可见性
    private var overlayFlag: Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)
        setupAdapter()
        initViews()
    }

    //设置适配器
    private fun setupAdapter() {
        val photos: List<String> = intent.getStringArrayListExtra(KEY_DATA)
        val temp: IMediaSource? = if (photos.isEmpty()) PickLiveSource.source() else null
        val source = temp ?: PathMediaSource(baseContext, photos)
        //预览相关
        photoAdapter = PreviewPhotoAdapter(baseContext, source)
        photoAdapter.setClickListener { switchOverlayVisibility() }
        photoAdapter.setScaleBlock {
            if (overlayFlag) {
                switchOverlayVisibility()
            }
        }
        //选择相关
        selectLimit = intent.getIntExtra(KEY_SELECT_LIMIT, selectLimit)
        val selects: List<String> = intent.getStringArrayListExtra(KEY_SELECT_DATA)
        selectAdapter = PreviewSelectAdapter(baseContext, selects)
        selectAdapter.setClickListener { path ->
            val selectIndex = photoAdapter.indexOf(path)
            if (selectIndex < 0) return@setClickListener
            setCurrentPageIndex(selectIndex, true)
        }
    }

    override fun finish() {
        val uris: List<Uri>? =
            if (selectLimit > 0) selectAdapter.getList().mapNotNull(Uri::parse) else null
        PickUtils.setResult(this@PreviewActivity, uris, finishFlag, false)
        super.finish()
        overridePendingTransition(0, 0)
    }

    //初始化视图控件
    private fun initViews() {
        //沉浸状态栏准备
        var systemUiVisibility: Int = window.decorView.systemUiVisibility
        if (PickUtils.hideSystemStatusBar(this)) {
            PickUtils.showSystemStatusBar(this)
        } else {
            systemUiVisibility = -1
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val lp: WindowManager.LayoutParams = window.attributes
            val mode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            lp.layoutInDisplayCutoutMode = mode
            window.attributes = lp
        }
        //标题栏
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = ""
        toolbar.setNavigationOnClickListener { finish() }
        if (systemUiVisibility >= 0) {
            val lp: ViewGroup.MarginLayoutParams =
                toolbar.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = PickUtils.getStatusBarHeight(baseContext)
        }
        doneButton = PickActionMenu(toolbar) {
            finishFlag = true
            finish()
        }
        doneButton.item().title = ""
        //底部菜单
        rlBottom = findViewById(R.id.rlBottom)
        checkBox = findViewById(R.id.check)
        llSelect = findViewById(R.id.llSelect)
        llSelect.setOnClickListener {
            if (currentPageIndex < 0) return@setOnClickListener
            val path: String = photoAdapter.getPath(currentPageIndex)
            val select: Boolean = !selectAdapter.getList().contains(path)
            if (select && selectAdapter.getList().size >= selectLimit) {
                val hint: String = getString(R.string.pick_media_most_select_limit)
                val content: String = String.format(hint, selectLimit)
                Toast.makeText(baseContext, content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            switchSelectBox(select, path, currentPageIndex)
            selectAdapter.onPreviewChanged(path, selectedView)
        }
        //选中列表
        selectedView = findViewById(R.id.selectRecycleView)
        selectedView.adapter = selectAdapter
        val lmOfSelect = LinearLayoutManager(baseContext)
        lmOfSelect.orientation = LinearLayoutManager.HORIZONTAL
        selectedView.layoutManager = lmOfSelect
        //预览列表
        photosView = findViewById(R.id.photoPager)
        photosView.addOnPageChangeListener(object : PagerListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == currentPageIndex) return
                setCurrentPageIndex(position, false)
            }
        })
        photosView.adapter = photoAdapter
        //状态更新
        val index = intent.getIntExtra(KEY_INDEX, 0)
        setCurrentPageIndex(min(index, photoAdapter.count - 1), true)
        notifyBottomChanged(true)
    }

    private fun notifyBottomChanged(shown: Boolean) {
        val views = arrayOf(rlBottom, selectedView)
        val vis = views.map { it.visibility }.toMutableList()
        if (selectLimit > 0) {
            vis[0] = if (shown) View.VISIBLE else View.GONE
            val emptySelect: Boolean = selectAdapter.getList().isEmpty()
            vis[1] = if (shown && !emptySelect) View.VISIBLE else View.GONE
        } else {
            vis[0] = View.GONE
            vis[1] = View.GONE
        }
        views.forEachIndexed { i, v -> if (v.visibility != vis[i]) v.visibility = vis[i] }
    }

    //设置当前选中索引
    private fun setCurrentPageIndex(index: Int, adjust: Boolean) {
        currentPageIndex = index
        toolbar.title = "${index + 1}/${photoAdapter.count}"
        val path = photoAdapter.getPath(index)
        val select: Boolean = selectAdapter.getList().contains(path)
        switchSelectBox(select, path, index)
        selectAdapter.onPreviewChanged(path, selectedView)
        photoAdapter.resetScale()
        photoAdapter.stopPlay()
        if (!adjust) return
        photosView.setCurrentItem(index, false)
    }

    //切换显示、隐藏标题等界面
    private fun switchOverlayVisibility() {
        val shown: Boolean = (toolbar.visibility == View.VISIBLE)
        val duration: Long = 240
        val views: Array<View> = arrayOf(toolbar, selectedView, rlBottom)
        if (shown) {
            overlayFlag = false
            hideOverlayAnim.duration = duration
            hideOverlayAnim.setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    toolbar.visibility = View.GONE
                    notifyBottomChanged(false)
                    PickUtils.hideSystemStatusBar(this@PreviewActivity)
                }
            })
            views.forEach { it.startAnimation(hideOverlayAnim) }
        } else {
            overlayFlag = true
            showOverlayAnim.duration = duration
            PickUtils.showSystemStatusBar(this@PreviewActivity)
            toolbar.postDelayed({
                notifyBottomChanged(true)
                toolbar.visibility = View.VISIBLE
                views.forEach { it.startAnimation(showOverlayAnim) }
            }, duration)
        }
    }

    //切换选择状态
    private fun switchSelectBox(select: Boolean, path: String, index: Int) {
        checkBox.setImageResource(if (select) R.drawable.album_check_active else R.drawable.album_check_normal)
        photoAdapter.get(index)?.let { media ->
            val enable = PickControl.obtain().filter().invoke(media)
            llSelect.visibility = if (enable) View.VISIBLE else View.GONE
        }
        val contain: Boolean = selectAdapter.getList().contains(path)
        if (select) {
            if (!contain && selectAdapter.getList().add(path)) {
                notifyBottomChanged(true)
                selectAdapter.notifyDataSetChanged()
            }
        } else {
            if (contain && selectAdapter.getList().remove(path)) {
                notifyBottomChanged(true)
                selectAdapter.notifyDataSetChanged()
            }
        }
        //标题菜单显示空时
        if (selectAdapter.getList().isEmpty()) {
            doneButton.item().setTitle(R.string.pick_media_finish)
            doneButton.enable(selectLimit <= 0)
        } else {
            if (selectLimit > 0) {
                val selectCount: Int = selectAdapter.getList().size
                val finishTitle: String = String.format(
                    getString(R.string.pick_media_finish_format),
                    selectCount,
                    selectLimit
                )
                doneButton.item().title = finishTitle

            }
            doneButton.enable(true)
        }
    }

    override fun onPause() {
        super.onPause()
        photoAdapter.stopPlay()
    }
}