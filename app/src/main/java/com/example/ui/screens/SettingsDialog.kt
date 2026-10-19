package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EditBudgetDialog
import com.example.ui.theme.ExpenseRed
import com.example.util.FormatUtils
import com.example.viewmodel.MainViewModel

@Composable
fun SettingsDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val currentCurrency by viewModel.currencySymbol.collectAsStateWithLifecycle()
    val isPinConfigured by viewModel.isPinConfigured.collectAsStateWithLifecycle()
    val monthlyBudget by viewModel.monthlyBudget.collectAsStateWithLifecycle()

    var showPinSetupDialog by remember { mutableStateOf(false) }
    var showRemovePinDialog by remember { mutableStateOf(false) }
    var showEditBudgetDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "সেটিংস ও নিরাপত্তা",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ")
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "মুদ্রা প্রতীক (Currency Symbol)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("₹", "৳", "$").forEach { symbol ->
                            FilterChip(
                                selected = currentCurrency == symbol,
                                onClick = { viewModel.setCurrency(symbol) },
                                label = {
                                    Text(
                                        when (symbol) {
                                            "₹" -> "₹ রুপি (INR)"
                                            "৳" -> "৳ টাকা (BDT)"
                                            else -> "$ ডলার (USD)"
                                        }
                                    )
                                },
                                modifier = Modifier.testTag("currency_$symbol")
                            )
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "PIN নিরাপত্তা লক",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = if (isPinConfigured) "বর্তমানে একটি PIN লক সক্রিয় আছে।" else "অ্যাপ সুরক্ষিত রাখতে PIN লক সেট করুন।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (isPinConfigured) {
                            Button(
                                onClick = { showPinSetupDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("PIN পরিবর্তন")
                            }
                            OutlinedButton(
                                onClick = { showRemovePinDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("PIN নিষ্ক্রিয়", color = ExpenseRed)
                            }
                        } else {
                            Button(
                                onClick = { showPinSetupDialog = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("নতুন PIN সেট করুন")
                            }
                        }
                    }
                }

                HorizontalDivider()

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Text(
                            text = "মাসিক বাজেট ও সতর্কতা",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Text(
                        text = "বর্তমান মাসিক বাজেট: ${FormatUtils.formatMoney(monthlyBudget, currentCurrency)}\nবাজেটের ৮০% খরচ হলে অ্যাপে সতর্কতা সংকেত এবং ১০০% অতিক্রান্ত হলে জরুরি লাল সংকেত প্রদর্শিত হয়।",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )

                    Button(
                        onClick = { showEditBudgetDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_edit_budget_button")
                    ) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মাসিক বাজেট পরিবর্তন করুন")
                    }
                }

                HorizontalDivider()

                Text(
                    text = "জমা-খরচ • ব্যক্তিগত হিসাব\nডেটা সম্পূর্ণভাবে আপনার ডিভাইসে সুরক্ষিত থাকে।",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("সম্পন্ন")
            }
        }
    )

    if (showPinSetupDialog) {
        var newPin by remember { mutableStateOf("") }
        var confirmPin by remember { mutableStateOf("") }
        var pinError by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showPinSetupDialog = false },
            title = { Text(if (isPinConfigured) "নতুন PIN দিন" else "PIN সেট করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("কমপক্ষে ৪ সংখ্যার PIN লিখুন:")
                    OutlinedTextField(
                        value = newPin,
                        onValueChange = { newPin = it; pinError = null },
                        label = { Text("নতুন PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = confirmPin,
                        onValueChange = { confirmPin = it; pinError = null },
                        label = { Text("নিশ্চিত করুন") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    if (pinError != null) {
                        Text(text = pinError!!, color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newPin.length < 4) {
                            pinError = "কমপক্ষে ৪ অক্ষর/সংখ্যা দিন।"
                            return@Button
                        }
                        if (newPin != confirmPin) {
                            pinError = "দুটি PIN মিলছে না।"
                            return@Button
                        }
                        viewModel.setPin(newPin)
                        Toast.makeText(context, "PIN সফলভাবে সেট হয়েছে।", Toast.LENGTH_SHORT).show()
                        showPinSetupDialog = false
                    }
                ) {
                    Text("সেভ করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPinSetupDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showRemovePinDialog) {
        var currentPin by remember { mutableStateOf("") }
        var error by remember { mutableStateOf<String?>(null) }

        AlertDialog(
            onDismissRequest = { showRemovePinDialog = false },
            title = { Text("PIN নিষ্ক্রিয় করুন") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("বর্তমান PIN দিয়ে নিশ্চিত করুন:")
                    OutlinedTextField(
                        value = currentPin,
                        onValueChange = { currentPin = it; error = null },
                        label = { Text("বর্তমান PIN") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                        singleLine = true
                    )
                    if (error != null) {
                        Text(text = error!!, color = ExpenseRed, style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val removed = viewModel.removePin(currentPin)
                        if (removed) {
                            Toast.makeText(context, "PIN নিষ্ক্রিয় করা হয়েছে।", Toast.LENGTH_SHORT).show()
                            showRemovePinDialog = false
                        } else {
                            error = "বর্তমান PIN সঠিক নয়।"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) {
                    Text("নিষ্ক্রিয় করুন")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRemovePinDialog = false }) {
                    Text("বাতিল")
                }
            }
        )
    }

    if (showEditBudgetDialog) {
        EditBudgetDialog(
            currentBudget = monthlyBudget,
            currencySymbol = currentCurrency,
            onDismiss = { showEditBudgetDialog = false },
            onSaveBudget = { newBudget ->
                viewModel.setMonthlyBudget(newBudget)
                Toast.makeText(context, "মাসিক বাজেট আপডেট করা হয়েছে।", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
