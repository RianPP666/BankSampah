package com.kkn.banksampah.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

enum class ResultType {
    SUCCESS,
    ERROR,
    WARNING,
    INFO,
    CONFIRMATION
}

@Composable
fun ResultDialog(
    type: ResultType,
    title: String,
    message: String,
    onConfirm: () -> Unit,
    onDismiss: (() -> Unit)? = null,
    confirmText: String = "OK",
    dismissText: String = "Batal"
) {
    val (icon: ImageVector, iconColor: Color, containerBgColor: Color) = when (type) {
        ResultType.SUCCESS -> Triple(Icons.Default.CheckCircle, Color(0xFF16A34A), Color(0xFFDCFCE7))
        ResultType.ERROR -> Triple(Icons.Default.Error, Color(0xFFDC2626), Color(0xFFFEE2E2))
        ResultType.WARNING -> Triple(Icons.Default.Warning, Color(0xFFD97706), Color(0xFFFEF3C7))
        ResultType.INFO -> Triple(Icons.Default.Info, Color(0xFF2563EB), Color(0xFFDBEAFE))
        ResultType.CONFIRMATION -> Triple(Icons.AutoMirrored.Filled.HelpOutline, MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
    }

    AlertDialog(
        onDismissRequest = { onDismiss?.invoke() ?: onConfirm() },
        shape = RoundedCornerShape(20.dp),
        title = null,
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(containerBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = type.name,
                        tint = iconColor,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = iconColor),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text(
                    text = confirmText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
            }
        },
        dismissButton = if (onDismiss != null) {
            {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = dismissText,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        } else null
    )
}
