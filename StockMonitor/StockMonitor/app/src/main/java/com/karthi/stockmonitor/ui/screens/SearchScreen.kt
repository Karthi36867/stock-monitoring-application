package com.karthi.stockmonitor.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.karthi.stockmonitor.viewmodel.StockViewModel

@Composable
fun SearchScreen(viewModel: StockViewModel) {
    val state by viewModel.searchState.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Search Stocks") }) }) { padding ->
        Column(modifier = Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Symbol or company name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                state.isLoading -> CircularProgressIndicator()
                state.error != null -> Text("Error: ${state.error}")
                else -> LazyColumn {
                    items(state.results) { (symbol, name) ->
                        ListItem(
                            headlineContent = { Text(symbol) },
                            supportingContent = { Text(name) },
                            trailingContent = {
                                TextButton(onClick = { viewModel.addToWatchlist(symbol, name) }) {
                                    Text("Add")
                                }
                            }
                        )
                        Divider()
                    }
                }
            }
        }
    }
}
