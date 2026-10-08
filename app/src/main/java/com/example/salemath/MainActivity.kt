package com.example.salemath

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.salemath.ui.SaleMathApp
import com.example.salemath.viewmodel.SaleMathViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel: SaleMathViewModel = viewModel()
            SaleMathApp(viewModel = viewModel)
        }
    }
}