package com.example.chat_app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import de.hdodenhof.circleimageview.CircleImageView
import java.util.*

class MainActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance().currentUser

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.reg_button_edit).setOnClickListener {
            perfomRegister()
        }
        findViewById<TextView>(R.id.already_edit).setOnClickListener {
            Log.d("MainActivity", "success")
            val intent = Intent(this, LoginnActivity::class.java)
            startActivity(intent)
        }
        findViewById<Button>(R.id.profile_button).setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 1001)
        }
    }

    var selectedphotouri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && data != null) {
            Log.d("MainActivity", "Photo was selected")
            selectedphotouri = data.data!!
            findViewById<CircleImageView>(R.id.profile_image).setImageURI(selectedphotouri)
            findViewById<Button>(R.id.profile_button).alpha = 0f
        }
    }

    fun perfomRegister() {
        val email = findViewById<EditText>(R.id.email2_edit).text.toString()
        val password = findViewById<EditText>(R.id.password_edit).text.toString()

        Log.d("MainActivity", "Email is: " + email)
        Log.d("MainActivity", "Password is: $password")

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "please enter a valid userid or password", Toast.LENGTH_SHORT)
                .show()
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) {
                if (!it.isSuccessful)
                    return@addOnCompleteListener
                else {
                    Log.d("MainActivity", it.toString())
                    Log.d(
                        "MainActivity",
                        "user created successfully-UId: ${it.result?.user?.uid}"
                    )

                    uploadimagetoFirebaseStorage()
                    val intent = Intent(this, MainScreen::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)

                    startActivity(intent)
                }

            }
            .addOnFailureListener {
                Log.d("MainActivity", "Failed to create: ${it.message}")
            }

    }

    private fun uploadimagetoFirebaseStorage() {
        if (selectedphotouri == null)
            return
        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedphotouri!!)
            .addOnSuccessListener {
                Log.d("Main", "Succsessfully uploaded image :${it.metadata?.path}")
                ref.downloadUrl.addOnSuccessListener { uri ->
                    Log.d("MainActivity", "FILE LOCATION :$uri")
                    saveusertoFirebaseDatabase(uri.toString())
                }
            }

    }

    private fun saveusertoFirebaseDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid ?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")

        val user =
            User(uid, findViewById<EditText>(R.id.name_edit).text.toString(), profileImageUrl)
        ref.setValue(user)
            .addOnSuccessListener {
                Log.d("MainActivity", "saved user details")
            }
    }

    override fun onStart() {
        super.onStart()
        if (auth != null) {
            startActivity(Intent(this, MainScreen::class.java))
            finish()
        }
    }

}





