package com.example.blogapp

import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.adapter.BlogItemModel
import com.example.blogapp.databinding.ActivitySavedArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SavedArticlesActivity : AppCompatActivity() {

    private val savedBlogArticles = mutableListOf<BlogItemModel>()
    private val binding: ActivitySavedArticlesBinding by lazy {
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // 1. Initialize adapter with the empty list directly (No .filter here)
        blogAdapter = BlogAdapter(savedBlogArticles)
        val recyclerView = binding.savedArticleRecyclerView
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference =
                FirebaseDatabase.getInstance("https://blog-app-e2190-default-rtdb.asia-southeast1.firebasedatabase.app")
                    .getReference("users").child(userId).child("saved")

            userReference.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    savedBlogArticles.clear() // Clear to avoid duplicates on refresh

                    for (postSnapshot in snapshot.children) {
                        val postId = postSnapshot.key
                        val isSaved = postSnapshot.value as? Boolean

                        if (postId != null && isSaved == true) {
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)
                                if (blogItem != null) {
                                    // 2. Explicitly set isSaved so the bookmark shows as filled
                                    blogItem.isSaved = true

                                    launch(Dispatchers.Main) {
                                        savedBlogArticles.add(blogItem)
                                        // 3. Notify the adapter that an item was added
                                        blogAdapter.notifyItemInserted(savedBlogArticles.size - 1)
                                    }
                                }
                            }
                        }
                    }
                }

                private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
                    val blogReference =
                        FirebaseDatabase.getInstance("https://blog-app-e2190-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .getReference("blogs")

                    return try {
                        val dataSnapshot = blogReference.child(postId).get().await()
                        dataSnapshot.getValue(BlogItemModel::class.java)
                    } catch (e: Exception) {
                        null
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(
                        this@SavedArticlesActivity, "Failed to load saved blogs", Toast.LENGTH_SHORT
                    ).show()
                }
            })
        }

        // Set on click listener for back button
        binding.backButton2.setOnClickListener {
            finish()
        }
    }
}