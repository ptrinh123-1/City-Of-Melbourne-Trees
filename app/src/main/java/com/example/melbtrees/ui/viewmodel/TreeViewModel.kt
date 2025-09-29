package com.example.melbtrees.ui.viewmodel

import android.content.Context
import android.location.Geocoder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.melbtrees.data.local.Tree
import com.example.melbtrees.repository.TreeRepository
import com.example.melbtrees.util.calculateDistance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import android.util.Log
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Locale

private val melbourneLocations = listOf(
    // Fitzroy Gardens
    -37.8136 to 144.9799,
    // Royal Botanic Gardens
    -37.8304 to 144.9796,
    // Flinders Street Station
    -37.8183 to 144.9671,
    // Queen Victoria Market
    -37.8077 to 144.9568
)

private const val MELB_MIN_LAT = -37.89
private const val MELB_MAX_LAT = -37.77
private const val MELB_MIN_LON = 144.86
private const val MELB_MAX_LON = 145.03

class TreeViewModel(
    private val repository: TreeRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Success(emptyList()))
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _favoritesState = MutableStateFlow<List<Tree>>(emptyList())
    val favoritesState: StateFlow<List<Tree>> = _favoritesState.asStateFlow()

    private val _selectedTree = MutableStateFlow<Tree?>(null)
    val selectedTree: StateFlow<Tree?> = _selectedTree.asStateFlow()

    init {
        // Load an initial random set of trees when the app starts
        findRandomTrees()
        loadFavorites()
    }

    fun findRandomTrees() {
        val randomLat = (MELB_MIN_LAT..MELB_MAX_LAT).random()
        val randomLon = (MELB_MIN_LON..MELB_MAX_LON).random()

        searchByCoordinates(randomLat, randomLon)
    }

    private fun searchByCoordinates(latitude: Double, longitude: Double) {
        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val nearbyTrees = repository.getTreesNearLocation(latitude, longitude)
                _uiState.value = UiState.Success(nearbyTrees)
                if (nearbyTrees.isEmpty()) {
                    // If we landed in an empty spot, just try again with a new random location.
                    findRandomTrees()
                } else {
                    // Otherwise, show the results.
                    _uiState.value = UiState.Success(nearbyTrees)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error("Failed to load trees.")
            }
        }
    }

    fun loadFavorites() {
        viewModelScope.launch { // <-- Run this in a coroutine
            Log.d("ViewModel", "Loading favorite trees...")
            _favoritesState.value = repository.loadFavoriteTrees()
            Log.d("ViewModel", "Found ${_favoritesState.value.size} favorite trees.")
        }
    }

    fun findTreeById(id: String?) {
        // Check both lists to find the selected tree
        val allTrees = (_uiState.value as? UiState.Success)?.trees ?: emptyList()
        val favoriteTrees = _favoritesState.value
        _selectedTree.value = (allTrees + favoriteTrees).find { it.id == id }
    }

    fun onFavoriteClicked(tree: Tree) {
        (_uiState.value as? UiState.Success)?.let { currentState ->
            val updatedList = repository.toggleFavoriteStatus(tree, currentState.trees)
            _uiState.value = UiState.Success(updatedList)
            _selectedTree.update { it?.copy(isFavourite = !it.isFavourite) }
            loadFavorites() // <-- Refresh the favorites list after a change
        }
    }
}

private fun ClosedRange<Double>.random() =
    start + java.util.Random().nextDouble() * (endInclusive - start)

class TreeViewModelFactory(
    private val repository: TreeRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TreeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TreeViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}