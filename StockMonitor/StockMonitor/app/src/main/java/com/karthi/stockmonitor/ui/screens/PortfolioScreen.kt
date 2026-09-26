package com.karthi.stockmonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.karthi.stockmonitor.viewmodel.StockViewModel

@Composable
fun PortfolioScreen(viewModel: StockViewModel) {
    val portfolio by viewModel.portfolio.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Portfolio") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add holding")
            }
        }
    ) { padding ->
        if (portfolio.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("No holdings yet. Tap + to add one.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding).fillMaxSize()) {
                items(portfolio, key = { it.symbol }) { holding ->
                    val gainLoss = viewModel.gainLossFor(holding)
                    val color = if (gainLoss >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)

                    ListItem(
                        headlineContent = { Text(holding.symbol, fontWeight = FontWeight.Bold) },
                        supportingContent = {
                            Text("${holding.quantity} shares @ ₹${"%.2f".format(holding.buyPrice)}")
                        },
                        trailingContent = {
                            Text(
                                "${if (gainLoss >= 0) "+" else ""}₹${"%.2f".format(gainLoss)}",
                                color = color,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    )
                    Divider()
                }
            }
        }

        if (showDialog) {
            AddHoldingDialog(
                onDismiss = { showDialog = false },
                onConfirm = { symbol, name, qty, price ->
                    viewModel.addHolding(symbol, name, qty, price)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddHoldingDialog(
    onDismiss: () -> Unit,
    onConfirm: (symbol: String, name: String, qty: Double, buyPrice: Double) -> Unit
) {
    var symbol by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var qty by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Holding") },
        text = {
            Column {
                OutlinedTextField(value = symbol, onValueChange = { symbol = it }, label = { Text("Symbol") })
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Company name") })
                OutlinedTextField(value = qty, onValueChange = { qty = it }, label = { Text("Quantity") })
                OutlinedTextField(value = price, onValueChange = { price = it }, label = { Text("Buy price") })
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val q = qty.toDoubleOrNull()
                val p = price.toDoubleOrNull()
                if (symbol.isNotBlank() && q != null && p != null) {
                    onConfirm(symbol.uppercase(), name, q, p)
                }
            }) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
