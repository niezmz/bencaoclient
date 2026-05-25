package com.example.bencaoclient.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.bencaoclient.model.SpeciesBackupOverlayUi
import com.example.bencaoclient.ui.theme.Dimens

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SpeciesBackupBlockingOverlay(ui: SpeciesBackupOverlayUi) {
    val absorbTouch = remember { MutableInteractionSource() }
    val determinate = ui.stepTotal > 0
    val fraction = if (determinate) {
        (ui.stepDone.toFloat() / ui.stepTotal.toFloat()).coerceIn(0f, 1f)
    } else {
        0f
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.55f))
            .clickable(
                indication = null,
                interactionSource = absorbTouch,
                onClick = {}
            ),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.padding(Dimens.xl),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(Dimens.lg),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Dimens.md)
            ) {
                Text(
                    text = ui.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                if (determinate) {
                    Text(
                        text = "${ui.stepDone} / ${ui.stepTotal}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                Text(
                    text = "请勿离开本页，不要切换到其它底部标签或返回桌面，直至进度结束，否则可能导致备份损坏或失败。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
