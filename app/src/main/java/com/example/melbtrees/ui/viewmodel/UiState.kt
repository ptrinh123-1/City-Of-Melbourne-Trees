package com.example.melbtrees.ui.viewmodel

import com.example.melbtrees.data.local.Tree

sealed class UiState {
    object Loading : UiState()
    data class Success(val trees: List<Tree>) : UiState()
    data class Error(val message: String) : UiState()
}