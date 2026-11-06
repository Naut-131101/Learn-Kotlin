package com.example.cloudalbum.ui

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.cloudalbum.CloudAlbumApp
import com.example.cloudalbum.data.PhotoRepository
import com.example.cloudalbum.data.remote.Photo
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class AlbumUiState(
    val photos: List<Photo> = emptyList(),
    val isLoading: Boolean = true,
    val uploadingCount: Int = 0,        // > 0: đang có ảnh tải lên
    val error: String? = null,          // lỗi tải danh sách
    val message: String? = null,        // thông báo một lần cho Snackbar
)

class AlbumViewModel(private val repository: PhotoRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(AlbumUiState())
    val uiState: StateFlow<AlbumUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val photos = repository.getPhotos()
                _uiState.update { it.copy(photos = photos, isLoading = false, error = null) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.toUserMessage()) }
            }
        }
    }

    /** Tải lần lượt từng ảnh; một ảnh lỗi không làm hỏng các ảnh còn lại. */
    fun upload(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _uiState.update { it.copy(uploadingCount = it.uploadingCount + uris.size) }
            var failed = 0
            for (uri in uris) {
                try {
                    repository.upload(uri)
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    failed++
                    Log.e("CloudAlbum", "Upload thất bại: $uri", e)
                } finally {
                    _uiState.update { it.copy(uploadingCount = it.uploadingCount - 1) }
                }
            }
            val done = uris.size - failed
            _uiState.update {
                it.copy(message = if (failed == 0) "Đã tải lên $done ảnh" else "Tải lên $done ảnh, lỗi $failed ảnh")
            }
            refresh()
        }
    }

    fun delete(photo: Photo) {
        viewModelScope.launch {
            try {
                repository.delete(photo)
                _uiState.update { it.copy(photos = it.photos - photo, message = "Đã xoá ảnh") }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(message = "Không xoá được: ${e.toUserMessage()}") }
            }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as CloudAlbumApp
                AlbumViewModel(app.container.photoRepository)
            }
        }
    }
}

private fun Throwable.toUserMessage(): String = when (this) {
    is IOException -> "Không kết nối được server. Server Node.js đã chạy chưa?"
    is HttpException -> "Server trả lỗi ${code()}"
    else -> message ?: javaClass.simpleName
}