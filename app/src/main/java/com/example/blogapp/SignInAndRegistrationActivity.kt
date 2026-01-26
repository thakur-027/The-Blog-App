package com.example.blogapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivitySignInAndRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInAndRegistrationActivity : AppCompatActivity() {

    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage: FirebaseStorage
    private val PICK_IMAGE_REQUEST = 1
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        // Initialize Firebase services
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://blog-app-e2190-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        val action = intent.getStringExtra("action")

        if (action == "login") {
            setupLoginUI()
        } else if (action == "register") {
            setupRegisterUI()
        }

        // Set onClickListener for the profile image selection
        binding.cardView.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "select image"), PICK_IMAGE_REQUEST)
        }
    }

    private fun setupLoginUI() {
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

        binding.loginButton.setOnClickListener {
            val email = binding.loginEmail.text.toString().trim()
            val password = binding.loginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Login Successful 😏", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        } else {
                            Toast.makeText(this, "Login Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    private fun setupRegisterUI() {
        binding.loginButton.isEnabled = false
        binding.loginButton.alpha = 0.5f

        binding.registerButton.setOnClickListener {
            val name = binding.registerName.text.toString().trim()
            val email = binding.registerEmail.text.toString().trim()
            val password = binding.registerPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || imageUri == null) {
                // FIX: Check imageUri to prevent crash
                val message = if (imageUri == null) "Please select an image" else "Please fill all fields"
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            } else {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            user?.let { firebaseUser ->
                                val userId = firebaseUser.uid
                                val userReference = database.getReference("users")
                                val userData = UserData(name, email)

                                // Save basic data
                                userReference.child(userId).setValue(userData)

                                // Upload image
                                val storageReference = storage.reference.child("profile_image/$userId.jpg")
                                storageReference.putFile(imageUri!!)
                                    .addOnSuccessListener {
                                        storageReference.downloadUrl.addOnSuccessListener { uri ->
                                            userReference.child(userId).child("profileImage").setValue(uri.toString())
                                        }
                                    }

                                // FIX: Sign out so they have to login
                                auth.signOut()

                                Toast.makeText(this, "Registered Successfully! Now Login.", Toast.LENGTH_SHORT).show()

                                // FIX: Redirect to Login state
                                val intent = Intent(this, SignInAndRegistrationActivity::class.java)
                                intent.putExtra("action", "login")
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data?.data != null) {
            imageUri = data.data
            Glide.with(this)
                .load(imageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.registerUserImage)
        }
    }
}