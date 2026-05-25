package com.example.bencaoclient.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.bencaoclient.ui.theme.Dimens

@Composable
fun BencaoTaxonomyTag(label: String, value: String) {
    if (value.isBlank()) return
    Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = RoundedCornerShape(Dimens.radiusSm)
    ) {
        Text(
            text = "$label $value",
            modifier = Modifier.padding(horizontal = Dimens.sm, vertical = Dimens.xs),
            style = MaterialTheme.typography.labelMedium
        )
    }
}
