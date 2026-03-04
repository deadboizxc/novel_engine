package com.deadboizxc.template.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.deadboizxc.template.ui.components.*
import com.deadboizxc.template.ui.theme.*

/**
 * =============================================================================
 * DetailScreen - экран деталей элемента
 * =============================================================================
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    id: String,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Детали", color = TextPrimary) },
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ScreenTitle(
                title = "Элемент: $id",
                subtitle = "Это страница деталей"
            )
            
            PurpleCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Здесь можно отобразить детальную информацию о выбранном элементе.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "ID элемента: $id",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            PurpleButton(
                text = "Назад",
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                outlined = true
            )
        }
    }
}
