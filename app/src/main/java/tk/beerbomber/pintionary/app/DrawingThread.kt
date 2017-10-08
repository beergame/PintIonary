package tk.beerbomber.pintionary.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.Log
import io.realm.Realm
import android.view.SurfaceView
import tk.beerbomber.pintionary.app.models.Drawing

class DrawThread(val realm: Realm?, private val surfaceView: SurfaceView?) : Thread() {
    private var bgRealm: Realm? = null
    private val ratio = 1.0
    private val marginLeft: Double = 0.toDouble()
    private val marginTop: Double = 0.toDouble()

    fun shutdown() {
        synchronized(this) {
            bgRealm!!.stopWaitForChange()
        }
        interrupt()
    }

    override fun run() {
        Log.i("Run draw thread", "true")
        while (ratio < 0 && !isInterrupted) {
        }

        Log.i("debug", "00001")
        if (isInterrupted) {
            return
        }
        Log.i("debug", "00002")

        var canvas = Canvas()

        try {
            val holder = surfaceView!!.holder
            canvas = holder.lockCanvas()
            canvas.drawColor(Color.WHITE)
            Log.i("debug", "00003")
        } finally {
            surfaceView!!.holder.unlockCanvasAndPost(canvas)
        }
        Log.i("debug", "00004")

        if (isInterrupted) {
            return
        }
        Log.i("debug", "00005")

        bgRealm = Realm.getDefaultInstance()
        val results = bgRealm!!.where(Drawing::class.java).findAll()

        while (!isInterrupted) {
            try {
                Log.i("debug", "00006")
                val holder = surfaceView!!.holder
                canvas = holder.lockCanvas()
                synchronized(holder) {
                    Log.i("debug", "00007")
                    canvas.drawColor(Color.WHITE)
                    val paint = Paint()
                    for (drawing in results) {
                        val points = drawing.drawingPoints
                        paint.color = 1
                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = (4 / ratio).toFloat()
                        val iterator = points.iterator()
                        val firstPoint = iterator.next()
                        val path = Path()
                        val firstX = (firstPoint.x / ratio + marginLeft).toFloat()
                        val firstY = (firstPoint.y / ratio + marginTop).toFloat()
                        Log.i("drawingPoints X", firstX.toString())
                        Log.i("drawingPoints Y", firstY.toString())
                        path.moveTo(firstX, firstY)
                        while (iterator.hasNext()) {
                            val point = iterator.next()
                            val x = (point.x / ratio + marginLeft).toFloat()
                            val y = (point.y / ratio + marginTop).toFloat()
                            path.lineTo(x, y)
                        }
                        canvas.drawPath(path, paint)
                    }
                }
            } finally {
                surfaceView!!.holder.unlockCanvasAndPost(canvas)
            }
            bgRealm!!.waitForChange()
        }
    }
}

