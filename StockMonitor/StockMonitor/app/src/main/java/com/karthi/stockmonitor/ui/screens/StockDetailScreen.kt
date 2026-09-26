package com.karthi.stockmonitor.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.karthi.stockmonitor.viewmodel.StockViewModel

@Composable
fun StockDetailScreen(symbol: String, viewModel: StockViewModel) {
    val chartPoints by viewModel.chartPoints.collectAsState()
    var alertInput by remember { mutableStateOf("") }

    LaunchedEffect(symbol) {
        viewModel.loadChart(symbol)
    }

    Scaffold(topBar = { TopAppBar(title = { Text(symbol) }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            Text("30-day closing price", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            if (chartPoints.isEmpty()) {
                Text("Loading chart…")
            } else {
                LineChart(
                    values = chartPoints.map { it.second },
                    modifier = Modifier.fillMaxWidth().height(200.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            Text("Set a price alert", style = MaterialTheme.typography.titleMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = alertInput,
                    onValueChange = { alertInput = it },
                    label = { Text("Target price") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = {
                    alertInput.toDoubleOrNull()?.let { viewModel.setAlert(symbol, it) }
                }) {
                    Text("Set")
                }
            }
        }
    }
}

/**
 * A minimal line chart drawn directly with Canvas — avoids pulling in a
 * charting library dependency for a simple 30-point series.
 */
@Composable
fun LineChart(values: List<Double>, modifier: Modifier = Modifier) {
    if (values.isEmpty()) return
    val minVal = values.min()
    val maxVal = values.max()
    val range = (maxVal - minVal).takeIf { it != 0.0 } ?: 1.0

    Canvas(modifier = modifier) {
        val stepX = size.width / (values.size - 1).coerceAtLeast(1)
        val points = values.mapIndexed { index, value ->
            val x = index * stepX
            val y = size.height - ((value - minVal) / range * size.height).toFloat()
            Offset(x, y)
        }
        for (i in 0 until points.size - 1) {
            drawLine(
                color = Color(0xFF1565C0),
                start = points[i],
                end = points[i + 1],
                strokeWidth = 4f
            )
        }
    }
}
