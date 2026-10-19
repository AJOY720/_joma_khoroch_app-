package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.TransactionEntity
import com.example.model.CategoryRegistry
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.util.FormatUtils
import com.example.viewmodel.BudgetStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditTransactionSheet(
    itemToEdit: TransactionEntity?,
    currencySymbol: String,
    budgetStatus: BudgetStatus? = null,
    onDismiss: () -> Unit,
    onSave: (
        type: String,
        amount: Double,
        note: String,
        category: String,
        date: String,
        paymentMethod: String,
        isRecurring: Boolean,
        recurringDayOfMonth: Int
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var type by remember { mutableStateOf(itemToEdit?.type ?: "expense") }
    var amountText by remember { mutableStateOf(itemToEdit?.amount?.let { if (it % 1 == 0.0) it.toLong().toString() else it.toString() } ?: "") }
    var note by remember { mutableStateOf(itemToEdit?.note ?: "") }
    var category by remember { mutableStateOf(itemToEdit?.category ?: "বাজার") }
    var date by remember { mutableStateOf(itemToEdit?.date ?: FormatUtils.todayDateString()) }
    var paymentMethod by remember {
        mutableStateOf(
            if (itemToEdit?.paymentMethod == "বিকাশ") "UPI অনলাইন"
            else (itemToEdit?.paymentMethod ?: "নগদ")
        )
    }

    // Recurring state
    var isRecurring by remember { mutableStateOf(itemToEdit?.isRecurring ?: false) }
    var recurringDay by remember {
        mutableStateOf(
            itemToEdit?.recurringDayOfMonth ?: try {
                LocalDate.parse(date).dayOfMonth
            } catch (e: Exception) {
                1
            }
        )
    }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (itemToEdit == null) "নতুন হিসাব যোগ করুন" else "হিসাব সম্পাদনা করুন",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "বন্ধ করুন")
                }
            }

            // Type Toggle
            val typeOptions = listOf(
                Pair("income", "জমা (+)"),
                Pair("expense", "খরচ (−)")
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                typeOptions.forEachIndexed { index, (key, label) ->
                    SegmentedButton(
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = 2),
                        onClick = {
                            type = key
                            if (key == "income" && category == "বাজার") {
                                category = "বেতন"
                            } else if (key == "expense" && category == "বেতন") {
                                category = "বাজার"
                            }
                        },
                        selected = type == key,
                        icon = {},
                        modifier = Modifier.testTag("form_type_$key")
                    ) {
                        Text(
                            text = label,
                            fontWeight = FontWeight.Bold,
                            color = if (type == key) {
                                if (key == "income") IncomeGreen else ExpenseRed
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
            }

            // Amount Field
            OutlinedTextField(
                value = amountText,
                onValueChange = {
                    amountText = it
                    errorMessage = null
                },
                label = { Text("টাকার পরিমাণ ($currencySymbol)") },
                placeholder = { Text("যেমন: 500") },
                prefix = { Text("$currencySymbol ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_amount_input")
            )

            // Note Field
            OutlinedTextField(
                value = note,
                onValueChange = {
                    note = it
                    errorMessage = null
                },
                label = { Text("বিবরণ") },
                placeholder = { Text("যেমন: বাড়িভাড়া, নেট বিল, সাবস্ক্রিপশন, বেতন") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_note_input")
            )

            // Category Selection Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "ক্যাটাগরি",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryRegistry.categoryNames.forEach { catName ->
                        val isSelected = category == catName
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                category = catName
                                // Auto-suggest recurring for rent, subscription, or bills
                                if (catName == "বাড়িভাড়া" || catName == "বিল" || catName == "সাবস্ক্রিপশন/ফি") {
                                    isRecurring = true
                                }
                            },
                            label = { Text(catName) },
                            modifier = Modifier.testTag("form_cat_$catName")
                        )
                    }
                }
            }

            // Date Picker Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = date,
                    onValueChange = { date = it },
                    label = { Text("তারিখ (YYYY-MM-DD)") },
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = "তারিখ বাছুন",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("form_date_input")
                )

                TextButton(
                    onClick = {
                        date = FormatUtils.todayDateString()
                        recurringDay = LocalDate.now().dayOfMonth
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("আজ")
                }
            }

            // --- RECURRING MONTHLY FEATURE TOGGLE ---
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_recurring_card"),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isRecurring) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Column {
                                Text(
                                    text = "প্রতি মাসে স্বয়ংক্রিয় যোগ (Recurring)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "বাড়িভাড়া, বিল বা সাবস্ক্রিপশন নির্দিষ্ট দিনে স্বয়ংক্রিয় যোগ হবে",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }

                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            ),
                            modifier = Modifier.testTag("recurring_toggle_switch")
                        )
                    }

                    AnimatedVisibility(visible = isRecurring) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "প্রতি মাসের কত তারিখে যোগ হবে?",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            // Quick Day Chips (1st, 5th, 10th, 15th, 20th, 25th, 30th)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf(1, 5, 10, 15, 20, 25, 28, 30).forEach { day ->
                                    FilterChip(
                                        selected = recurringDay == day,
                                        onClick = { recurringDay = day },
                                        label = { Text("${day} তারিখ") },
                                        modifier = Modifier.testTag("recurring_day_$day")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Payment Method Quick Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "পেমেন্ট মাধ্যম",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val methods = listOf("নগদ", "UPI অনলাইন", "ব্যাংক", "কার্ড")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    methods.forEach { method ->
                        FilterChip(
                            selected = paymentMethod == method,
                            onClick = { paymentMethod = method },
                            label = { Text(method) }
                        )
                    }
                }
            }

            // Real-time Monthly Budget Alert
            val enteredAmount = amountText.toDoubleOrNull() ?: 0.0
            val oldAmountIfExpense = if (itemToEdit?.type == "expense") (itemToEdit.amount) else 0.0
            val willExceed = type == "expense" && (budgetStatus != null) && (budgetStatus.monthlyBudget > 0) &&
                ((budgetStatus.currentMonthExpense - oldAmountIfExpense + enteredAmount) > budgetStatus.monthlyBudget)
            val willWarn = !willExceed && type == "expense" && (budgetStatus != null) && (budgetStatus.monthlyBudget > 0) &&
                (((budgetStatus.currentMonthExpense - oldAmountIfExpense + enteredAmount) / budgetStatus.monthlyBudget) >= 0.8)

            if (willExceed) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ExpenseRed.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
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
                            text = "বাজেট সতর্কতা: এই খরচ যোগ করলে আপনার মাসিক বাজেট (${FormatUtils.formatMoney(budgetStatus!!.monthlyBudget, currencySymbol)}) অতিক্রম করে যাবে!",
                            style = MaterialTheme.typography.bodySmall,
                            color = ExpenseRed,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else if (willWarn) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFF3E0), RoundedCornerShape(8.dp))
                        .padding(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.WarningAmber,
                            contentDescription = null,
                            tint = Color(0xFFE65100),
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "বাজেট সতর্কতা: এই খরচ যোগ করার পর মাসিক বাজেটের ৮০% এর বেশি শেষ হবে।",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFE65100),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            if (errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = ExpenseRed,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("বাতিল")
                }

                Button(
                    onClick = {
                        val amountVal = amountText.toDoubleOrNull()
                        if (amountVal == null || amountVal <= 0.0) {
                            errorMessage = "অনুগ্রহ করে সঠিক টাকার পরিমাণ লিখুন।"
                            return@Button
                        }
                        if (note.trim().isEmpty()) {
                            errorMessage = "অনুগ্রহ করে বিবরণ লিখুন।"
                            return@Button
                        }
                        onSave(
                            type,
                            amountVal,
                            note.trim(),
                            category,
                            date,
                            paymentMethod,
                            isRecurring,
                            recurringDay
                        )
                        onDismiss()
                    },
                    modifier = Modifier
                        .weight(2f)
                        .testTag("form_submit_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(
                        text = if (itemToEdit == null) "হিসাব সেভ করুন" else "পরিবর্তন সেভ করুন",
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showDatePicker) {
        val initialMillis = remember(date) {
            try {
                LocalDate.parse(date).atStartOfDay(ZoneId.of("UTC")).toInstant().toEpochMilli()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
        }
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val millis = datePickerState.selectedDateMillis
                        if (millis != null) {
                            val picked = Instant.ofEpochMilli(millis).atZone(ZoneId.of("UTC")).toLocalDate()
                            date = picked.format(formatter)
                            if (!isRecurring) {
                                recurringDay = picked.dayOfMonth
                            }
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("ঠিক আছে")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("বাতিল")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
