package pizzk.media.picker.view

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.animation.DecelerateInterpolator
import pizzk.media.picker.R
import pizzk.media.picker.adapter.PickChoseAdapter

class PickChoseActivity : AppCompatActivity() {
    companion object {
        private const val KEY_CHOICE_LIST = "key_choice_list"
        private var resultBlock: (String) -> Unit = { _ -> }

        fun show(activity: Activity, choice: List<String>, block: (key: String) -> Unit) {
            resultBlock = block
            val intent = Intent(activity, PickChoseActivity::class.java)
            intent.putStringArrayListExtra(KEY_CHOICE_LIST, ArrayList(choice))
            activity.startActivity(intent)
            activity.overridePendingTransition(0, 0)
        }
    }

    private lateinit var vMask: View
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pick_chose)
        val adapter = PickChoseAdapter(baseContext)
        adapter.setTapBlock { _, index ->
            finish()
            val result: String = adapter.getList()[index]
            resultBlock(result)
        }
        val choice: List<String> = intent.getStringArrayListExtra(KEY_CHOICE_LIST)
        adapter.append(choice, true)
        //初始化控件
        vMask = findViewById(R.id.vMask)
        vMask.setOnClickListener { finish() }
        recyclerView = findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(baseContext)
        recyclerView.adapter = adapter
        //显示动画
        playRecycleViewAnim(true)
    }


    private fun playRecycleViewAnim(shown: Boolean, block: () -> Unit = {}) {
        val animatorSet = AnimatorSet()
        //透明度
        val alphaValues: FloatArray = if (shown) floatArrayOf(0f, 1f) else floatArrayOf(1f, 0f)
        val alpha: ObjectAnimator = ObjectAnimator.ofFloat(vMask, "alpha", *alphaValues)
        //缩放
        val scaleValues: FloatArray = if (shown) floatArrayOf(0.8f, 1f) else floatArrayOf(1f, 0.9f)
        val scaleX: ObjectAnimator = ObjectAnimator.ofFloat(recyclerView, "scaleX", *scaleValues)
        val scaleY: ObjectAnimator = ObjectAnimator.ofFloat(recyclerView, "scaleY", *scaleValues)
        animatorSet.duration = if (shown) 70 else 35
        animatorSet.interpolator = DecelerateInterpolator()
        animatorSet.play(alpha).with(scaleX).with(scaleY)
        if (!shown) {
            animatorSet.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    super.onAnimationEnd(animation)
                    block()
                }
            })
        }
        animatorSet.start()
    }

    override fun finish() {
        playRecycleViewAnim(false) {
            recyclerView.visibility = View.GONE
            vMask.visibility = View.GONE
        }
        vMask.postDelayed({
            super.finish()
            overridePendingTransition(0, 0)
        }, 30)
    }
}