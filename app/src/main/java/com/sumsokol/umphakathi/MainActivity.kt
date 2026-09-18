package com.sumsokol.umphakathi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.sumsokol.umphakathi.theme.CrisisReportingTheme
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.data.firebase.FirebaseSeeder
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    FirebaseDataModule.init(this)

    // Seed data for development
    lifecycleScope.launch {
        try {
            FirebaseSeeder.seedIfEmpty(Firebase.firestore)
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Failed to seed data: ${e.message}")
        }
    }

    enableEdgeToEdge()
    setContent {
      CrisisReportingTheme { Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) { MainNavigation() } }
    }
  }
}
