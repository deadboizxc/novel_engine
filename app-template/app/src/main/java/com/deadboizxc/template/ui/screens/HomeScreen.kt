package com.deadboizxc.template.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deadboizxc.template.ui.components.*
import com.deadboizxc.template.ui.theme.*

/**
 * =============================================================================
 * HomeScreen - главный экран
 * =============================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToSettings: () -> Unit,
    onNavigateToDetail: (String) -> Unit
) {
    val items = remember {
        listOf(
            "item1" to "Элемент 1",
            "item2" to "Элемент 2", 
            "item3" to "Элемент 3"
        )
    }
    
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("App Template", color = TextPrimary) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                ),
                actions = {
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(
                            Icons.Default.Settings,
                            contentDescription = "Настройки",
                            tint = AccentPurple
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                ScreenTitle(
                    title = "Добро пожаловать",
                    subtitle = "Это базовый шаблон приложения"
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            item {
                PurpleCard {
                    Text(
                        "Используйте этот шаблон как основу для ваших приложений.",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    PurpleButton(
                        text = "Начать",
                        onClick = { onNavigateToDetail("demo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Список элементов",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimary
                )
            }
            
            items(items) { (id, title) ->
                PurpleCard(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PurpleListItem(
                        title = title,
                        subtitle = "ID: $id",
                        onClick = { onNavigateToDetail(id) }
                    )
                }
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
