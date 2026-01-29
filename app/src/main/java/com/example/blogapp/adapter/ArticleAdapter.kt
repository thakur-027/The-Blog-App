package com.example.blogapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityArticlesBinding
import com.example.blogapp.databinding.ArticleItemBinding
import android.widget.AdapterView.OnItemClickListener

class ArticleAdapter(
    private val context: Context,
    private var blogList: List<BlogItemModel>,
    private val itemClickListener: OnItemClickListener
): RecyclerView.Adapter<ArticleAdapter.BlogViewHolder>() {

    interface OnItemClickListener {
        fun onEditClick(blogItem: BlogItemModel)
        fun onDeleteClick(blogItem: BlogItemModel)
        fun onReadMoreClick(blogItem: BlogItemModel)
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ArticleAdapter.BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ArticleItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)

    }

    override fun onBindViewHolder(holder: ArticleAdapter.BlogViewHolder, position: Int) {
        val blogItem = blogList[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return blogList.size
    }

    fun setData(blogSavedList: ArrayList<BlogItemModel>) {
        this.blogList = blogSavedList
        notifyDataSetChanged()
    }

    inner class BlogViewHolder(private val binding : ArticleItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItem: BlogItemModel) {

            binding.heading.text = blogItem.heading
            binding.username.text = blogItem.username
            Glide.with(binding.profile.context).load(blogItem.profileImage)
                .into(binding.profile)
            binding.date.text = blogItem.date
            binding.blogpost.text = blogItem.post

            //handle read more
            binding.readmorebutton.setOnClickListener {
                itemClickListener.onReadMoreClick(blogItem)
            }

            //handle edit
            binding.editButton.setOnClickListener {
                itemClickListener.onEditClick(blogItem)
            }

            //handle delete
            binding.deleteButton.setOnClickListener {
                itemClickListener.onDeleteClick(blogItem)
            }

        }

    }

}