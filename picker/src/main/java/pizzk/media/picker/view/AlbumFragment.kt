package pizzk.media.picker.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.support.v7.widget.GridLayoutManager
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.SimpleItemAnimator
import android.view.MenuItem
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.AccelerateInterpolator
import android.widget.TextView
import pizzk.media.picker.R
import pizzk.media.picker.adapter.AlbumPhotoAdapter
import pizzk.media.picker.adapter.AlbumSectionAdapter
import pizzk.media.picker.arch.PickControl
import pizzk.media.picker.entity.AlbumItem

class AlbumFragment : BaseFragment() {
    private lateinit var photoRecycleView: RecyclerView
    private lateinit var tvSection: TextView
    private lateinit var vCenter: View
    private lateinit var llCenter: View
    private lateinit var tvPreview: TextView
    private lateinit var rlBottom: View
    //相册目录
    private lateinit var sectionMask: View
    private lateinit var sectionRecycleView: RecyclerView
    //标题菜单
    private lateinit var commitMenu: MenuItem
    //适配器
    private lateinit var photoAdapter: AlbumPhotoAdapter
    private lateinit var sectionAdapter: AlbumSectionAdapter
    //标志位
    private var useOriginPhoto: Boolean = false
    //动画
    private var animHideSection: AnimatorSet? = null
    private var animShowSection: AnimatorSet? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val parent: Activity = activity!!
        //
        photoAdapter = AlbumPhotoAdapter(parent)
        photoAdapter.setSelectBlock(::onSelectChanged)
        photoAdapter.setTapBlock { holder, index ->

        }
        //
        sectionAdapter = AlbumSectionAdapter(parent)
        sectionAdapter.setTapBlock { holder, index ->

        }
    }

    override fun getLayoutId(): Int = R.layout.fragment_album

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        getToolbar().title = getString(R.string.title_select_picture)
        getToolbar().setNavigationOnClickListener { finish() }
        //菜单按钮
        commitMenu = getToolbar().menu.add(getString(R.string.finish))
        commitMenu.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)
        commitMenu.setOnMenuItemClickListener {
            finish()
            return@setOnMenuItemClickListener true
        }
        //图片列表
        photoRecycleView = view.findViewById(R.id.photoRecycleView)
        (photoRecycleView.itemAnimator as SimpleItemAnimator).supportsChangeAnimations = false
        val layoutManager = GridLayoutManager(context!!, resources.getInteger(R.integer.album_span_count))
        photoRecycleView.layoutManager = layoutManager
        photoRecycleView.adapter = photoAdapter
        //底部操作栏
        tvSection = view.findViewById(R.id.tvSection)
        tvSection.setOnClickListener(::onWidgetClick)
        vCenter = view.findViewById(R.id.vCenter)
        changeOriginState()
        llCenter = view.findViewById(R.id.llCenter)
        llCenter.setOnClickListener(::onWidgetClick)
        tvPreview = view.findViewById(R.id.tvPreview)
        tvPreview.setOnClickListener(::onWidgetClick)
        rlBottom = view.findViewById(R.id.rlBottom)
        //相册目录
        sectionMask = view.findViewById(R.id.sectionMask)
        sectionMask.setOnClickListener(::onWidgetClick)
        sectionRecycleView = view.findViewById(R.id.sectionRecycleView)
        sectionRecycleView.layoutManager = LinearLayoutManager(context)
        sectionRecycleView.adapter = sectionAdapter
        //选中状态调整
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

            }
            sectionMask -> {
                showSectionView(false)
            }
        }
    }


    private fun showSectionView(shown: Boolean) {
        if (null == animShowSection) {
            val tansY = "translationY"
            val bottomHeight: Float = rlBottom.top.toFloat()
            val alpha = "alpha"
            sectionMask.visibility = View.VISIBLE
            val translationShow: ObjectAnimator = ObjectAnimator.ofFloat(sectionRecycleView, tansY, bottomHeight, 0f)
            val alphaShow: ObjectAnimator = ObjectAnimator.ofFloat(sectionMask, alpha, 0.0f, 1.0f)
            translationShow.duration = 300
            val animShow = AnimatorSet()
            animShow.interpolator = AccelerateDecelerateInterpolator()
            animShow.play(translationShow).with(alphaShow)
            animShowSection = animShow
            //隐藏动画
            val translationHide: ObjectAnimator = ObjectAnimator.ofFloat(sectionRecycleView, tansY, 0f, bottomHeight)
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
            animShowSection?.start()
        } else {
            animHideSection?.start()
        }

    }


    //选择发生变化回调
    private fun onSelectChanged(list: List<AlbumItem>) {
        if (list.isEmpty()) {
            tvPreview.setText(R.string.preview)
            commitMenu.setTitle(R.string.finish)
            commitMenu.isEnabled = false
        } else {
            tvPreview.text = String.format(getString(R.string.preview_format), list.size)
            commitMenu.title = String.format(getString(R.string.finish_format), list.size, PickControl.obtain().limit())
            commitMenu.isEnabled = true
        }
    }


    //调整原图选中状态
    private fun changeOriginState(check: Boolean = useOriginPhoto) {
        val res: Int = if (check) R.drawable.pick_radio_checked else R.drawable.pick_radio_normal
        vCenter.background = ContextCompat.getDrawable(context!!, res)
    }
}