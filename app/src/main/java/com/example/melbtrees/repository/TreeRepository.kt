package com.example.melbtrees.repository

import com.example.melbtrees.data.local.Tree
import com.example.melbtrees.data.local.TreeDataSource
import com.example.melbtrees.data.remote.MelbourneTreesApi
import android.util.Log
import java.util.Locale


class TreeRepository(
    private val api: MelbourneTreesApi,
    private val dataSource: TreeDataSource,

) {
    suspend fun getTreesNearLocation(latitude: Double, longitude: Double): List<Tree> {
        // --- THIS IS THE FIX ---
        // Build the "where" query string for the v2.1 API
        // Note the order: longitude then latitude
        val locationQuery = String.format(
            Locale.US,
            "distance(geolocation, geom'POINT(%.6f %.6f)', 1km)",
            longitude,
            latitude
        )

        // Call the API with the new query
        val response = api.getTrees(locationQuery = locationQuery)

        val networkTrees = response.results.mapNotNull { dto  ->
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
                latitude = dto.geolocation?.latitude,
                longitude = dto.geolocation?.longitude,
                isFavourite = false
            )
        }
        return mergeWithFavorites(networkTrees)
    }

    fun toggleFavoriteStatus(tree: Tree, currentList: List<Tree>): List<Tree> {
        val favoriteIds = dataSource.loadFavoriteIds().toMutableSet()

        if (tree.isFavourite) {
            favoriteIds.remove(tree.id)
        } else {
            favoriteIds.add(tree.id)
        }
        dataSource.saveFavoriteIds(favoriteIds)

        return currentList.map {
            if (it.id == tree.id) {
                it.copy(isFavourite = !it.isFavourite)
            } else {
                it
            }
        }
    }

    suspend fun loadFavoriteTrees(): List<Tree> {
        val favoriteIds = dataSource.loadFavoriteIds()
        Log.d("Repository", "Loaded favorite IDs: $favoriteIds")

        if (favoriteIds.isEmpty()) {
            return emptyList()
        }

        val idQuery = "com_id IN (" + favoriteIds.joinToString(",") { "\"$it\"" } + ")"

        // Call the API with the ID query
        val response = api.getTrees(locationQuery = idQuery, limit = favoriteIds.size)

        // --- THIS IS THE FIX ---
        // The mapping logic now needs to go one level deeper into "record.fields"
        val favoriteTrees = response.results.mapNotNull { dto  ->
            if (dto.id == null) {
                null
            } else {
                Tree(
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
                    latitude = dto.geolocation?.latitude,
                    longitude = dto.geolocation?.longitude,
                    //isFavourite = false // Default to false, will be set to true below
                )
            }
        }

        // Return the fetched favorites, ensuring their isFavourite flag is set to true
        return favoriteTrees.map { it.copy(isFavourite = true) }
    }

    private fun mergeWithFavorites(trees: List<Tree>): List<Tree> {
        val favoriteIds = dataSource.loadFavoriteIds()
        trees.forEach { tree ->
            tree.isFavourite = tree.id in favoriteIds
        }
        return trees
    }
}