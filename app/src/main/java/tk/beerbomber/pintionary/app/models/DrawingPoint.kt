package tk.beerbomber.pintionary.app.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass

@RealmClass
class DrawingPoint : RealmObject() {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    open var id: Int = 0

    @SerializedName("x")
    @Expose
    var x: Double = 0.toDouble()

    @SerializedName("y")
    @Expose
    var y: Double = 0.toDouble()
}