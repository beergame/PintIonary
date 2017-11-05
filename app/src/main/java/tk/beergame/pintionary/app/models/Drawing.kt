package tk.beergame.pintionary.app.models

import io.realm.RealmObject
import io.realm.RealmList

open class Drawing : RealmObject() {
    var completed: Boolean = false
    var color: String? = null
    var drawingPoints: RealmList<DrawingPoint> = RealmList()
}
