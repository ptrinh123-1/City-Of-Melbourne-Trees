package com.example.melbtrees.data.local

import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class TreeDataSource(private val sharedPreferences: SharedPreferences) {

    private val gson = Gson()
    private val CACHE_KEY = "tree_cache"
    private val FAVORITES_KEY = "favorite_tree_ids"

    fun saveTrees(trees: List<Tree>) {
        val jsonString = gson.toJson(trees)
        sharedPreferences.edit().putString(CACHE_KEY, jsonString).apply()
    }

    fun loadTrees(): List<Tree> {
        val jsonString = sharedPreferences.getString(CACHE_KEY, null)
        return if (jsonString != null) {
            val type = object : TypeToken<List<Tree>>() {}.type
            gson.fromJson(jsonString, type)
        } else {
            emptyList()
        }
    }

    fun loadFavoriteIds(): Set<String> {
        return sharedPreferences.getStringSet(FAVORITES_KEY, emptySet()) ?: emptySet()
    }

    fun saveFavoriteIds(ids: Set<String>) {
        sharedPreferences.edit().putStringSet(FAVORITES_KEY, ids).apply()
    }

}