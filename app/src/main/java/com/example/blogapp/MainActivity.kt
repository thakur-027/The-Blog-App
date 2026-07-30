package com.example.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivityMainBinding
import com.example.blogapp.viewmodel.FeedViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val binding: ActivityMainBinding by lazy { ActivityMainBinding.inflate(layoutInflater) }
    private val viewModel: FeedViewModel by viewModels()
    private lateinit var blogAdapter: BlogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.saveArticleButton.setOnClickListener {
            startActivity(Intent(this, SavedArticlesActivity::class.java))
        }
        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this, AddArticleActivity::class.java))
        }

        blogAdapter = BlogAdapter(
            onLikeClicked = viewModel::onLikeClicked,
            onSaveClicked = viewModel::onSaveClicked
        )
        binding.BlogRecyclerView.apply {
            adapter = blogAdapter
            layoutManager = LinearLayoutManager(this@MainActivity)
        }

        loadUserProfileImage()
        observeUiState()
    }

    private fun observeUiState() {
        // repeatOnLifecycle(STARTED) starts collecting when the Activity becomes visible
        // and cancels the coroutine when it stops. Because BlogRepository's flows are
        // built with callbackFlow + awaitClose, cancelling this coroutine automatically
        // detaches the underlying Firebase ValueEventListeners — the leak from the
        // original MainActivity is gone without writing a single removeEventListener call.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    blogAdapter.submitList(state.blogs)
                    state.errorMessage?.let {
                        Toast.makeText(this@MainActivity, it, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun loadUserProfileImage() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        lifecycleScope.launch {
            val user = viewModel.fetchUserProfile(uid)
            user?.profileImage?.takeIf { it.isNotBlank() }?.let {
                Glide.with(this@MainActivity).load(it).into(binding.profileImage)
            }
        }
    }
}