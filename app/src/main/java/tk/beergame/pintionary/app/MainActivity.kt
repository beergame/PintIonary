
package tk.beergame.pintionary.app

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Button
import android.widget.Toast
import java.util.HashMap
import io.realm.ErrorCode
import io.realm.ObjectServerError
import io.realm.Realm
import io.realm.SyncConfiguration
import io.realm.SyncCredentials
import io.realm.SyncUser
import tk.beergame.pintionary.app.models.Drawing
import tk.beergame.pintionary.app.models.DrawingPoint


class MainActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private var realm: Realm? = null
    private var surfaceView: SurfaceView? = null
    private var ratio = -1.0
    private var marginLeft: Double = 0.toDouble()
    private var marginTop: Double = 0.toDouble()
    private var drawThread: DrawThread? = null
    private var color = "Charcoal"
    private var currentPath: Drawing? = null
    private val nameToColorMap = HashMap<String, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        createUserIfNeededAndAndLogin()

        surfaceView = findViewById(R.id.surface_view) as SurfaceView
        surfaceView!!.holder.addCallback(this@MainActivity)

        val btnClean = findViewById(R.id.btn_clean) as Button
        btnClean.setOnClickListener {
            wipeCanvas()
            Toast.makeText(this@MainActivity, "Draw cleaned", Toast.LENGTH_SHORT).show()
        }
    }

    private fun createUserIfNeededAndAndLogin() {
        val syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false)

        SyncUser.loginAsync(syncCredentials, AUTH_URL, object : SyncUser.Callback<SyncUser> {
            override fun onSuccess(user: SyncUser) {
                val syncConfiguration = SyncConfiguration.Builder(user, REALM_URL).build()
                Realm.setDefaultConfiguration(syncConfiguration)
                realm = Realm.getDefaultInstance()
            }

            override fun onError(error: ObjectServerError) {
                if (error.errorCode == ErrorCode.INVALID_CREDENTIALS) {
                    SyncUser.loginAsync(SyncCredentials.usernamePassword(ID, PASSWORD, true), AUTH_URL, this)
                } else {
                    val errorMsg = "$error.errorCode, $error.errorMessage"
                    Toast.makeText(applicationContext, errorMsg, Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun wipeCanvas() {
        if (realm != null) {
            realm!!.executeTransactionAsync { r -> r.deleteAll() }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (realm != null) {
            realm!!.close()
            realm = null
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {

        if (realm == null) {
            return false
        }

        val viewLocation = IntArray(2)
        surfaceView!!.getLocationInWindow(viewLocation)
        val action = event.action
        if (action == MotionEvent.ACTION_DOWN
                || action == MotionEvent.ACTION_MOVE
                || action == MotionEvent.ACTION_UP
                || action == MotionEvent.ACTION_CANCEL) {
            val x = event.rawX
            val y = event.rawY
            val pointX = (x.toDouble() - marginLeft - viewLocation[0].toDouble()) * ratio
            val pointY = (y.toDouble() - marginTop - viewLocation[1].toDouble()) * ratio

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    realm!!.beginTransaction()
                    currentPath = realm!!.createObject(Drawing::class.java)
                    currentPath!!.color = color
                    val point = realm!!.createObject<DrawingPoint>(DrawingPoint::class.java)
                    point.x = pointX
                    point.y = pointY
                    currentPath!!.drawingPoints.add(point)
                    realm!!.commitTransaction()
                }
                MotionEvent.ACTION_MOVE -> {
                    realm!!.beginTransaction()
                    val point = realm!!.createObject<DrawingPoint>(DrawingPoint::class.java)
                    point.x = pointX
                    point.y = pointY
                    currentPath!!.drawingPoints.add(point)
                    realm!!.commitTransaction()
                }
                MotionEvent.ACTION_UP -> {
                    realm!!.beginTransaction()
                    currentPath!!.completed = true
                    val point = realm!!.createObject<DrawingPoint>(DrawingPoint::class.java)
                    point.x = pointX
                    point.y = pointY
                    currentPath!!.drawingPoints.add(point)
                    realm!!.commitTransaction()
                    currentPath = null
                }
                else -> {
                    realm!!.beginTransaction()
                    currentPath!!.completed = true
                    realm!!.commitTransaction()
                    currentPath = null
                }
            }
            return true

        }
        return false
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        if (drawThread == null) {
            drawThread = DrawThread()
            drawThread!!.start()
        }
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val isPortrait = width < height
        ratio = if (isPortrait) {
            EDGE_WIDTH.toDouble() / height
        } else {
            EDGE_WIDTH.toDouble() / width
        }
        if (isPortrait) {
            marginLeft = (width - height) / 2.0
            marginTop = 0.0
        } else {
            marginLeft = 0.0
            marginTop = (height - width) / 2.0
        }
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (drawThread != null) {
            drawThread!!.shutdown()
            drawThread = null
        }
        ratio = -1.0
    }

    internal inner class DrawThread : Thread() {
        private var bgRealm: Realm? = null

        fun shutdown() {
            synchronized(this) {
                if (bgRealm != null) {
                    bgRealm!!.stopWaitForChange()
                }
            }
            interrupt()
        }

        override fun run() {
            while (ratio < 0 && !isInterrupted) {
            }

            if (isInterrupted) {
                return
            }

            var canvas: Canvas? = null

            try {
                val holder = surfaceView!!.holder
                canvas = holder.lockCanvas()
                canvas!!.drawColor(Color.WHITE)
            } finally {
                if (canvas != null) {
                    surfaceView!!.holder.unlockCanvasAndPost(canvas)
                }
            }

            while (realm == null && !isInterrupted) {
            }

            if (isInterrupted) {
                return
            }

            bgRealm = Realm.getDefaultInstance()
            val results = bgRealm!!.where<Drawing>(Drawing::class.java).findAll()

            while (!isInterrupted) {
                try {
                    val holder = surfaceView!!.holder
                    canvas = holder.lockCanvas()

                    synchronized(holder) {
                        canvas!!.drawColor(Color.WHITE)
                        val paint = Paint()
                        for (drawing in results) {
                            val points = drawing.drawingPoints
                            val color = nameToColorMap[drawing.color]
                            if (color != null) {
                                paint.color = color
                            } else {
                                paint.color = -0xe3d7c1
                            }
                            paint.style = Paint.Style.STROKE
                            paint.strokeWidth = (4 / ratio).toFloat()
                            val iterator = points.iterator()
                            val firstPoint = iterator.next()
                            val path = Path()
                            val firstX = (firstPoint.x / ratio + marginLeft).toFloat()
                            val firstY = (firstPoint.y / ratio + marginTop).toFloat()
                            path.moveTo(firstX, firstY)
                            while (iterator.hasNext()) {
                                val point = iterator.next()
                                val x = (point.x / ratio + marginLeft).toFloat()
                                val y = (point.y / ratio + marginTop).toFloat()
                                path.lineTo(x, y)
                            }
                            canvas!!.drawPath(path, paint)
                        }
                    }
                } finally {
                    if (canvas != null) {
                        surfaceView!!.holder.unlockCanvasAndPost(canvas)
                    }
                }
                bgRealm!!.waitForChange()
            }

            synchronized(this) {
                bgRealm!!.close()
            }
        }
    }

    /**
     * Realm object server identification.
     */
    companion object {
        private val REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/Draw"
        private val AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth"
        private val ID = "balssa_v@etna-alternance.net"
        private val PASSWORD = "testtest"
        private val EDGE_WIDTH = 683
    }
}
