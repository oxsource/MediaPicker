package pizzk.media.picker.view

import android.net.Uri
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import pizzk.media.picker.R
import pizzk.media.picker.adapter.PreviewPhotoAdapter
import android.support.v4.view.ViewPager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.MenuItem
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import pizzk.media.picker.adapter.PagerListener
import pizzk.media.picker.adapter.PreviewSelectAdapter
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.listener.SimpleAnimationListener
import pizzk.media.picker.utils.PickUtils


class PreviewFragment : BaseFragment() {
    companion object {
        private const val KEY_DATA: String = "key_data"
        private const val KEY_INDEX: String = "key_index"
        private const val KEY_SELECT_FLAG: String = "key_select_flag"
        private const val KEY_SELECT_DATA: String = "key_select_data"

        fun getPreviewBundle(list: ArrayList<Uri>, current: Int, showSelect: Boolean, selects: ArrayList<Uri>? = null): Bundle {
            val bundle = Bundle()
            bundle.putParcelableArrayList(KEY_DATA, list)
            bundle.putInt(KEY_INDEX, current)
            bundle.putBoolean(KEY_SELECT_FLAG, showSelect)
            val selectList: ArrayList<Uri> = selects ?: ArrayList(0)
            bundle.putParcelableArrayList(KEY_SELECT_DATA, selectList)
            return bundle
        }
    }

    //控件
    private lateinit var toolbar: Toolbar
    private lateinit var photoPager: ViewPager
    private lateinit var rlBottom: RelativeLayout
    private lateinit var selectRecycleView: RecyclerView
    private lateinit var llSelect: LinearLayout
    private lateinit var checkBox: ImageView
    //标题菜单
    private lateinit var commitMenu: MenuItem
    //动画
    private val hideOverlayAnim: AlphaAnimation = AlphaAnimation(1f, 0f)
    private val showOverlayAnim: AlphaAnimation = AlphaAnimation(0f, 1f)
    //适配器
    private lateinit var photoAdapter: PreviewPhotoAdapter
    private lateinit var selectAdapter: PreviewSelectAdapter

    private var currentIndex: Int = 0
    private var selectMode: Boolean = false


    override fun getLayoutId(): Int = R.layout.fragment_preview

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (null == arguments) {
            finish()
            return
        }
        val bundle: Bundle = arguments!!
        val photos: List<Uri> = bundle.getParcelableArrayList(KEY_DATA)
        currentIndex = bundle.getInt(KEY_INDEX, currentIndex)
        photoAdapter = PreviewPhotoAdapter(context!!, photos)
        photoAdapter.setClickListener {
            switchOverlayVisibility()
        }
        //选择相关
        selectMode = bundle.getBoolean(KEY_SELECT_FLAG, selectMode)
        val selects: List<Uri> = bundle.getParcelableArrayList(KEY_SELECT_DATA)
        selectAdapter = PreviewSelectAdapter(context!!, selects)
        selectAdapter.setClickListener {
            val uri: Uri = it
            val targetUri: Uri? = photoAdapter.getList().findLast { it == uri }
            val selectIndex: Int = targetUri?.let { photoAdapter.getList().indexOf(it) } ?: -1
            if (selectIndex < 0) return@setClickListener
            setCurrentIndex(selectIndex, true)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view: ViewGroup = inflater.inflate(getLayoutId(), null, false) as ViewGroup
        toolbar = view.findViewById(R.id.toolbar)
        return view
    }

