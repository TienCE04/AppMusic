package com.example.notificationapp.data.model

import kotlinx.parcelize.Parcelize
import android.os.Parcelable

@Parcelize
data class Song(
    val title: String,
    val artist: String,
    val url: String
) : Parcelable