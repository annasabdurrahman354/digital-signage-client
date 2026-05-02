package com.ppwb.digitalsignage.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import coil3.compose.AsyncImage
import com.ppwb.digitalsignage.domain.model.Asset
import java.io.File

@Composable
fun SignageScreen(viewModel: SignageViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val currentAsset by viewModel.currentAsset.collectAsState()

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        when (val state = uiState) {
            is SignageUiState.Loading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading...", color = Color.White)
                    Text("Serial: ${viewModel.androidId}", color = Color.Gray)
                }
            }
            is SignageUiState.Error -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Error: ${state.message}", color = Color.Red)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Serial: ${viewModel.androidId}", color = Color.White)
                }
            }
            is SignageUiState.Success -> {
                if (currentAsset != null) {
                    MediaDisplay(asset = currentAsset!!)
                } else {
                    Text("Waiting for assets...", color = Color.White, modifier = Modifier.align(Alignment.Center))
                }
            }
        }
    }
}

@Composable
fun MediaDisplay(asset: Asset) {
    if (asset.type == "video") {
        VideoPlayer(path = asset.localPath!!)
    } else {
        AsyncImage(
            model = File(asset.localPath!!),
            contentDescription = asset.name,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun VideoPlayer(path: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(File(path).toString()))
            prepare()
            playWhenReady = true
            repeatMode = ExoPlayer.REPEAT_MODE_ONE
        }
    }

    DisposableEffect(path) {
        exoPlayer.setMediaItem(MediaItem.fromUri(File(path).toString()))
        exoPlayer.prepare()
        onDispose {
            exoPlayer.release()
        }
    }

    AndroidView(
        factory = {
            PlayerView(context).apply {
                player = exoPlayer
                useController = false
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}
