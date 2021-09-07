package com.example.chat_app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class LoginnActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loginn)

        findViewById<Button>(R.id.loginbutton_edit).setOnClickListener {
            val email = findViewById<EditText>(R.id.email2_edit).text.toString()
            val password = findViewById<EditText>(R.id.password2_edit).text.toString()

            Log.d("MainActivity", "attempt a login with username and password: $email")
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnSuccessListener {
                    Log.d("MainActivity", it.toString())
                    Log.d(
                        "MainActivity",
                        "user logged in successfully-UId: ${it.user?.uid}"
                    )
                    val intent= Intent(this,MainScreen::class.java)
                    startActivity(intent)

                }
                .addOnFailureListener {
                    Log.d("MainActivity", "Failed to login: ${it.message}")
                    Toast.makeText(this, "Failed to login:", Toast.LENGTH_SHORT).show()
                }
        }

        findViewById<TextView>(R.id.backto_edit).setOnClickListener {
            finish()

        }
    }
}