package com.example.melbtrees.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.melbtrees.ui.viewmodel.TreeViewModel
import android.util.Log
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TreeDetailScreen(
    viewModel: TreeViewModel,
    treeId: String?,
    onNavigateUp: () -> Unit
) {
    // Tell the ViewModel to find the tree when this screen appears
    LaunchedEffect(key1 = treeId) {
        viewModel.findTreeById(treeId)
    }

    val tree by viewModel.selectedTree.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tree?.commonName ?: "Tree Details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    tree?.let {
                        IconButton(onClick = { viewModel.onFavoriteClicked(it) }) {
                            Icon(
                                imageVector = if (it.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (it.isFavourite) MaterialTheme.colorScheme.primary else LocalContentColor.current
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        val currentTree = tree
        if (currentTree == null) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                Text("Tree not found.")
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()) // Make the column scrollable
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // --- Static Map View ---
                if (currentTree.latitude != null && currentTree.longitude != null) {
                    StaticMapView(
                        latitude = currentTree.latitude,
                        longitude = currentTree.longitude
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // --- Tree Details ---
                Text(currentTree.commonName ?: "N/A", style = MaterialTheme.typography.headlineMedium)
                Text(currentTree.scientificName ?: "N/A", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(16.dp))

                DetailRow(label = "Family", value = currentTree.family ?: "N/A")
                DetailRow(label = "Genus", value = currentTree.genus ?: "N/A")
                DetailRow(label = "Year Planted", value = currentTree.yearPlanted ?: "N/A")
                DetailRow(label = "Date Planted", value = currentTree.datePlanted ?: "N/A")
                DetailRow(label = "Age Description", value = currentTree.ageDescription ?: "N/A")
                DetailRow(label = "Trunk Diameter (cm)", value = currentTree.diameterAtBreastHeightCm?.toString() ?: "N/A")
                DetailRow(label = "Precinct", value = currentTree.precinct ?: "N/A")
                DetailRow(label = "Location Type", value = currentTree.locatedIn ?: "N/A")
                DetailRow(label = "Latitude", value = currentTree.latitude?.toString() ?: "N/A")
                DetailRow(label = "Longitude", value = currentTree.longitude?.toString() ?: "N/A")
            }
        }
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row {
        Text("$label: ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Text(value, fontSize = 16.sp)
    }
}

@Composable
fun StaticMapView(
    latitude: Double,
    longitude: Double,
    modifier: Modifier = Modifier
) {
    val offset = 0.002
    val minLon = longitude - offset
    val minLat = latitude - offset
    val maxLon = longitude + offset
    val maxLat = latitude + offset

    // --- THIS IS THE FIX ---
    // Format the numbers to 6 decimal places to create a clean URL
    val bbox = String.format(
        Locale.US,
        "%.3f,%.3f,%.3f,%.3f",
        minLon, minLat, maxLon, maxLat
    )

    val mapUrl = "https://static-maps.yandex.ru/1.x/?lang=en-US&ll=$longitude,$latitude&z=17&l=map&size=600,300&pt=$longitude,$latitude,pm2rdl"

    Log.d("StaticMapView", "Loading map from URL: $mapUrl")

    val imageRequest = ImageRequest.Builder(LocalContext.current)
        .data(mapUrl)
        .crossfade(true)
        .memoryCacheKey(mapUrl)
        .diskCacheKey(mapUrl)
        .listener(onError = { _, result ->
            Log.e("StaticMapView", "Coil failed to load image", result.throwable)
        })
        .build()

    AsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(mapUrl)
            .crossfade(true)
            .build(),
        contentDescription = "Map view of the tree's location",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
    )
}