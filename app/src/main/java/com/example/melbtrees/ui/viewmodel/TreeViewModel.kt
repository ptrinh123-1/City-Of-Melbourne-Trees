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
import kotlinx.coroutines.flow.update

class TreeViewModel(
    private val repository: TreeRepository,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val _favoritesState = MutableStateFlow<List<Tree>>(emptyList())
    val favoritesState: StateFlow<List<Tree>> = _favoritesState.asStateFlow()

    private val _selectedTree = MutableStateFlow<Tree?>(null)
    val selectedTree: StateFlow<Tree?> = _selectedTree.asStateFlow()

    private var allTrees: List<Tree> = emptyList()

    init {
        loadTrees()
    }

    private fun loadTrees() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                allTrees = repository.getTrees()
                Log.d("TreeViewModel", "Repository returned ${allTrees.size} trees.")
                _uiState.value = UiState.Success(allTrees)
                updateFavoritesList()
            } catch (e: Exception) {
                Log.e("TreeViewModel", "Error loading trees", e)
                _uiState.value = UiState.Error("Failed to load trees. Check network connection.")
            }
        }
    }

    fun searchByAddress(address: String) {
        if (address.isBlank()) {
            _uiState.value = UiState.Success(allTrees)
            return
        }

        _uiState.value = UiState.Loading
        viewModelScope.launch {
            try {
                val geocoder = Geocoder(context)
                val addresses = geocoder.getFromLocationName(address, 1)

                if (addresses?.isNotEmpty() == true) {
                    val location = addresses[0]
                    val sortedTrees = allTrees.sortedBy { tree ->
                        calculateDistance(
                            location.latitude,
                            location.longitude,
                            tree.latitude ?: 0.0,
                            tree.longitude ?: 0.0
                        )
                    }
                    _uiState.value = UiState.Success(sortedTrees)
                } else {
                    _uiState.value = UiState.Error("Address not found.")
                }
            } catch (e: IOException) {
                _uiState.value = UiState.Error("Network error or invalid address.")
            }
        }
    }

    fun findTreeById(id: String?) {
        _selectedTree.value = allTrees.find { it.id == id }
    }

    fun onFavoriteClicked(tree: Tree) {
        // Get the new, updated list from the repository
        val updatedTrees = repository.toggleFavoriteStatus(tree.id)
        allTrees = updatedTrees

        // Find the newly updated tree from that list
        val updatedSelectedTree = updatedTrees.find { it.id == tree.id }

        // Update all the states with the new information
        _uiState.value = UiState.Success(updatedTrees)
        _selectedTree.value = updatedSelectedTree
        updateFavoritesList()
    }

    private fun updateFavoritesList() {
        _favoritesState.value = allTrees.filter { it.isFavourite }
    }

}

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