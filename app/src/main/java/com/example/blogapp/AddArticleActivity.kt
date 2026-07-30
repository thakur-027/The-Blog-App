package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.example.blogapp.viewmodel.AddArticleViewModel
import com.example.blogapp.viewmodel.SaveBlogResult
import kotlinx.coroutines.launch

class AddArticleActivity : AppCompatActivity() {
    private val binding: ActivityAddArticleBinding by lazy { ActivityAddArticleBinding.inflate(layoutInflater) }
    private val viewModel: AddArticleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.backButton1.setOnClickListener { finish() }

        binding.saveBlogButton.setOnClickListener {
            val title = binding.blogTitle.editText?.text.toString().trim()
            val description = binding.blogDescription.editText?.text.toString().trim()
            viewModel.saveBlog(title, description)
        }

        observeSaveResult()
    }

    private fun observeSaveResult() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveResult.collect { result ->
                    when (result) {
                        is SaveBlogResult.ValidationError ->
                            Toast.makeText(this@AddArticleActivity, result.message, Toast.LENGTH_SHORT).show()
                        is SaveBlogResult.Failure ->
                            Toast.makeText(this@AddArticleActivity, result.message, Toast.LENGTH_SHORT).show()
                        SaveBlogResult.Success -> finish()
                        SaveBlogResult.Saving, SaveBlogResult.Idle -> Unit
                    }
                }
            }
        }
    }
}