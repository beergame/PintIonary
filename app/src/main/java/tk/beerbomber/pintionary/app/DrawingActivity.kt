package tk.beerbomber.pintionary.app

import tk.beerbomber.pintionary.app.models.Drawing
import tk.beerbomber.pintionary.app.models.DrawingPoint
import android.os.Bundle
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.view.View
import android.support.v7.app.AppCompatActivity
import android.util.Log
import io.realm.Realm
import io.realm.ObjectServerError
import io.realm.SyncConfiguration
import io.realm.SyncUser
import io.realm.SyncCredentials

class DrawingActivity : AppCompatActivity(), SurfaceHolder.Callback, View.OnClickListener {
    private val REALM_URL = "realm://" + BuildConfig.OBJECT_SERVER_IP + ":9080/~/Draw"
    private val AUTH_URL = "http://" + BuildConfig.OBJECT_SERVER_IP + ":9080/auth"
    private val ID = "victor.balssa@gmail.com"
    private val PASSWORD = "testtest"
    private val EDGE_WIDTH = 683

    private var realm: Realm? = null
    private var surfaceView: SurfaceView? = null
    private var ratio = 1.0
    private var marginLeft: Double = 0.toDouble()
    private var marginTop: Double = 0.toDouble()
    private var drawThread: DrawThread? = null
    private var currentColor = "Charcoal"
    private var currentDraw: Drawing? = Drawing()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val syncCredentials = SyncCredentials.usernamePassword(ID, PASSWORD, false)
        SyncUser.loginAsync(syncCredentials, AUTH_URL, object : SyncUser.Callback {
            override fun onSuccess(user: SyncUser) {
                val syncConfiguration = SyncConfiguration.Builder(user, REALM_URL).build()
                Realm.setDefaultConfiguration(syncConfiguration)
                realm = Realm.getDefaultInstance()
            }

            override fun onError(error: ObjectServerError) {}
        })

        surfaceView = findViewById(R.id.surface_view) as SurfaceView?
        if (drawThread == null) {
            Log.i("thread", "Thread creation")
            drawThread = DrawThread(realm, surfaceView)
            drawThread!!.start()
            Log.i("thread", "Thread started")
        }
    }

    private fun wipeCanvas() {
        if (realm != null) {
            realm!!.executeTransactionAsync(Realm.Transaction { r -> r.deleteAll() })
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
            val x: Float = event.rawX
            val y: Float = event.rawY
            val pointX: Double = (x - marginLeft - viewLocation[0]) * ratio
            val pointY: Double = (y - marginTop - viewLocation[1]) * ratio

            when (action) {
                MotionEvent.ACTION_DOWN -> {
                    realm!!.beginTransaction()
                    currentDraw = realm!!.createObject(Drawing::class.java)
                    currentDraw!!.color = currentColor
                    Log.i("thread", currentColor)
                    val point: DrawingPoint = realm!!.createObject(DrawingPoint::class.java)
                    point.x = pointX
                    point.y = pointY
                    currentDraw!!.drawingPoints.add(point)
                    realm!!.commitTransaction()
                }
                MotionEvent.ACTION_MOVE -> {
                    realm!!.beginTransaction()
                    val point = realm!!.createObject(DrawingPoint::class.java)
                    point.x = pointX
                    point.y = pointY
                    currentDraw!!.drawingPoints.add(point)
                    realm!!.commitTransaction()
                }
                MotionEvent.ACTION_UP -> {
                    realm!!.beginTransaction()
                    currentDraw!!.completed = true
                    val point = realm!!.createObject(DrawingPoint::class.java)
                    point.x = pointX
                    point.y = pointY
                    currentDraw!!.drawingPoints.add(point)
                    realm!!.commitTransaction()
                    currentDraw = null
                }
                else -> {
                    realm!!.beginTransaction()
                    currentDraw!!.completed = true
                    realm!!.commitTransaction()
                    currentDraw = null
                }
            }
            return true

        }
        return false
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        if (drawThread == null) {
            Log.i("thread", "Thread creation")
            drawThread = DrawThread(realm as Realm, surfaceView as SurfaceView)
            drawThread!!.start()
            Log.i("thread", "Thread started")
        }
    }

    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
        if (drawThread != null) {
            drawThread!!.shutdown()
            drawThread = null
        }
        ratio = -1.0
    }

    override fun surfaceChanged(surfaceHolder: SurfaceHolder, format: Int, width: Int, height: Int) {
        val isPortrait: Boolean = width < height
        ratio = if (isPortrait) {
            ( EDGE_WIDTH / height ).toDouble()
        } else {
            ( EDGE_WIDTH / width ).toDouble()
        }
        if (isPortrait) {
            marginLeft = (width - height) / 2.0
            marginTop = (0).toDouble()
        } else {
            marginLeft = (0).toDouble()
            marginTop = (height - width) / 2.0
        }
    }

    override fun onClick(view: View) {
        currentColor = "Charcoal"
    }
}
