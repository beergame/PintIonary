package tk.beerbomber.pintionary.app

import android.support.v7.app.AppCompatActivity
import io.realm.Realm

class MainActivity : AppCompatActivity() {

    override fun onCreate() {
        super.onCreate()
        // Initialize Realm. Should only be done once when the application starts.
        Realm.init(this)
    }
}