package com.example.blogapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.data.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class FeedUiState(
    val blogs: List<BlogItemModel> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null
)

class FeedViewModel(
    private val repository: BlogRepository = BlogRepository()
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)

    // combine() merges the two live Firebase streams into one UI-ready state.
    // This is what replaces the adapter's old per-item "check if liked / check if saved"
    // listeners entirely — likedBy already travels with each blog, and savedIds is a
    // single stream for the whole list.
    val uiState: StateFlow<FeedUiState> = combine(
        repository.observeBlogs(),
        repository.observeSavedPostIds(),
        _actionError
    ) { blogs, savedIds, error ->
        val currentUid = repository.currentUserId
        val merged = blogs.map { blog ->
            blog.copy(
                isSaved = savedIds.contains(blog.postId),
                isLikedByCurrentUser = currentUid != null && blog.likedBy?.contains(currentUid) == true
            )
        }
        FeedUiState(blogs = merged, isLoading = false, errorMessage = error)
    }
        .catch { e -> emit(FeedUiState(isLoading = false, errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            // WhileSubscribed keeps the Firebase listeners alive for 5s after the last
            // observer disappears, so a quick screen rotation doesn't tear down and
            // immediately re-fetch the whole feed.
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FeedUiState()
        )

    fun onLikeClicked(blog: BlogItemModel) {
        viewModelScope.launch {
            try {
                repository.toggleLike(blog.postId, blog.isLikedByCurrentUser)
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Failed to update like"
            }
        }
    }

    fun onSaveClicked(blog: BlogItemModel) {
        viewModelScope.launch {
            try {
                repository.toggleSave(blog.postId, blog.isSaved)
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Failed to update save"
            }
        }
    }

    suspend fun fetchUserProfile(uid: String): UserData? = repository.fetchUser(uid)
}