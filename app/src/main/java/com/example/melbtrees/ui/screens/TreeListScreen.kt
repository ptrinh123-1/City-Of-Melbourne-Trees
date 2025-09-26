package com.example.melbtrees.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.melbtrees.data.local.Tree
import com.example.melbtrees.ui.viewmodel.TreeViewModel
import com.example.melbtrees.ui.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeListScreen(
    viewModel: TreeViewModel,
    onTreeClick: (String) -> Unit // <-- CRITICAL PART 1: The screen receives the click event
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Melbourne Urban Forest") })
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AddressSearchBar(
                onSearch = { address ->
                    viewModel.searchByAddress(address)
                }
            )

            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> {
                    if (state.trees.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No trees found.")
                        }
                    } else {
                        TreeList(trees = state.trees, onTreeClick = onTreeClick) // <-- CRITICAL PART 2: The event is passed to the list
                    }
                }
                is UiState.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}

@Composable
fun AddressSearchBar(onSearch: (String) -> Unit) {
    // 1. Ensure mutableStateOf is spelled correctly
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            // 2. Explicitly name the new value "newValue" or anything you like
            onValueChange = { newValue -> text = newValue },
            label = { Text("Search by address or postcode") },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = { onSearch(text) }) {
            Text("Go")
        }
    }
}


@Composable
fun TreeList(
    trees: List<Tree>,
    onTreeClick: (String) -> Unit // <-- The list receives the event
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        items(trees, key = { it.id }) { tree ->
            // The event is passed to the item, calling it with the specific tree's ID
            TreeItem(tree = tree, onTreeClick = { onTreeClick(tree.id) })
            HorizontalDivider()
        }
    }
}

@Composable
fun TreeItem(
    tree: Tree,
    onTreeClick: () -> Unit // <-- The item receives the event
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onTreeClick) // <-- CRITICAL PART 3: The clickable modifier is applied here
            .padding(vertical = 12.dp)
            .padding(horizontal = 16.dp) // Added horizontal padding for a better click area
    ) {
        Text(
            text = tree.commonName ?: "Unknown Tree",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = tree.scientificName ?: "No scientific name",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        tree.yearPlanted?.let {
            Text(
                text = "Planted: $it",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}