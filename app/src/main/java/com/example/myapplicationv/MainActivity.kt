package com.example.myapplicationv

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplicationv.navigation.AppNavGraph
import com.example.myapplicationv.ui.theme.VetHomeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VetHomeApp()
        }
    }
}

@Composable
fun VetHomeApp() {
    val navController = rememberNavController()

    VetHomeTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            AppNavGraph(navController = navController)
        }
    }
}