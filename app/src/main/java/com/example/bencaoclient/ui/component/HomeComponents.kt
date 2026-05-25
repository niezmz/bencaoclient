package com.example.bencaoclient.ui.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.bencaoclient.Bencao
import com.example.bencaoclient.ui.theme.Dimens
import com.example.bencaoclient.util.rememberHomeDiscoveryPainter
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeDiscoveryCard(
    bencao: Bencao,
    onClick: () -> Unit
) {
    val barGreen = MaterialTheme.colorScheme.primary
    val onBarGreen = MaterialTheme.colorScheme.onPrimary
    val timeText = remember(bencao.createdAt.time) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(bencao.createdAt)
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .clip(RoundedCornerShape(Dimens.radiusSm))
            .clickable(onClick = onClick)
    ) {
        if (bencao.images.isNotEmpty()) {
            Image(
                painter = rememberHomeDiscoveryPainter(bencao.images[0]),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFF2C2C2C)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "无图片",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(barGreen.copy(alpha = 0.88f))
                .padding(horizontal = Dimens.md, vertical = Dimens.sm)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bencao.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = onBarGreen,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f).padding(end = Dimens.sm)
                )
                Text(
                    text = timeText,
                    style = MaterialTheme.typography.bodySmall,
                    color = onBarGreen.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
fun HomePrimaryTile(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    tileGreen: Color,
    icon: ImageVector,
    label: String,
    showBadge: Boolean = false,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.45f
    val onTile = MaterialTheme.colorScheme.onPrimary
    Box(modifier = modifier.height(88.dp)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(Dimens.radiusSm))
                .background(tileGreen.copy(alpha = alpha))
                .clickable(enabled = enabled, onClick = onClick),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = onTile,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(Dimens.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
            color = onTile
        )
        }
        if (showBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 6.dp, end = 10.dp)
                    .size(9.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE53935))
            )
        }
    }
}

@Composable
fun HomeSecondaryTile(
    modifier: Modifier = Modifier,
    enabled: Boolean,
    tileGreen: Color,
    icon: ImageVector,
    iconTint: Color? = null,
    label: String,
    onClick: () -> Unit
) {
    val alpha = if (enabled) 1f else 0.45f
    val onTile = iconTint ?: MaterialTheme.colorScheme.onPrimary
    Column(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(Dimens.radiusSm))
            .background(tileGreen.copy(alpha = alpha))
            .clickable(enabled = enabled, onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = onTile,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = onTile
        )
    }
}
