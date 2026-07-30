package com.example.blogapp.data

import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val DATABASE_URL =
    "https://the-blog-app-157c1-default-rtdb.asia-southeast1.firebasedatabase.app"

/**
 * Single source of truth for all blog/user data access.
 *
 * Nothing outside this class should ever call FirebaseDatabase.getInstance(...) directly.
 * That fixes three problems from the original code in one move:
 *  1) the DB URL was hardcoded in 3 different files
 *  2) Firebase reads were happening inside RecyclerView.onBindViewHolder (re-fired on every
 *     scroll/recycle)
 *  3) listeners were never detached, leaking for the lifetime of the process
 *
 * Every listener here is wrapped in callbackFlow + awaitClose, so when the collecting
 * coroutine scope is cancelled (e.g. the Activity goes through onStop via
 * repeatOnLifecycle), Firebase's removeEventListener is called automatically.
 */
class BlogRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    private val database = FirebaseDatabase.getInstance(DATABASE_URL)
    private val blogsRef: DatabaseReference = database.getReference("blogs")
    private val usersRef: DatabaseReference = database.getReference("users")

    val currentUserId: String?
        get() = auth.currentUser?.uid

    /** Live stream of every blog post, newest first. */
    fun observeBlogs(): Flow<List<BlogItemModel>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val blogs = snapshot.children
                    .mapNotNull { it.getValue(BlogItemModel::class.java) }
                    .sortedByDescending { it.date }
                trySend(blogs)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        blogsRef.addValueEventListener(listener)
        awaitClose { blogsRef.removeEventListener(listener) }
    }

    /**
     * Live stream of the current user's saved post IDs.
     *
     * This replaces the old per-row "is this post saved?" listener that used to fire
     * inside the adapter for every single item. Now it's one listener for the whole list,
     * and the ViewModel maps it onto each blog client-side.
     */
    fun observeSavedPostIds(): Flow<Set<String>> = callbackFlow {
        val uid = currentUserId
        if (uid == null) {
            trySend(emptySet())
            awaitClose { }
            return@callbackFlow
        }
        val savedRef = usersRef.child(uid).child("saved")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.children.mapNotNull { it.key }.toSet())
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        savedRef.addValueEventListener(listener)
        awaitClose { savedRef.removeEventListener(listener) }
    }

    suspend fun fetchUser(uid: String): UserData? =
        usersRef.child(uid).get().await().getValue(UserData::class.java)

    suspend fun addBlog(blog: BlogItemModel) {
        val key = blogsRef.push().key ?: error("Could not generate post id")
        blogsRef.child(key).setValue(blog.copy(postId = key)).await()
    }

    /**
     * "likedBy" already lives on each blog node, so whether the current user has liked a
     * post is derived client-side (see FeedViewModel) instead of firing an extra listener
     * per row like the original adapter did.
     */
    suspend fun toggleLike(postId: String, alreadyLiked: Boolean) {
        val uid = currentUserId ?: error("You must be signed in to like a post")
        val postRef = blogsRef.child(postId)
        val userLikeRef = usersRef.child(uid).child("likes").child(postId)
        val postLikeRef = postRef.child("likes").child(uid)

        if (alreadyLiked) {
            userLikeRef.removeValue().await()
            postLikeRef.removeValue().await()
        } else {
            userLikeRef.setValue(true).await()
            postLikeRef.setValue(true).await()
        }

        // Re-read the counter instead of trusting client-held state, so two devices
        // liking at once don't stomp on each other.
        val currentCount = postRef.child("likecount").get().await().getValue(Int::class.java) ?: 0
        val newCount = (currentCount + if (alreadyLiked) -1 else 1).coerceAtLeast(0)
        postRef.child("likecount").setValue(newCount).await()
    }

    suspend fun toggleSave(postId: String, alreadySaved: Boolean) {
        val uid = currentUserId ?: error("You must be signed in to save a post")
        val savedRef = usersRef.child(uid).child("saved").child(postId)
        if (alreadySaved) savedRef.removeValue().await() else savedRef.setValue(true).await()
    }
}