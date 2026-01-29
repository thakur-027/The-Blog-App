package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadMoreBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs = intent.getParcelableExtra<BlogItemModel>("blogItem")

        if(blogs != null){

            //retrieve user related data
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.username
            binding.blogDate.text = blogs.date
            binding.blogDescriptionTextview.text = blogs.post

            Glide.with(this)
                .load(blogs.profileImage)
                .placeholder(R.drawable.malepfp)
                .error(R.drawable.malepfp)
                .centerCrop()
                .into(binding.profileImage)


        }
        else{
            Toast.makeText(this, "No data found", Toast.LENGTH_SHORT).show()
        }


    }
}