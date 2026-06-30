package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.DashboardScreen
import com.example.ui.ReleaseViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  private val viewModel: ReleaseViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme(darkTheme = true) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          DashboardScreen(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
