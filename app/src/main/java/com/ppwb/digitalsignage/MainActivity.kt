package com.ppwb.digitalsignage

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.hilt.navigation.compose.hiltViewModel
import com.ppwb.digitalsignage.presentation.SignageScreen
import com.ppwb.digitalsignage.presentation.SignageViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val viewModel: SignageViewModel = hiltViewModel()
            SignageScreen(viewModel)
        }
    }
}
