package pizzk.media.picker.demo

import android.app.Application
import android.util.Log

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Thread.UncaughtExceptionHandler { t, e ->
            e.printStackTrace()
            Log.e("", e.message)
        }
    }
}