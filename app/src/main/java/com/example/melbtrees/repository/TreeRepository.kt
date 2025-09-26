package com.example.melbtrees.repository

import com.example.melbtrees.data.local.Tree
import com.example.melbtrees.data.local.TreeDataSource
import com.example.melbtrees.data.remote.MelbourneTreesApi
import android.util.Log

class TreeRepository(
    private val api: MelbourneTreesApi,
    private val dataSource: TreeDataSource
) {
    // Keep a local copy of trees in memory
    private var inMemoryTrees: List<Tree> = emptyList()

    suspend fun getTrees(): List<Tree> {
        if (inMemoryTrees.isNotEmpty()) {
            return inMemoryTrees
        }

        val cachedTrees = dataSource.loadTrees()
        if (cachedTrees.isNotEmpty()) {
            inMemoryTrees = mergeWithFavorites(cachedTrees)
            return inMemoryTrees
        }

        val response = api.getTrees()
        val networkTrees = response.results.mapNotNull { dto ->
            if (dto.id == null) null else Tree(
                id = dto.id,
                commonName = dto.commonName,
                scientificName = dto.scientificName,
                family = dto.family,
                genus = dto.genus,
                ageDescription = dto.ageDescription,
                yearPlanted = dto.yearPlanted,
                datePlanted = dto.datePlanted,
                diameterAtBreastHeightCm = dto.diameterAtBreastHeightCm,
                precinct = dto.precinct,
                locatedIn = dto.locatedIn,
                latitude = dto.location?.latitude,
                longitude = dto.location?.longitude
            )
        }

        dataSource.saveTrees(networkTrees)
        inMemoryTrees = mergeWithFavorites(networkTrees)
        return inMemoryTrees
    }

    fun toggleFavoriteStatus(treeId: String): List<Tree> {
        val favoriteIds = dataSource.loadFavoriteIds().toMutableSet()
        val isCurrentlyFavorite = treeId in favoriteIds

        if (isCurrentlyFavorite) {
            favoriteIds.remove(treeId)
        } else {
            favoriteIds.add(treeId)
        }
        dataSource.saveFavoriteIds(favoriteIds)

        // Create a new list with the updated tree
        inMemoryTrees = inMemoryTrees.map { tree ->
            if (tree.id == treeId) {
                tree.copy(isFavourite = !isCurrentlyFavorite)
            } else {
                tree
            }
        }
        return inMemoryTrees
    }

    private fun mergeWithFavorites(trees: List<Tree>): List<Tree> {
        val favoriteIds = dataSource.loadFavoriteIds()
        trees.forEach { tree ->
            tree.isFavourite = tree.id in favoriteIds
        }
        return trees
    }
}