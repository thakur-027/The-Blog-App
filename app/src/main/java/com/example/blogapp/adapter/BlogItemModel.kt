package com.example.blogapp.adapter

import android.R
import android.os.Parcel
import android.os.Parcelable

data class BlogItemModel(
    val heading: String = "null",
    val username: String = "null",
    val date: String = "null",
    val post: String = "null",
    var likecount: Int = 0,
    val profileImage: String = "null",
    var postId: String = "null",
    val likedBy: MutableList<String>? = null,
    var isSaved: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList()?.toMutableList(),
        parcel.readByte() != 0.toByte()

    ) {

    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(heading)
        parcel.writeString(username)
        parcel.writeString(date)
        parcel.writeString(post)
        parcel.writeInt(likecount)
        parcel.writeString(profileImage)
        parcel.writeString(postId)
        parcel.writeStringList(likedBy)
        parcel.writeByte(if (isSaved) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BlogItemModel> {
        override fun createFromParcel(parcel: Parcel): BlogItemModel {
            return BlogItemModel(parcel)
        }

        override fun newArray(size: Int): Array<BlogItemModel?> {
            return arrayOfNulls(size)
        }
    }

}
