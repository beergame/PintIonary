package tk.beergame.pintionary.app

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.*
import android.content.Intent

/**
 * Class MainActivity.
 */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_login)

        // get reference to all views
        val id = findViewById(R.id.edLogin) as EditText
        val password = findViewById(R.id.edPassword) as EditText
        val btnSubmit = findViewById(R.id.tvConnect) as TextView

        btnSubmit.setOnClickListener {
            val myIntent = Intent(this@MainActivity, DrawActivity::class.java)
            myIntent.putExtra("id", id.text.toString())
            myIntent.putExtra("password", password.text.toString())
            this@MainActivity.startActivity(myIntent)
        }
    }
}