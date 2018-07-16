package pizzk.media.picker.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.view.ViewPager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.View
import android.view.ViewGroup
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import pizzk.media.picker.R
import pizzk.media.picker.adapter.PagerListener
import pizzk.media.picker.adapter.PreviewPhotoAdapter
import pizzk.media.picker.adapter.PreviewSelectAdapter
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.listener.SimpleAnimationListener
import pizzk.media.picker.utils.PickUtils


class PreviewActivity : AppCompatActivity() {
    companion object {
        private const val KEY_DATA: String = "key_data"
        private const val KEY_INDEX: String = "key_index"
        private const val KEY_SELECT_FLAG: String = "key_select_flag"
        private const val KEY_SELECT_DATA: String = "key_select_data"

        //返回结果标志
        internal const val KEY_RESULT_DATA: String = "key_result_data"
        internal const val KEY_FINISH_FLAG: String = "key_finish_flag"

        internal const val REQUEST_PREVIEW: Int = 101

        internal fun show(activity: Activity?, all: List<Uri>, selects: List<Uri>, index: Int, showSelect: Boolean) {
            val context: Activity = activity ?: return
            val bundle = Bundle()
            bundle.putParcelableArrayList(KEY_DATA, ArrayList(all))
            bundle.putInt(KEY_INDEX, index)
            bundle.putBoolean(KEY_SELECT_FLAG, showSelect)
            bundle.putParcelableArrayList(KEY_SELECT_DATA, ArrayList(selects))
            val intent = Intent(context, PreviewActivity::class.java)
            intent.putExtras(bundle)
            context.startActivityForResult(intent, REQUEST_PREVIEW)
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
    private var currentIndex: Int = 0
    private var selectMode: Boolean = false
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
        val bundle: Bundle = intent.extras
        val photos: List<Uri> = bundle.getParcelableArrayList(PreviewActivity.KEY_DATA)
        //预览相关
        currentIndex = bundle.getInt(PreviewActivity.KEY_INDEX, currentIndex)
        photoAdapter = PreviewPhotoAdapter(baseContext, photos)
        photoAdapter.setClickListener { switchOverlayVisibility() }
        photoAdapter.setScaleBlock {
            if (overlayFlag) {
                switchOverlayVisibility()
            }
        }
        //选择相关
        selectMode = bundle.getBoolean(PreviewActivity.KEY_SELECT_FLAG, selectMode)
        val selects: List<Uri> = bundle.getParcelableArrayList(PreviewActivity.KEY_SELECT_DATA)
        selectAdapter = PreviewSelectAdapter(baseContext, selects)
        selectAdapter.setClickListener {
            val uri: Uri = it
            val targetUri: Uri? = photoAdapter.getList().findLast { it == uri }
            val selectIndex: Int = targetUri?.let { photoAdapter.getList().indexOf(it) } ?: -1
            if (selectIndex < 0) return@setClickListener
            setCurrentIndex(selectIndex, true)
        }
    }

    override fun finish() {
        val intent = Intent()
        intent.putParcelableArrayListExtra(KEY_RESULT_DATA, ArrayList(selectAdapter.getList()))
        intent.putExtra(KEY_FINISH_FLAG, finishFlag)
        setResult(Activity.RESULT_OK, intent)
        super.finish()
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
        //标题栏
        toolbar = findViewById(R.id.toolbar)
        toolbar.title = getString(R.string.pick_media_preview)
        toolbar.setNavigationOnClickListener { finish() }
        if (systemUiVisibility >= 0) {
            val lp: ViewGroup.MarginLayoutParams = toolbar.layoutParams as ViewGroup.MarginLayoutParams
            lp.topMargin = PickUtils.getStatusBarHeight(baseContext)
        }
        doneButton = PickActionMenu(toolbar) {
            finishFlag = true
            finish()
        }
        doneButton.item().setTitle(R.string.pick_media_finish)
        //底部菜单
        rlBottom = findViewById(R.id.rlBottom)
        checkBox = findViewById(R.id.check)
        llSelect = findViewById(R.id.llSelect)
        llSelect.setOnClickListener {
            if (currentIndex < 0) return@setOnClickListener
            val limit: Int = PickControl.obtain().limit()
            if (selectAdapter.getList().size >= limit) {
                val hint: String = getString(R.string.pick_media_most_select_limit)
                val content: String = String.format(hint, limit)
                Toast.makeText(baseContext, content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uri: Uri = photoAdapter.getList()[currentIndex]
            val select: Boolean = !selectAdapter.getList().contains(uri)
            switchSelectBox(select, currentIndex)
            selectAdapter.onPreviewChanged(uri, selectedView)
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
                if (position == currentIndex) return
                currentIndex = position
                setCurrentIndex(position, false)
            }
        })
        photosView.adapter = photoAdapter
        //状态更新
        setCurrentIndex(currentIndex, true)
        notifyBottomChanged(true)
    }


    private fun notifyBottomChanged(shown: Boolean) {
        if (selectMode) {
            rlBottom.visibility = if (shown) View.VISIBLE else View.GONE
            val emptySelect: Boolean = selectAdapter.getList().isEmpty()
            selectedView.visibility = if (shown && !emptySelect) View.VISIBLE else View.GONE
        } else {
            rlBottom.visibility = View.GONE
            selectedView.visibility = View.GONE
        }
    }

    //设置当前选中索引
    private fun setCurrentIndex(index: Int, adjust: Boolean) {
        toolbar.title = "${index + 1}/${photoAdapter.count}"
        val select: Boolean = selectAdapter.onPreviewChanged(photoAdapter.getList()[index], selectedView)
        switchSelectBox(select, index)
        photoAdapter.getPrimaryItem()?.let { if (it.scale != 1.0f) it.scale = 1.0f }
        if (!adjust) return
        val smooth = false
        photosView.setCurrentItem(index, smooth)
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
    private fun switchSelectBox(value: Boolean, index: Int) {
        checkBox.setImageResource(if (value) R.drawable.album_check_active else R.drawable.album_check_normal)
        val uri: Uri = photoAdapter.getList()[index]
        val contain: Boolean = selectAdapter.getList().contains(uri)
        if (value) {
            if (!contain && selectAdapter.getList().add(uri)) {
                notifyBottomChanged(true)
                selectAdapter.notifyDataSetChanged()
            }
        } else {
            if (contain && selectAdapter.getList().remove(uri)) {
                notifyBottomChanged(true)
                selectAdapter.notifyDataSetChanged()
            }
        }
        //标题菜单显示空时
        if (selectAdapter.getList().isEmpty()) {
            doneButton.item().setTitle(R.string.pick_media_finish)
            doneButton.enable(!selectMode)
        } else {
            if (selectMode) {
                val selectCount: Int = selectAdapter.getList().size
                val limit: Int = PickControl.obtain().limit()
                val finishTitle: String = String.format(getString(R.string.pick_media_finish_format), selectCount, limit)
                doneButton.item().title = finishTitle

            }
            doneButton.enable(true)
        }
    }
}