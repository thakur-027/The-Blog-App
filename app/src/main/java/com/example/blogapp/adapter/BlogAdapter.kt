package com.example.blogapp.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.R
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding

private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<BlogItemModel>() {
    override fun areItemsTheSame(oldItem: BlogItemModel, newItem: BlogItemModel) =
        oldItem.postId == newItem.postId

    override fun areContentsTheSame(oldItem: BlogItemModel, newItem: BlogItemModel) =
        oldItem == newItem
}

/**
 * Pure UI adapter. It renders whatever state the ViewModel hands it and forwards taps
 * upward via lambdas — it never talks to Firebase directly.
 *
 * That fixes the original three problems in BlogAdapter:
 *  - Firebase reads firing on every onBindViewHolder (i.e. every scroll)
 *  - notifyDataSetChanged() redrawing the whole list on a single like/save toggle
 *  - currentUser!!.uid crashing if the session ends while the list is on screen
 *    (auth is no longer touched here at all)
 */
class BlogAdapter(
    private val onLikeClicked: (BlogItemModel) -> Unit,
    private val onSaveClicked: (BlogItemModel) -> Unit
) : ListAdapter<BlogItemModel, BlogAdapter.BlogViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val binding = BlogItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: BlogItemModel) {
            binding.heading.text = item.heading
            binding.username.text = item.username
            binding.date.text = item.date
            binding.blogpost.text = item.post
            binding.likecount.text = item.likecount.toString()

            Glide.with(binding.profile).load(item.profileImage).into(binding.profile)

            binding.likebutton.setImageResource(
                if (item.isLikedByCurrentUser) R.drawable.red_heart_icon else R.drawable.black_heart_icon
            )
            binding.savebutton.setImageResource(
                if (item.isSaved) R.drawable.bookmark3_icon else R.drawable.bookmark_icon
            )

            binding.likebutton.setOnClickListener { onLikeClicked(item) }
            binding.savebutton.setOnClickListener { onSaveClicked(item) }

            binding.root.setOnClickListener {
                val context = binding.root.context
                context.startActivity(
                    Intent(context, ReadMoreActivity::class.java).apply {
                        putExtra("blogItem", item)
                    }
                )
            }
        }
    }
}