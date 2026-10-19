package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.FormatUtils
import com.example.viewmodel.BudgetStatus

val WarningAmberColor = Color(0xFFE65100)
val WarningBgColor = Color(0xFFFFF3E0)

@Composable
fun BudgetAlertCard(
    budgetStatus: BudgetStatus,
    currencySymbol: String,
    onEditBudget: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isExceeded = budgetStatus.isExceeded
    val isWarning = budgetStatus.isWarning

    val containerColor = when {
        isExceeded -> ExpenseRed.copy(alpha = 0.10f)
        isWarning -> WarningBgColor
        else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
    }

    val borderColor = when {
        isExceeded -> ExpenseRed.copy(alpha = 0.7f)
        isWarning -> WarningAmberColor.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

    val progressColor = when {
        isExceeded -> ExpenseRed
        isWarning -> WarningAmberColor
        else -> MaterialTheme.colorScheme.primary
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (budgetStatus.monthlyBudget > 0) {
            (budgetStatus.currentMonthExpense / budgetStatus.monthlyBudget).toFloat().coerceIn(0f, 1f)
        } else 0f,
        label = "budget_progress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("budget_alert_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header: Title, Alert Status Badge, and Edit Action
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = when {
                                    isExceeded -> ExpenseRed
                                    isWarning -> WarningAmberColor
                                    else -> MaterialTheme.colorScheme.primary
                                },
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when {
                                isExceeded -> Icons.Default.Warning
                                isWarning -> Icons.Default.NotificationsActive
                                else -> Icons.Default.AccountBalanceWallet
                            },
                            contentDescription = "বাজেট সতর্কতা",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Column {
                        Text(
                            text = "মাসিক বাজেট সতর্কতা",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                isExceeded -> "জরুরি সতর্কতা: বাজেট অতিক্রম করেছে!"
                                isWarning -> "সতর্কতা: বাজেটের ৮০% এর বেশি শেষ!"
                                else -> "চলতি মাসের খরচ বাজেটের মধ্যে আছে"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isExceeded || isWarning) FontWeight.SemiBold else FontWeight.Normal,
                            color = when {
                                isExceeded -> ExpenseRed
                                isWarning -> WarningAmberColor
                                else -> MaterialTheme.colorScheme.outline
                            }
                        )
                    }
                }

                OutlinedButton(
                    onClick = onEditBudget,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.testTag("edit_budget_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "বাজেট পরিবর্তন",
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "বাজেট সেট",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Progress Bar & Percentage
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "বাজেট ব্যবহার: ${budgetStatus.usagePercentage}%",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = when {
                            isExceeded -> ExpenseRed
                            isWarning -> WarningAmberColor
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                    Text(
                        text = "${FormatUtils.formatMoney(budgetStatus.currentMonthExpense, currencySymbol)} / ${FormatUtils.formatMoney(budgetStatus.monthlyBudget, currencySymbol)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                LinearProgressIndicator(
                    progress = { animatedProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp)),
                    color = progressColor,
                    trackColor = progressColor.copy(alpha = 0.2f),
                )
            }

            // Stats Row: Expenses, Budget, Remaining
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Column 1: This month expense
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "চলতি মাসের খরচ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = FormatUtils.formatMoney(budgetStatus.currentMonthExpense, currencySymbol),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ExpenseRed
                    )
                }

                // Column 2: Total budget
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "নির্ধারিত বাজেট",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = FormatUtils.formatMoney(budgetStatus.monthlyBudget, currencySymbol),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Column 3: Remaining or Exceeded
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isExceeded) "অতিরিক্ত খরচ" else "অবশিষ্ট বাজেট",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isExceeded) ExpenseRed else MaterialTheme.colorScheme.outline
                    )
                    Text(
                        text = if (isExceeded) {
                            "+ " + FormatUtils.formatMoney(budgetStatus.currentMonthExpense - budgetStatus.monthlyBudget, currencySymbol)
                        } else {
                            FormatUtils.formatMoney(budgetStatus.remainingBudget, currencySymbol)
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isExceeded) ExpenseRed else IncomeGreen
                    )
                }
            }

            // Exceeded or Warning Alert Notification Callout
            if (isExceeded) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ExpenseRed.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = ExpenseRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "বাজেট সতর্কতা: আপনার এই মাসের খরচ বাজেট সীমা অতিক্রম করে গেছে (${FormatUtils.formatMoney(budgetStatus.currentMonthExpense - budgetStatus.monthlyBudget, currencySymbol)} অতিরিক্ত)। সাশ্রয়ী হোন!",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (isWarning) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WarningAmberColor.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = WarningAmberColor,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "বাজেট সতর্কতা: এই মাসের বাজেটের ৮০% এর বেশি শেষ হয়েছে! হাতে আছে মাত্র ${FormatUtils.formatMoney(budgetStatus.remainingBudget, currencySymbol)}।",
                            style = MaterialTheme.typography.bodySmall,
                            color = WarningAmberColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onSaveBudget: (Double) -> Unit
) {
    var budgetInput by remember {
        mutableStateOf(if (currentBudget % 1 == 0.0) currentBudget.toLong().toString() else currentBudget.toString())
    }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val presetBudgets = listOf(10000.0, 20000.0, 25000.0, 35000.0, 50000.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountBalanceWallet,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "মাসিক বাজেট নির্ধারণ",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Text(
                    text = "প্রতি মাসের সর্বোচ্চ খরচের লক্ষ্যমাত্রা নির্ধারণ করুন। খরচের পরিমাণ ৮০% বা ১০০% হলে অ্যাপ আপনাকে স্বয়ংক্রিয় সতর্কতা দেখাবে।",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = budgetInput,
                    onValueChange = {
                        budgetInput = it
                        errorMessage = null
                    },
                    label = { Text("মাসিক বাজেট ($currencySymbol)") },
                    prefix = { Text("$currencySymbol ") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("budget_input_field"),
                    isError = errorMessage != null
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = ExpenseRed,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // Quick preset chips
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "দ্রুত নির্বাচন করুন:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetBudgets.take(3).forEach { amount ->
                            FilterChip(
                                selected = budgetInput.toDoubleOrNull() == amount,
                                onClick = { budgetInput = amount.toLong().toString() },
                                label = { Text(FormatUtils.formatMoney(amount, currencySymbol)) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presetBudgets.drop(3).forEach { amount ->
                            FilterChip(
                                selected = budgetInput.toDoubleOrNull() == amount,
                                onClick = { budgetInput = amount.toLong().toString() },
                                label = { Text(FormatUtils.formatMoney(amount, currencySymbol)) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = budgetInput.trim().toDoubleOrNull()
                    if (parsed == null || parsed <= 0) {
                        errorMessage = "দয়া করে একটি সঠিক বাজেটের পরিমাণ লিখুন (০ এর বেশি)।"
                    } else {
                        onSaveBudget(parsed)
                        onDismiss()
                    }
                },
                modifier = Modifier.testTag("save_budget_button")
            ) {
                Text("সংরক্ষণ করুন")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("বাতিল")
            }
        }
    )
}
