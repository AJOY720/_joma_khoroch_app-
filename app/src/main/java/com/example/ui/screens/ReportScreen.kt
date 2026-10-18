package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FilterListOff
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransactionEntity
import com.example.model.DateRangePreset
import com.example.ui.components.CategoryAndTypeFilters
import com.example.ui.components.CategoryBreakdownSection
import com.example.ui.components.DateRangeSelector
import com.example.ui.components.SummaryCards
import com.example.ui.components.TransactionItemCard
import com.example.util.FormatUtils
import com.example.viewmodel.MainViewModel

@Composable
fun ReportScreen(
    viewModel: MainViewModel,
    onEditTransaction: (TransactionEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val reportState by viewModel.reportUiState.collectAsStateWithLifecycle()
    val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val allTransactions by viewModel.allTransactions.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("report_screen_list"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Section Header
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "রিপোর্ট ও বিশ্লেষণ",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "তারিখ (Date to Date), ক্যাটাগরি ও ধরন অনুযায়ী বিস্তারিত হিসাব",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // 1. Date to Date Range Selector
        item {
            DateRangeSelector(
                startDate = reportState.startDate,
                endDate = reportState.endDate,
                selectedPreset = reportState.selectedPreset,
                onPresetSelected = { viewModel.selectPreset(it) },
                onCustomRangeSelected = { from, to -> viewModel.setCustomDateRange(from, to) }
            )
        }

        // 2. Report Financial Summary Cards (Report Totals)
        item {
            SummaryCards(
                totalIncome = reportState.totalIncome,
                totalExpense = reportState.totalExpense,
                balance = reportState.netBalance,
                currencySymbol = currencySymbol,
                titlePrefix = "রিপোর্টে "
            )
        }

        // 3. Category & Type Filters + Search Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CategoryAndTypeFilters(
                        selectedType = reportState.selectedType,
                        onTypeSelected = { viewModel.setTypeFilter(it) },
                        selectedCategory = reportState.selectedCategory,
                        onCategorySelected = { viewModel.setCategoryFilter(it) },
                        searchQuery = reportState.searchQuery,
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) }
                    )
                }
            }
        }

        // 4. Category-wise Visual Breakdown Section
        item {
            val totalVolume = reportState.totalIncome + reportState.totalExpense
            CategoryBreakdownSection(
                categorySummaries = reportState.categoryBreakdown,
                totalReportVolume = totalVolume,
                currencySymbol = currencySymbol,
                onCategoryClick = { selectedCat ->
                    if (reportState.selectedCategory == selectedCat) {
                        viewModel.setCategoryFilter("ALL")
                    } else {
                        viewModel.setCategoryFilter(selectedCat)
                    }
                }
            )
        }

        // 5. CSV Export and Share Actions
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        shareReportCsv(
                            context = context,
                            transactions = reportState.filteredTransactions,
                            currencySymbol = currencySymbol,
                            startDate = reportState.startDate,
                            endDate = reportState.endDate
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_export_report"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "রিপোর্ট CSV শেয়ার")
                }

                OutlinedButton(
                    onClick = {
                        shareReportCsv(
                            context = context,
                            transactions = allTransactions,
                            currencySymbol = currencySymbol,
                            startDate = "All",
                            endDate = "All"
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_export_all"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "সব হিসাব CSV")
                }
            }
        }

        // 6. Filtered Transactions List Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReceiptLong,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Text(
                        text = "ফিল্টার করা হিসাবের তালিকা",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Text(
                    text = "${reportState.transactionCount}টি রেকর্ড",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }

        // Empty state when filtered out
        if (reportState.filteredTransactions.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                        .testTag("report_empty_card"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterListOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "নির্বাচিত তারিখে বা ফিল্টারে কোনো হিসাব পাওয়া যায়নি",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(
                            onClick = {
                                viewModel.selectPreset(DateRangePreset.ALL_TIME)
                                viewModel.setTypeFilter("ALL")
                                viewModel.setCategoryFilter("ALL")
                                viewModel.setSearchQuery("")
                            }
                        ) {
                            Text("সব ফিল্টার সাফ করুন")
                        }
                    }
                }
            }
        } else {
            // Render list of matching transactions
            items(
                items = reportState.filteredTransactions,
                key = { it.id }
            ) { item ->
                TransactionItemCard(
                    item = item,
                    currencySymbol = currencySymbol,
                    onEdit = onEditTransaction,
                    onDelete = { viewModel.deleteTransaction(it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}

private fun shareReportCsv(
    context: Context,
    transactions: List<TransactionEntity>,
    currencySymbol: String,
    startDate: String,
    endDate: String
) {
    if (transactions.isEmpty()) {
        Toast.makeText(context, "ডাউনলোড করার মতো কোনো হিসাব নেই।", Toast.LENGTH_SHORT).show()
        return
    }

    val csv = FormatUtils.generateCsv(transactions, currencySymbol)
    val sendIntent = Intent().apply {
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, csv)
        putExtra(Intent.EXTRA_TITLE, "জমা-খরচ রিপোর্ট ($startDate থেকে $endDate)")
        type = "text/csv"
    }
    val shareIntent = Intent.createChooser(sendIntent, "রিপোর্ট CSV শেয়ার করুন")
    context.startActivity(shareIntent)
}
