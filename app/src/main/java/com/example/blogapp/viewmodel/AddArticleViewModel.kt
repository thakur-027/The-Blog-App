package com.example.blogapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.data.BlogRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

sealed class SaveBlogResult {
    data object Idle : SaveBlogResult()
    data object Saving : SaveBlogResult()
    data object Success : SaveBlogResult()
    data class ValidationError(val message: String) : SaveBlogResult()
    data class Failure(val message: String) : SaveBlogResult()
}

class AddArticleViewModel(
    private val repository: BlogRepository = BlogRepository(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _saveResult = MutableStateFlow<SaveBlogResult>(SaveBlogResult.Idle)
    val saveResult: StateFlow<SaveBlogResult> = _saveResult.asStateFlow()

    fun saveBlog(title: String, description: String) {
        if (title.isBlank() || description.isBlank()) {
            // THIS is the fix for the original bug: the original code showed a Toast here
            // and then kept executing, saving an empty blog anyway. Returning here stops it.
            _saveResult.value = SaveBlogResult.ValidationError("Please fill all the fields")
            return
        }

        val user = auth.currentUser
        if (user == null) {
            _saveResult.value = SaveBlogResult.ValidationError("You must be signed in to post")
            return
        }

        viewModelScope.launch {
            _saveResult.value = SaveBlogResult.Saving
            try {
                val userData = repository.fetchUser(user.uid)
                val blog = BlogItemModel(
                    heading = title,
                    username = userData?.name ?: user.displayName ?: "Anonymous",
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date()),
                    post = description,
                    userId = user.uid,
                    likecount = 0,
                    profileImage = userData?.profileImage ?: ""
                )
                repository.addBlog(blog)
                _saveResult.value = SaveBlogResult.Success
            } catch (e: Exception) {
                _saveResult.value = SaveBlogResult.Failure(e.message ?: "Failed to add blog")
            }
        }
    }
}