package com.example.melbtrees

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.melbtrees.data.local.TreeDataSource
import com.example.melbtrees.data.remote.MelbourneTreesApi
import com.example.melbtrees.repository.TreeRepository
import com.example.melbtrees.ui.screens.TreeListScreen
import com.example.melbtrees.ui.theme.MelbtreesTheme // Update this if needed
import com.example.melbtrees.ui.viewmodel.TreeViewModel
import com.example.melbtrees.ui.viewmodel.TreeViewModelFactory
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class MainActivity : ComponentActivity() {

    // Manually create all dependencies
    private val api: MelbourneTreesApi by lazy {

        // Create a logger
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        // Create a custom OkHttpClient
        val client = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()

        Retrofit.Builder()
            .baseUrl("https://data.melbourne.vic.gov.au/api/explore/v2.1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MelbourneTreesApi::class.java)
    }

    private val treeDataSource: TreeDataSource by lazy {
        val sharedPrefs = getSharedPreferences("tree_prefs", Context.MODE_PRIVATE)
        TreeDataSource(sharedPrefs)
    }

    private val treeRepository: TreeRepository by lazy {
        TreeRepository(api, treeDataSource) // <-- Pass both dependencies
    }

    private val treeViewModel: TreeViewModel by viewModels {
        TreeViewModelFactory(treeRepository, applicationContext)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MelbtreesTheme {
                AppNavHost(viewModel = treeViewModel)
            }
        }
    }
}