package com.karthi.stockmonitor.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karthi.stockmonitor.data.local.StockEntity
import com.karthi.stockmonitor.viewmodel.StockViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun WatchlistScreen(viewModel: StockViewModel, onStockClick: (String) -> Unit) {
    val watchlist by viewModel.watchlist.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Watchlist") },
                actions = {
                    IconButton(onClick = { viewModel.refreshAll() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh prices")
                    }
                }
            )
        }
    ) { padding ->
        if (watchlist.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("No stocks yet. Add some from the Search tab.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(watchlist, key = { it.symbol }) { stock ->
                    WatchlistRow(
                        stock = stock,
                        onClick = { onStockClick(stock.symbol) },
                        onRemove = { viewModel.removeFromWatchlist(stock.symbol) }
                    )
                    Divider()
                }
            }
        }
    }
}

@Composable
private fun WatchlistRow(stock: StockEntity, onClick: () -> Unit, onRemove: () -> Unit) {
    val changeColor = if (stock.changePercent >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
    val timeFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(stock.symbol, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(stock.companyName, style = MaterialTheme.typography.bodySmall)
            Text(
                "Updated ${timeFormat.format(Date(stock.lastUpdated))}",
                style = MaterialTheme.typography.labelSmall
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(horizontalAlignment = Alignment.End) {
                Text("₹${"%.2f".format(stock.lastPrice)}", fontWeight = FontWeight.Bold)
                Text(
                    "${if (stock.changePercent >= 0) "+" else ""}${"%.2f".format(stock.changePercent)}%",
                    color = changeColor
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Default.Delete, contentDescription = "Remove ${stock.symbol}")
            }
        }
    }
}

