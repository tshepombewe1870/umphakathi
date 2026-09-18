package com.sumsokol.umphakathi.ui.community

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.sumsokol.umphakathi.data.firebase.FirebaseDataModule
import com.sumsokol.umphakathi.domain.model.Comment
import com.sumsokol.umphakathi.domain.model.CommunityPost
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.UUID

data class PostDetailUiState(
    val post: CommunityPost? = null,
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = true
)

class PostDetailViewModel(private val postId: String) : ViewModel() {
    private val communityRepository = FirebaseDataModule.communityRepository

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            communityRepository.getCommentsForPost(postId).collect { comments ->
                _uiState.value = _uiState.value.copy(comments = comments, isLoading = false)
            }
        }
    }

    fun addComment(body: String) {
        if (body.isBlank()) return
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            val comment = Comment(
                id = UUID.randomUUID().toString(),
                postId = postId,
                authorId = userId,
                body = body
            )
            communityRepository.addComment(comment)
        }
    }

    fun markResolved() {
        val userId = FirebaseDataModule.currentUserId ?: return
        viewModelScope.launch {
            communityRepository.markResolved(postId, userId)
        }
    }

    companion object {
        fun factory(postId: String) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                PostDetailViewModel(postId) as T
        }
    }
}
