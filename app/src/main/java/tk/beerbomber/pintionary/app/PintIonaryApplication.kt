package tk.beerbomber.pintionary.app

import android.app.Application
import io.realm.Realm

class PintIonaryApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        Realm.init(this)
    }
}
