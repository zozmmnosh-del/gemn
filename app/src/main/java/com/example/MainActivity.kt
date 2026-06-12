package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.data.AppDatabase
import com.example.data.ProcessedImageRepository
import com.example.ui.screens.MainScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.WatermarkViewModel
import com.example.ui.viewmodel.WatermarkViewModelFactory

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = ProcessedImageRepository(database.processedImageDao())
    val factory = WatermarkViewModelFactory(repository)

    setContent {
      MyApplicationTheme {
        val viewModel: WatermarkViewModel = androidx.lifecycle.viewmodel.compose.viewModel(factory = factory)
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          Box(modifier = Modifier.padding(innerPadding)) {
            MainScreen(viewModel = viewModel)
          }
        }
      }
    }
  }
}
