package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivitySavedArticlesBinding
import com.example.blogapp.viewmodel.SavedBlogsViewModel
import kotlinx.coroutines.launch

class SavedArticlesActivity : AppCompatActivity() {
    private val binding: ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }
    private val viewModel: SavedBlogsViewModel by viewModels()
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        blogAdapter = BlogAdapter(
            onLikeClicked = viewModel::onLikeClicked,
            onSaveClicked = viewModel::onSaveClicked
        )
        binding.savedArticleRecyclerView.apply {
            adapter = blogAdapter
            layoutManager = LinearLayoutManager(this@SavedArticlesActivity)
        }

        binding.backButton2.setOnClickListener { finish() }

        observeUiState()
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    blogAdapter.submitList(state.blogs)
                    state.errorMessage?.let {
                        Toast.makeText(this@SavedArticlesActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}