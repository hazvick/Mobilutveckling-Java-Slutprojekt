package com.example.slutprojekt_mobilutveckling

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_register.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        edit_button_login.setOnClickListener {
            val email = edit_email_login.text.toString()
            val password = edit_password_login.text.toString()

            Log.d("LoginActivity", "Executing login function.")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password).isSuccessful

            val intent = Intent(this, LatestMessagesActivity::class.java)
            startActivity(intent)
        }

        edit_register_account.setOnClickListener {
            finish()
        }
    }

}