    override fun getToolbar(): Toolbar = toolbar

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //标题
        getToolbar().title = getString(R.string.pick_media_preview)
        getToolbar().setNavigationOnClickListener { finish() }
        commitMenu = getToolbar().menu.add(R.string.pick_media_finish)
        commitMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        commitMenu.setOnMenuItemClickListener {
            if (selectMode) {
                PickControl.obtain().callbacks().invoke(selectAdapter.getList())
            }
            activity!!.finish()
            return@setOnMenuItemClickListener true
        }
        //底部菜单
        rlBottom = view.findViewById(R.id.rlBottom)
        checkBox = view.findViewById(R.id.check)
        llSelect = view.findViewById(R.id.llSelect)
        llSelect.setOnClickListener {
            if (currentIndex < 0) return@setOnClickListener
            val limit: Int = PickControl.obtain().limit()
            if (selectAdapter.getList().size >= limit) {
                val hint: String = context!!.getString(R.string.pick_media_most_select_limit)
                val content: String = String.format(hint, limit)
                Toast.makeText(context, content, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val uri: Uri = photoAdapter.getList()[currentIndex]
            val select: Boolean = !selectAdapter.getList().contains(uri)
            switchSelectBox(select, currentIndex)
            selectAdapter.onPreviewChanged(uri, selectRecycleView)
        }
        //
        //选择列表
        selectRecycleView = view.findViewById(R.id.selectRecycleView)
        val lmOfSelect = LinearLayoutManager(view.context)
        lmOfSelect.orientation = LinearLayoutManager.HORIZONTAL
        selectRecycleView.layoutManager = lmOfSelect
        selectRecycleView.adapter = selectAdapter
        //图片列表
        photoPager = view.findViewById(R.id.photoPager)
        photoPager.adapter = photoAdapter
        photoPager.addOnPageChangeListener(object : PagerListener() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                if (position == currentIndex) return
                currentIndex = position
                setCurrentIndex(position, false)
            }
        })
        setCurrentIndex(currentIndex, true)
        notifyBottomChanged(true)
    }

    private fun notifyBottomChanged(shown: Boolean) {
        if (selectMode) {
            rlBottom.visibility = if (shown) View.VISIBLE else View.GONE
            val emptySelect: Boolean = selectAdapter.getList().isEmpty()
            selectRecycleView.visibility = if (shown && !emptySelect) View.VISIBLE else View.GONE
        } else {
            rlBottom.visibility = View.GONE
            selectRecycleView.visibility = View.GONE
        }
    }

    //设置当前选中索引
    private fun setCurrentIndex(index: Int, move: Boolean) {
        getToolbar().title = "${index + 1}/${photoAdapter.count}"
        val select: Boolean = selectAdapter.onPreviewChanged(photoAdapter.getList()[index], selectRecycleView)
        switchSelectBox(select, index)
        photoAdapter.getPrimaryItem()?.let { if (it.scale != 1.0f) it.scale = 1.0f }
        if (move) {
            photoPager.setCurrentItem(index, false)
        }
    }

    //切换显示、隐藏标题等界面
    private fun switchOverlayVisibility() {
        val shown: Boolean = getToolbar().visibility == View.VISIBLE
        val duration: Long = 240
        val views: Array<View> = arrayOf(getToolbar(), selectRecycleView, rlBottom)
        if (shown) {
            hideOverlayAnim.duration = duration
            hideOverlayAnim.setAnimationListener(object : SimpleAnimationListener() {
                override fun onAnimationEnd(animation: Animation?) {
                    getToolbar().visibility = View.GONE
                    notifyBottomChanged(false)
                    if (!PickUtils.withNavBar(activity!!)) {
                        photoPager.setPadding(0, PickUtils.getStatusBarHeight(context!!), 0, 0)
                    }
                    PickUtils.hideSystemStatusBar(activity)
                }
            })
            views.forEach { it.startAnimation(hideOverlayAnim) }
        } else {
            showOverlayAnim.duration = duration
            PickUtils.showSystemStatusBar(activity)
            if (!PickUtils.withNavBar(activity!!)) {
                photoPager.setPadding(0, 0, 0, 0)
            }
            getToolbar().postDelayed({
                notifyBottomChanged(true)
                getToolbar().visibility = View.VISIBLE
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
            if (contain) return
            if (selectAdapter.getList().add(uri)) {
                notifyBottomChanged(true)
                selectAdapter.notifyDataSetChanged()
            }
        } else {
            if (!contain) return
            if (selectAdapter.getList().remove(uri)) {
                notifyBottomChanged(true)
                selectAdapter.notifyDataSetChanged()
            }
        }
        //标题菜单显示空时
        if (selectAdapter.getList().isEmpty()) {
            commitMenu.setTitle(R.string.pick_media_finish)
            commitMenu.isEnabled = !selectMode
        } else {
            if (selectMode) {
                val selectCount: Int = selectAdapter.getList().size
                val limit: Int = PickControl.obtain().limit()
                val commitTitle: String = String.format(getString(R.string.pick_media_finish_format), selectCount, limit)
                commitMenu.title = commitTitle
            }
            commitMenu.isEnabled = true
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        PickUtils.showSystemStatusBar(activity)
    }
}