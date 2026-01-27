package com.example.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.handleCoroutineException


class BlogAdapter(private val items: List<BlogItemModel>) :
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {
    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blog-app-e2190-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreateViewHolder(
        parent: ViewGroup, viewType: Int
    ): BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: BlogViewHolder, position: Int
    ) {
        val blogItem = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId
            val context = binding.root.context

            //bind data to views
            binding.heading.text = blogItemModel.heading
            binding.username.text = blogItemModel.username
            Glide.with(binding.profile.context).load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.date.text = blogItemModel.date
            binding.blogpost.text = blogItemModel.post
            binding.likecount.text = blogItemModel.likecount.toString()


            //set on click listener
            binding.root.setOnClickListener {
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)

            }

            //check if user has liked the blog or not and update the like button image
            val postLikeReference: DatabaseReference =
                databaseReference.child("blogs").child(postId).child("likes")
            val currentUserLiked = currentUser?.uid.let { uid ->
                postLikeReference.child(uid ?: "")
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            if (snapshot.exists()) {
                                binding.likebutton.setImageResource(R.drawable.red_heart_icon)
                            } else {
                                binding.likebutton.setImageResource(R.drawable.black_heart_icon)
                            }
                        }

                        override fun onCancelled(error: DatabaseError) {
                        }

                    })


            }

            //handle like button clicks
            binding.likebutton.setOnClickListener {
                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding)

                } else {
                    Toast.makeText(context, "Please login to like the blog", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            //handle save button clicks
            binding.savebutton.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Please login to save the blog", Toast.LENGTH_SHORT)
                        .show()
                }
            }

        }

    }

    private fun handleLikeButtonClicked(
        postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        //user has already liked the post then unlike it
        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        userReference.child("likes").child(postId).removeValue()
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).removeValue()
                                blogItemModel.likedBy?.remove(currentUser.uid)
                                updateLikeButtonImage(binding, false)

                                //decrement in likes in database
                                val newLikeCount = blogItemModel.likecount - 1
                                blogItemModel.likecount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likecount")
                                    .setValue(newLikeCount)
                                binding.likecount.text = newLikeCount.toString()
                                updateLikeButtonImage(binding, true)

                            }

                            .addOnFailureListener { e ->

                                Log.e("LikeClicked", "onDataChange: Failed to unlike the block $e")

                            }
                    } else {
                        //user has not liked the post then like it
                        userReference.child("likes").child(postId).setValue(true)
                            .addOnSuccessListener {
                                postLikeReference.child(currentUser.uid).setValue(true)
                                blogItemModel.likedBy?.add(currentUser.uid)
                                updateLikeButtonImage(binding, true)

                                //Increment the like in count databse
                                val newLikeCount = blogItemModel.likecount + 1
                                blogItemModel.likecount = newLikeCount
                                databaseReference.child("blogs").child(postId).child("likecount")
                                    .setValue(newLikeCount)
                                notifyDataSetChanged()

                            }.addOnFailureListener { e ->

                                Log.e("LikeClicked", "onDataChange: Failed to like the block $e")

                            }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })
    }


    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {

        binding.likebutton.setImageResource(if (liked) R.drawable.red_heart_icon else R.drawable.black_heart_icon)


    }

    private fun handleSaveButtonClicked(
        postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding
    ) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saved").child(postId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists()) {
                        // the blog is currently saved then unsave it
                        userReference.child("saved").child(postId).removeValue()
                            .addOnSuccessListener {
                                //update the UI
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = false
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog unsaved", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {

                                val context = binding.root.context
                                Toast.makeText(context, "Failed to unsave blog", Toast.LENGTH_SHORT)
                                    .show()

                            }

                        binding.savebutton.setImageResource(R.drawable.bookmark_icon)
                    } else {
                        //the blog is not saved then save it
                        userReference.child("saved").child(postId).setValue(true)
                            .addOnSuccessListener {
                                //update the UI
                                val clickedBlogItem = items.find { it.postId == postId }
                                clickedBlogItem?.isSaved = true
                                notifyDataSetChanged()

                                val context = binding.root.context
                                Toast.makeText(context, "Blog saved", Toast.LENGTH_SHORT).show()
                            }.addOnFailureListener {
                                val context = binding.root.context
                                Toast.makeText(context, "Failed to save blog", Toast.LENGTH_SHORT)
                                    .show()
                            }

                        //change the save button image
                        binding.savebutton.setImageResource(R.drawable.bookmark3_icon)

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }

            })


    }
}
