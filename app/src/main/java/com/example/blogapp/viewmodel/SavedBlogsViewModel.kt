package com.example.blogapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.data.BlogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SavedBlogsUiState(
    val blogs: List<BlogItemModel> = emptyList(),
    val errorMessage: String? = null
)

/**
 * Notice this reuses repository.observeBlogs() + observeSavedPostIds() — the exact same
 * two streams FeedViewModel uses — and just filters down to the saved ones. The original
 * SavedArticlesActivity re-implemented its own per-post fetch loop with nested
 * CoroutineScope(Dispatchers.IO).launch calls; that entire loop goes away because the
 * repository already gives us every blog plus the saved-ID set as live streams.
 */
class SavedBlogsViewModel(
    private val repository: BlogRepository = BlogRepository()
) : ViewModel() {

    private val _actionError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<SavedBlogsUiState> = combine(
        repository.observeBlogs(),
        repository.observeSavedPostIds(),
        _actionError
    ) { blogs, savedIds, error ->
        val currentUid = repository.currentUserId
        val saved = blogs
            .filter { savedIds.contains(it.postId) }
            .map { blog ->
                blog.copy(
                    isSaved = true,
                    isLikedByCurrentUser = currentUid != null && blog.likedBy?.contains(currentUid) == true
                )
            }
        SavedBlogsUiState(blogs = saved, errorMessage = error)
    }
        .catch { e -> emit(SavedBlogsUiState(errorMessage = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SavedBlogsUiState()
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
        // Tapping "save" here always means "unsave", since everything in this list is
        // already saved (isSaved = true was forced above).
        viewModelScope.launch {
            try {
                repository.toggleSave(blog.postId, alreadySaved = true)
            } catch (e: Exception) {
                _actionError.value = e.message ?: "Failed to update save"
            }
        }
    }
}