package tk.beerbomber.pintionary.app.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import io.realm.RealmObject
import io.realm.RealmList
import io.realm.annotations.PrimaryKey
import io.realm.annotations.RealmClass
import  tk.beerbomber.pintionary.app.models.DrawingPoint

@RealmClass
open class Drawing : RealmObject() {

    @PrimaryKey
    @SerializedName("id")
    @Expose
    open var id: Int = 0

    @SerializedName("completed")
    @Expose
    private var completed: Boolean = false

    @SerializedName("color")
    @Expose
    private var color: String? = null

    @SerializedName("draw_points")
    @Expose
    private var drawingPoints: RealmList<DrawingPoint> = RealmList()
}