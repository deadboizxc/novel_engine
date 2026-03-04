package com.deadboizxc.template.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deadboizxc.template.ui.components.*
import com.deadboizxc.template.ui.theme.*

/**
 * =============================================================================
 * SettingsScreen - экран настроек
 * =============================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    var notificationsEnabled by remember { mutableStateOf(true) }
    var darkMode by remember { mutableStateOf(true) }
    
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Настройки", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Назад",
                            tint = AccentPurple
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            PurpleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Уведомления", color = TextPrimary)
                        Text("Получать push-уведомления", 
                             style = MaterialTheme.typography.bodyMedium,
                             color = TextSecondary)
                    }
                    Switch(
                        checked = notificationsEnabled,
                        onCheckedChange = { notificationsEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = PrimaryPurpleDark
                        )
                    )
                }
            }
            
            PurpleCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Тёмная тема", color = TextPrimary)
                        Text("Использовать тёмный режим", 
                             style = MaterialTheme.typography.bodyMedium,
                             color = TextSecondary)
                    }
                    Switch(
                        checked = darkMode,
                        onCheckedChange = { darkMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AccentPurple,
                            checkedTrackColor = PrimaryPurpleDark
                        )
                    )
                }
            }
            
            PurpleCard {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("О приложении", color = TextPrimary)
                    PurpleDivider()
                    Text("Версия: 1.0.0", color = TextSecondary)
                    Text("Автор: deadboizxc", color = TextSecondary)
                }
            }
        }
    }
}
