package com.example.blogapp.Model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.Exclude

data class BlogItemModel(
    var heading: String = "null",
    val username: String = "null",
    val date: String = "null",
    var post: String = "null",
    val userId: String? = "null",
    var likecount: Int = 0,
    val profileImage: String = "null",
    var postId: String = "null",
    val likedBy: MutableList<String>? = null,
    // @get:Exclude tells Firebase's reflection-based serializer to skip these when
    // writing this object with setValue(). They're derived, UI-only state computed by
    // the ViewModel (see FeedViewModel) and were never meant to be persisted — without
    // Exclude they'd silently get written into every blog node in the DB.
    @get:Exclude var isSaved: Boolean = false,
    @get:Exclude val isLikedByCurrentUser: Boolean = false
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.readInt(),
        parcel.readString() ?: "",
        parcel.readString() ?: "",
        parcel.createStringArrayList()?.toMutableList(),
        parcel.readByte() != 0.toByte(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(heading)
        parcel.writeString(username)
        parcel.writeString(date)
        parcel.writeString(post)
        parcel.writeString(userId)
        parcel.writeInt(likecount)
        parcel.writeString(profileImage)
        parcel.writeString(postId)
        parcel.writeStringList(likedBy)
        parcel.writeByte(if (isSaved) 1 else 0)
        parcel.writeByte(if (isLikedByCurrentUser) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<BlogItemModel> {
        override fun createFromParcel(parcel: Parcel): BlogItemModel = BlogItemModel(parcel)
        override fun newArray(size: Int): Array<BlogItemModel?> = arrayOfNulls(size)
    }
}