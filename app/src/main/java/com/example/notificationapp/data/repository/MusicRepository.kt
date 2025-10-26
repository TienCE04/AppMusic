package com.example.notificationapp.data.repository

import android.content.Context
import com.example.notificationapp.data.model.Song
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken


class MusicRepository() {
    fun getSongs(context: Context): List<Song> {
        val json = readJsonFromAssets(context, "songs.json")
        val type = object : TypeToken<List<Song>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun readJsonFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }
}
