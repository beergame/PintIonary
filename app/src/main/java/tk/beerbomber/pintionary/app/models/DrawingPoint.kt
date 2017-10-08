package tk.beerbomber.pintionary.app.models

import io.realm.RealmObject

open class DrawingPoint : RealmObject() {
    var x: Double = 0.toDouble()
    var y: Double = 0.toDouble()
}
