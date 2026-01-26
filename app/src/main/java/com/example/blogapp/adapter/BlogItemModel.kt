package com.example.blogapp.adapter

data class BlogItemModel(
    val heading: String = "null",
    val username: String = "null",
    val date: String = "null",
    val post: String = "null",
    val likecount: Int = 0,
    val profileImage: String = "null"
)
