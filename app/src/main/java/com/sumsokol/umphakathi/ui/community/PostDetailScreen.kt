package com.sumsokol.umphakathi.ui.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.sumsokol.umphakathi.domain.model.Comment
import com.sumsokol.umphakathi.ui.report.ActionStatChip
import com.sumsokol.umphakathi.ui.report.ImageSlideshowScreen
import com.sumsokol.umphakathi.ui.report.VolunteerOfferDialog
import com.sumsokol.umphakathi.ui.report.timeAgo
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    viewModel: PostDetailViewModel = viewModel(factory = PostDetailViewModel.factory(postId))
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var commentText by remember { mutableStateOf("") }
    var showVolunteerDialog by remember { mutableStateOf(false) }
    var selectedImageIndex by remember { mutableStateOf<Int?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    if (showVolunteerDialog) {
        VolunteerOfferDialog(
            onDismiss = { showVolunteerDialog = false },
            onSubmit = { _, _ -> showVolunteerDialog = false }
        )
    }

    val post = uiState.post
    if (post != null && selectedImageIndex != null) {
        ImageSlideshowScreen(
            imageUrls = post.imageUrls,
            initialIndex = selectedImageIndex ?: 0,
            onBack = { selectedImageIndex = null }
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .navigationBarsPadding(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        placeholder = { Text("Add a comment...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = MaterialTheme.shapes.large
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                viewModel.addComment(commentText)
                                commentText = ""
                            }
                        }
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, "Send", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(bottom = 8.dp)
        ) {
            if (post != null) {
                item {
                    // Post body
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // User Info and actions header above everything else
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Avatar",
                                    modifier = Modifier.size(36.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(8.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = post.authorName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (!post.communityName.isNullOrBlank()) {
                                        Text(
                                            text = post.communityName,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.outline
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("More actions selected")
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.MoreVert, "More actions")
                                }
                            }

                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                            // Heading
                            Text(post.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            
                            // Images in-between heading and description if included
                            if (post.imageUrls.isNotEmpty()) {
                                Spacer(Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    post.imageUrls.forEachIndexed { index, url ->
                                        AsyncImage(
                                            model = url,
                                            contentDescription = "Report Image $index",
                                            modifier = Modifier
                                                .size(120.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { selectedImageIndex = index },
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                            }

                            Spacer(Modifier.height(8.dp))
                            // Description (body)
                            Text(post.body, style = MaterialTheme.typography.bodyMedium)
                            Spacer(Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                ActionStatChip(
                                    icon = Icons.Default.BackHand,
                                    value = post.meTooCount,
                                    onClick = { /* Like */ }
                                )
                                ActionStatChip(
                                    icon = Icons.AutoMirrored.Filled.Comment,
                                    value = post.commentCount,
                                    onClick = { /* Focus comment field? */ }
                                )
                                ActionStatChip(
                                    icon = Icons.Default.VolunteerActivism,
                                    value = post.volunteerCount,
                                    onClick = { showVolunteerDialog = true }
                                )
                            }
                        }
                    }
                }
                item {
                    // Action buttons with added Tag and Share actions
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showVolunteerDialog = true },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.VolunteerActivism, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Help", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = { viewModel.markResolved() },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Resolved", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Tag feature coming soon!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.AlternateEmail, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Tag", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                        OutlinedButton(
                            onClick = {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Post shared successfully!")
                                }
                            },
                            modifier = Modifier.weight(1f),
                            contentPadding = PaddingValues(horizontal = 4.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Share, null, Modifier.size(14.dp))
                            Spacer(Modifier.width(2.dp))
                            Text("Share", style = MaterialTheme.typography.labelSmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
                item {
                    Text(
                        "Comments (${uiState.comments.size})",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                }
                items(uiState.comments) { comment ->
                    CommentItem(comment = comment)
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    val isReply = comment.parentCommentId != null
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = if (isReply) 40.dp else 16.dp,
                end = 16.dp,
                top = 8.dp,
                bottom = 4.dp
            )
    ) {
        if (isReply) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
                    .padding(end = 8.dp)
            )
            Spacer(Modifier.width(8.dp))
        }
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AccountCircle, null, Modifier.size(20.dp), tint = MaterialTheme.colorScheme.outline)
                Spacer(Modifier.width(4.dp))
                Text("@anonymous", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(timeAgo(comment.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(3.dp))
            Text(comment.body, style = MaterialTheme.typography.bodySmall)
        }
    }
}
