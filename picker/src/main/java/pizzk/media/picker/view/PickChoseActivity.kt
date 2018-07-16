package pizzk.media.picker.view

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
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
            activity.overridePendingTransition(0, 0)
            activity.startActivity(intent)
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
        //
        vMask = findViewById(R.id.vMask)
        vMask.setOnClickListener { finish() }
        recyclerView = findViewById(R.id.recycleView)
        recyclerView.layoutManager = LinearLayoutManager(baseContext)
        recyclerView.adapter = adapter
    }
}