package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.AnamolApplication
import com.example.R
import com.example.data.model.ScanItem
import com.example.ui.theme.CyberBackground
import com.example.ui.theme.CyberCard
import com.example.ui.theme.CyberCardBorder
import com.example.ui.theme.CyberTextMuted
import com.example.ui.theme.CyberTextPrimary
import com.example.ui.theme.CyberTextSecondary
import com.example.ui.theme.MatrixGreen
import com.example.ui.theme.MonospaceFont
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.SolarAmber
import com.example.ui.theme.ThreatDanger
import com.example.ui.theme.ThreatSafe
import com.example.ui.theme.ThreatWarning
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    onInspectItem: (raw: String, format: String) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val app = context.applicationContext as? AnamolApplication ?: return

    val scans by app.repository.allScans.collectAsStateWithLifecycle(initialValue = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    var favoritesOnly by remember { mutableStateOf(false) }
    var showPurgeDialog by remember { mutableStateOf(false) }

    val filteredScans = scans.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.rawContent.contains(searchQuery, ignoreCase = true) ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.parsedType.contains(searchQuery, ignoreCase = true)
        val matchesFav = !favoritesOnly || item.isBookmarked
        matchesQuery && matchesFav
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberBackground)
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 44.dp, start = 20.dp, end = 20.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(10.dp).background(MatrixGreen, CircleShape))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.history_title),
                        color = NeonCyan,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
                Text(
                    text = "${scans.size} TELEMETRY RECORDS STORED",
                    color = CyberTextSecondary,
                    fontFamily = MonospaceFont,
                    fontSize = 11.sp
                )
            }

            Row {
                // Export JSON
                IconButton(
                    onClick = {
                        val json = app.repository.exportToJson(filteredScans)
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            putExtra(Intent.EXTRA_TEXT, json)
                            type = "application/json"
                        }
                        context.startActivity(Intent.createChooser(sendIntent, "Export JSON"))
                    },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberCard, CircleShape)
                        .border(1.dp, CyberCardBorder, CircleShape)
                ) {
                    Icon(Icons.Default.Share, contentDescription = "Export JSON", tint = NeonCyan, modifier = Modifier.size(16.dp))
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Purge All
                IconButton(
                    onClick = { showPurgeDialog = true },
                    modifier = Modifier
                        .size(36.dp)
                        .background(CyberCard, CircleShape)
                        .border(1.dp, NeonCrimson.copy(alpha = 0.5f), CircleShape)
                ) {
                    Icon(Icons.Default.DeleteSweep, contentDescription = "Purge All", tint = NeonCrimson, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text(stringResource(R.string.search_placeholder), fontFamily = MonospaceFont, fontSize = 12.sp, color = CyberTextMuted) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = NeonCyan) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonCyan,
                unfocusedBorderColor = CyberCardBorder,
                focusedContainerColor = CyberCard,
                unfocusedContainerColor = CyberCard,
                focusedTextColor = CyberTextPrimary,
                unfocusedTextColor = CyberTextPrimary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 6.dp)
        )

        // Filter Chips & Export CSV
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterChip(
                selected = favoritesOnly,
                onClick = { favoritesOnly = !favoritesOnly },
                label = { Text("Favorites Only", fontFamily = MonospaceFont, fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = SolarAmber,
                    selectedLabelColor = Color.Black
                )
            )

            Button(
                onClick = {
                    val csv = app.repository.exportToCsv(filteredScans)
                    val sendIntent = Intent(Intent.ACTION_SEND).apply {
                        putExtra(Intent.EXTRA_TEXT, csv)
                        type = "text/csv"
                    }
                    context.startActivity(Intent.createChooser(sendIntent, "Export CSV"))
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.12f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.FileDownload, contentDescription = null, tint = MatrixGreen, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Export CSV", color = MatrixGreen, fontFamily = MonospaceFont, fontSize = 11.sp)
            }
        }

        // Log Items List
        if (filteredScans.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.history_empty),
                    color = CyberTextSecondary,
                    fontFamily = MonospaceFont,
                    fontSize = 13.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredScans, key = { it.id }) { item ->
                    ScanHistoryItemCard(
                        item = item,
                        onClick = { onInspectItem(item.rawContent, item.format) },
                        onToggleBookmark = {
                            coroutineScope.launch { app.repository.toggleBookmark(item) }
                        },
                        onDelete = {
                            coroutineScope.launch { app.repository.delete(item) }
                        }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }

    // Purge confirmation dialog
    if (showPurgeDialog) {
        AlertDialog(
            onDismissRequest = { showPurgeDialog = false },
            title = { Text("PURGE TELEMETRY DATABASE?", color = NeonCrimson, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold) },
            text = { Text("All scanned logs and encrypted telemetry will be permanently destroyed.", color = CyberTextPrimary) },
            containerColor = CyberCard,
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch { app.repository.clearAll() }
                        showPurgeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonCrimson)
                ) {
                    Text("PURGE ALL", color = Color.White, fontFamily = MonospaceFont, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPurgeDialog = false }) {
                    Text("CANCEL", color = CyberTextSecondary, fontFamily = MonospaceFont)
                }
            }
        )
    }
}

@Composable
private fun ScanHistoryItemCard(
    item: ScanItem,
    onClick: () -> Unit,
    onToggleBookmark: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US) }
    val formattedDate = remember(item.timestamp) { dateFormat.format(Date(item.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = CyberCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.format,
                        color = NeonCyan,
                        fontFamily = MonospaceFont,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.securityRating,
                        color = when (item.securityRating) {
                            "SAFE" -> ThreatSafe
                            "WARNING" -> ThreatWarning
                            else -> ThreatDanger
                        },
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formattedDate,
                        color = CyberTextMuted,
                        fontFamily = MonospaceFont,
                        fontSize = 10.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = item.title.ifBlank { item.rawContent },
                    color = CyberTextPrimary,
                    fontFamily = MonospaceFont,
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            IconButton(onClick = onToggleBookmark, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (item.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                    contentDescription = "Bookmark",
                    tint = if (item.isBookmarked) SolarAmber else CyberTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = CyberTextMuted,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
