package com.example.cloudalbum.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import com.example.cloudalbum.data.remote.Photo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(viewModel: AlbumViewModel = viewModel(factory = AlbumViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbar = remember { SnackbarHostState() }
    var openedId by rememberSaveable { mutableStateOf<String?>(null) }

    // Photo Picker: không cần xin quyền đọc ảnh, người dùng chọn tối đa 5 ảnh
    val pickPhotos = rememberLauncherForActivityResult(
        ActivityResultContracts.PickMultipleVisualMedia(maxItems = 5),
    ) { uris -> viewModel.upload(uris) }

    // Android 17+ chặn app gọi tới máy trong mạng cục bộ (server Node.js chạy trên laptop)
    // cho tới khi người dùng cấp quyền ACCESS_LOCAL_NETWORK.
    val context = LocalContext.current
    var askedLocalNetwork by rememberSaveable { mutableStateOf(false) }
    val localNetworkPermission = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted -> if (granted) viewModel.refresh() }
    LaunchedEffect(Unit) {
        val needsPermission = Build.VERSION.SDK_INT >= 37 &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_LOCAL_NETWORK) !=
                PackageManager.PERMISSION_GRANTED
        if (needsPermission && !askedLocalNetwork) {
            askedLocalNetwork = true
            localNetworkPermission.launch(Manifest.permission.ACCESS_LOCAL_NETWORK)
        }
    }

    LaunchedEffect(state.message) {
        state.message?.let {
            snackbar.showSnackbar(it)
            viewModel.onMessageShown()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Album Cloudinary") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Tải lại")
                    }
                },
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = {
                    pickPhotos.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                text = { Text("Thêm ảnh") },
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            if (state.uploadingCount > 0) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
                Text(
                    "Đang tải lên ${state.uploadingCount} ảnh…",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                )
            }
            when {
                state.isLoading && state.photos.isEmpty() -> Centered { CircularProgressIndicator() }
                state.error != null && state.photos.isEmpty() -> Centered {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        TextButton(onClick = viewModel::refresh) { Text("Thử lại") }
                    }
                }
                state.photos.isEmpty() -> Centered { Text("Album trống. Bấm \"Thêm ảnh\" để tải lên.") }
                else -> LazyVerticalGrid(
                    columns = GridCells.Adaptive(minSize = 110.dp),
                    contentPadding = PaddingValues(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(state.photos, key = { it.id }) { photo ->
                        AsyncImage(
                            model = photo.thumbUrl,    // ảnh 400x400 nhẹ, không tải ảnh gốc cho lưới
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable { openedId = photo.id },
                        )
                    }
                }
            }
        }
    }

    state.photos.find { it.id == openedId }?.let { photo ->
        PhotoDialog(
            photo = photo,
            onDismiss = { openedId = null },
            onDelete = {
                viewModel.delete(photo)
                openedId = null
            },
        )
    }
}

@Composable
private fun PhotoDialog(photo: Photo, onDismiss: () -> Unit, onDelete: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                AsyncImage(
                    model = photo.url,
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxWidth(),
                )
                Text("${photo.width} × ${photo.height} • ${photo.bytes / 1024} KB")
            }
        },
        confirmButton = { Button(onClick = onDelete) { Text("Xoá ảnh") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Đóng") } },
    )
}

@Composable
private fun Centered(content: @Composable () -> Unit) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) { content() }
}