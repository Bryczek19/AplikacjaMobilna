package pl.wsei.pam.lab02

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import pl.wsei.pam.lab01.R

class Lab02Activity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lab02)
    }

    fun onClickBoardSize(v: View) {
        val tag = v.tag as String?
        val tokens = tag?.split(" ")
        val rows = tokens?.get(0)?.toInt()
        val columns = tokens?.get(1)?.toInt()

        Toast.makeText(this, "rows: $rows, columns: $columns", Toast.LENGTH_SHORT).show()
    }
}