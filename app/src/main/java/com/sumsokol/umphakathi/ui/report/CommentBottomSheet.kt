package com.sumsokol.umphakathi.ui.report

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.sumsokol.umphakathi.domain.model.Comment
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommentBottomSheet(
    reportId: String,
    onDismiss: () -> Unit,
    comments: List<Comment>,
    onAddComment: (String, String?) -> Unit // body, parentCommentId
) {
    var newCommentText by remember { mutableStateOf("") }
    var replyingToComment by remember { mutableStateOf<Comment?>(null) }
    
    // Group comments into main and nested reply flows cleanly
    val mainComments = remember(comments) { comments.filter { it.parentCommentId == null } }
    val replyMap = remember(comments) { comments.filter { it.parentCommentId != null }.groupBy { it.parentCommentId } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.85f)
                .fillMaxWidth()
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Comments (${comments.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            HorizontalDivider()

            // List area
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (mainComments.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No comments yet. Start the conversation!",
                                color = MaterialTheme.colorScheme.outline,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else {
                    items(mainComments) { comment ->
                        Column(modifier = Modifier.fillMaxWidth()) {
                            MainCommentItem(
                                comment = comment,
                                onReplyClick = { replyingToComment = comment }
                            )
                            
                            // Render Threaded Replies
                            val replies = replyMap[comment.id] ?: emptyList()
                            replies.forEach { reply ->
                                ReplyCommentItem(comment = reply)
                            }
                        }
                    }
                }
            }

            // Replying Banner if active
            replyingToComment?.let { replyTarget ->
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Replying to ${replyTarget.authorName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        IconButton(
                            onClick = { replyingToComment = null },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(Icons.Default.Close, null, modifier = Modifier.size(12.dp))
                        }
                    }
                }
            }

            // Bottom Input Bar
            Surface(shadowElevation = 8.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newCommentText,
                        onValueChange = { newCommentText = it },
                        placeholder = { 
                            Text(if (replyingToComment != null) "Write a reply..." else "Add a comment...") 
                        },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        shape = MaterialTheme.shapes.large
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (newCommentText.isNotBlank()) {
                                onAddComment(newCommentText, replyingToComment?.id)
                                newCommentText = ""
                                replyingToComment = null
                            }
                        }
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send, 
                            "Send", 
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainCommentItem(comment: Comment, onReplyClick: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Icon(
            Icons.Default.AccountCircle, 
            null, 
            Modifier.size(28.dp), 
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.authorName, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(8.dp))
                Text(timeAgo(comment.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(2.dp))
            Text(comment.body, style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(4.dp))
            Row(
                modifier = Modifier.clickable { onReplyClick() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Reply, 
                    null, 
                    modifier = Modifier.size(14.dp), 
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Text("Reply", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun ReplyCommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 36.dp, top = 6.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .width(2.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
        )
        Spacer(Modifier.width(10.dp))
        Icon(
            Icons.Default.AccountCircle, 
            null, 
            Modifier.size(22.dp), 
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(Modifier.width(6.dp))
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(comment.authorName, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                Text(timeAgo(comment.createdAt), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
            }
            Spacer(Modifier.height(1.dp))
            Text(comment.body, style = MaterialTheme.typography.bodySmall)
        }
    }
}
