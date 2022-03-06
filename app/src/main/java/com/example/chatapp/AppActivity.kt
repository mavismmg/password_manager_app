package com.example.chatapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_app.*

class AppActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app)

        FirebaseApp.initializeApp(applicationContext)

        logout_button.setOnClickListener {
            signOut()
        }
    }

    private fun signOut() {
        FirebaseAuth.getInstance().signOut()
        Log.d("Logout", "User is logging out")
        finish()
    }
}