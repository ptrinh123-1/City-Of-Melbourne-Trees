package com.example.melbtrees.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.melbtrees.ui.viewmodel.TreeViewModel
import com.example.melbtrees.ui.viewmodel.UiState
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.fillMaxSize


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: TreeViewModel,
    onTreeClick: (String) -> Unit
) {
    var selectedTabIndex by remember { mutableStateOf(0) }
    val tabs = listOf("All Trees", "Favorites")

    val allTreesState by viewModel.uiState.collectAsState()
    val favoriteTrees by viewModel.favoritesState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Melbourne Urban Forest") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            // Address Search Bar
            (allTreesState as? UiState.Success)?.let {
                AddressSearchBar(onSearch = viewModel::searchByAddress)
            }

            // Tabs for switching views
            TabRow(selectedTabIndex = selectedTabIndex) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = { Text(title) }
                    )
                }
            }

            // Display content based on the selected tab
            when (selectedTabIndex) {
                0 -> { // All Trees Tab
                    when (val state = allTreesState) {
                        is UiState.Loading -> LoadingState()
                        is UiState.Success -> TreeList(trees = state.trees, onTreeClick = onTreeClick)
                        is UiState.Error -> ErrorState(message = state.message)
                    }
                }
                1 -> { // Favorites Tab
                    TreeList(trees = favoriteTrees, onTreeClick = onTreeClick)
                }
            }
        }
    }
}

@Composable
fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun ErrorState(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = message, color = MaterialTheme.colorScheme.error)
    }
}