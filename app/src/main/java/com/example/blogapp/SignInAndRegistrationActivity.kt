package com.example.blogapp

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import com.example.blogapp.databinding.ActivitySignInAndRegistrationBinding

class SignInAndRegistrationActivity : AppCompatActivity() {

    private val binding: ActivitySignInAndRegistrationBinding by lazy{
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val action = intent.getStringExtra("action")

        //adjust visibility for login
        if(action=="login"){
            binding.loginEmail.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE
            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f
            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f

            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE
            binding.registerName.visibility = View.GONE

        }
        else if(action == "register"){
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

        }

    }
}