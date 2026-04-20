package com.example.a216553_praavieen_rajj_nelson_lab04

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeScreen(profile: UserProfile, onViewProfile: () -> Unit, onLogout: () -> Unit) {
    val displayName = when {
        profile.fullName.isNotEmpty() -> profile.fullName
        profile.username.isNotEmpty() -> profile.username
        else -> "User"
    }

    // The data list
    val allEnvelopes = remember {
        listOf(
            Envelope("Food", "🍔", 300f, 120f),
            Envelope("Transport", "🚗", 200f, 80f),
            Envelope("Entertainment", "🎬", 150f, 60f),
            Envelope("Shopping", "🛍️", 250f, 200f),
            Envelope("Health", "💊", 100f, 30f),
            Envelope("Utilities", "💡", 180f, 175f)
        )
    }

    var searchQuery by remember { mutableStateOf("") }

    val filteredEnvelopes = remember(searchQuery) {
        if (searchQuery.isEmpty()) allEnvelopes
        else allEnvelopes.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    // Screen-in animation
    var screenVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { screenVisible = true }

    AnimatedVisibility(
        visible = screenVisible,
        enter = fadeIn(tween(400)) + slideInVertically(animationSpec = tween(500), initialOffsetY = { it / 5 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("216553 | Smart Budget", fontSize = 11.sp, color = MaterialTheme.colorScheme.outline)
                Button(onClick = onLogout) { Text("Logout") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Primary Balance Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text("Hello, $displayName 👋", color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.bodyLarge)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("RM 850.00", style = MaterialTheme.typography.displayMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    Text("Available Balance", color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(onClick = onViewProfile, modifier = Modifier.fillMaxWidth()) {
                Text("View My Profile →")
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text("Budget Envelopes", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)

            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Search envelopes...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = null)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // List of items
            if (filteredEnvelopes.isEmpty()) {
                Text("No match for \"$searchQuery\"", modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outline)
            }

            filteredEnvelopes.forEach { envelope ->
                ExpandableEnvelopeCard(envelope.title, envelope.emoji, envelope.budgetTotal, envelope.budgetSpent)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ExpandableEnvelopeCard(title: String, emoji: String, budgetTotal: Float, budgetSpent: Float) {
    var expanded by remember { mutableStateOf(false) }
    val progress = (budgetSpent / budgetTotal).coerceIn(0f, 1f)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(emoji, fontSize = 26.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                        Text("RM %.0f / RM %.0f".format(budgetSpent, budgetTotal), style = MaterialTheme.typography.bodySmall)
                    }
                }
                Icon(if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore, contentDescription = null)
            }

            if (expanded) {
                Column(modifier = Modifier.padding(top = 14.dp)) {
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = if (progress >= 0.9f) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Remaining: RM %.2f".format(budgetTotal - budgetSpent), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}