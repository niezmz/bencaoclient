package com.example.bencaoclient.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import android.widget.Toast
import com.example.bencaoclient.ApiKeyStore
import com.example.bencaoclient.ai.DoubaoAi
import com.example.bencaoclient.ui.theme.Dimens
import com.example.bencaoclient.util.appGreenTopAppBarColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiSettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var savedKeys by remember { mutableStateOf(ApiKeyStore.loadKeys(context)) }
    var inputText by remember { mutableStateOf("") }
    val activeKey = remember(savedKeys) { ApiKeyStore.getActiveKey(context) }

    fun refreshKeys() {
        savedKeys = ApiKeyStore.loadKeys(context)
    }

    fun maskKey(fullKey: String): String {
        if (fullKey.length <= 8) return fullKey.first() + "****" + fullKey.last()
        return fullKey.take(4) + "****" + fullKey.takeLast(4)
    }

    fun confirmKey(key: String) {
        val trimmed = key.trim()
        if (trimmed.isBlank()) return
        ApiKeyStore.addKey(context, trimmed)
        ApiKeyStore.setActiveKey(context, trimmed)
        DoubaoAi.setActiveApiKey(trimmed)
        inputText = ""
        refreshKeys()
        Toast.makeText(context, "API-KEY 已保存并设为当前使用", Toast.LENGTH_SHORT).show()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = { Text("AI 设置") },
            colors = appGreenTopAppBarColors(),
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            }
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = Dimens.lg, vertical = Dimens.md),
            verticalArrangement = Arrangement.spacedBy(Dimens.md)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.radiusMd),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Text(
                            text = "AI 模型说明",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "本APP默认使用火山方舟的 Doubao-Seed-1.6-vision 视觉模型进行植物识别。" +
                                   "配置你的 API-KEY 即可使用。开源版本可另行开发，配置其他任何支持图像识别的云端 AI。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(Dimens.radiusMd),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(Dimens.lg),
                        verticalArrangement = Arrangement.spacedBy(Dimens.sm)
                    ) {
                        Text(
                            text = "API-KEY",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        OutlinedTextField(
                            value = inputText,
                            onValueChange = { inputText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("请输入 API-KEY") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(
                                onDone = { confirmKey(inputText) }
                            )
                        )
                        Button(
                            onClick = { confirmKey(inputText) },
                            modifier = Modifier.align(Alignment.End),
                            enabled = inputText.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Text("确认保存")
                        }
                    }
                }
            }

            if (savedKeys.isNotEmpty()) {
                item {
                    Text(
                        text = "已保存的 API-KEY",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                items(savedKeys.size) { index ->
                    val fullKey = savedKeys[index]
                    val isActive = (fullKey == activeKey)

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(Dimens.radiusSm),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isActive)
                                MaterialTheme.colorScheme.primaryContainer
                            else
                                MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = if (isActive) 2.dp else 1.dp
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.md, vertical = Dimens.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = maskKey(fullKey),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (isActive) {
                                    Text(
                                        text = "当前使用中",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                            if (!isActive) {
                                TextButton(
                                    onClick = {
                                        ApiKeyStore.setActiveKey(context, fullKey)
                                        DoubaoAi.setActiveApiKey(fullKey)
                                        refreshKeys()
                                        Toast.makeText(context, "已切换 API-KEY", Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Text("使用")
                                }
                            }
                            IconButton(
                                onClick = {
                                    ApiKeyStore.deleteKey(context, index)
                                    val remaining = ApiKeyStore.loadKeys(context)
                                    val newActive = remaining.firstOrNull()
                                    DoubaoAi.setActiveApiKey(newActive)
                                    if (newActive != null) {
                                        ApiKeyStore.setActiveKey(context, newActive)
                                    }
                                    refreshKeys()
                                    Toast.makeText(context, "已删除", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "删除此 API-KEY",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}
