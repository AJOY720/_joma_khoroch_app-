package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.TransactionEntity
import com.example.ui.components.AddEditTransactionSheet
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.PinLockScreen
import com.example.ui.screens.ReportScreen
import com.example.ui.screens.SettingsDialog
import com.example.ui.screens.TransactionsListScreen
import com.example.ui.theme.JomaKhorochTheme
import com.example.viewmodel.MainViewModel

enum class AppTab(val title: String) {
    HOME("হোম"),
    REPORT("রিপোর্ট"),
    LIST("খাতা")
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            JomaKhorochTheme {
                MainAppScreen(viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: MainViewModel) {
    val isAppLocked by viewModel.isAppLocked.collectAsStateWithLifecycle()
    val isPinConfigured by viewModel.isPinConfigured.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(AppTab.HOME) }
    var showAddSheet by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<TransactionEntity?>(null) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var defaultTypeForAdd by remember { mutableStateOf("expense") }
    var showSaveSuccessDialog by remember { mutableStateOf(false) }
    var saveSuccessTitle by remember { mutableStateOf("হিসাব সফলভাবে সেভ হয়েছে!") }
    var saveSuccessDetails by remember { mutableStateOf("") }
    val context = androidx.compose.ui.platform.LocalContext.current

    BackHandler(enabled = currentTab != AppTab.HOME && !isAppLocked) {
        currentTab = AppTab.HOME
    }

    if (isAppLocked) {
        PinLockScreen(viewModel = viewModel)
        return
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "জমা-খরচ",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                },
                actions = {
                    if (isPinConfigured) {
                        IconButton(
                            onClick = { viewModel.lockNow() },
                            modifier = Modifier.testTag("app_bar_lock_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "লক করুন",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("app_bar_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "সেটিংস",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == AppTab.HOME,
                    onClick = { currentTab = AppTab.HOME },
                    icon = { Icon(imageVector = Icons.Default.Home, contentDescription = "হোম") },
                    label = { Text("হোম") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_home")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.REPORT,
                    onClick = { currentTab = AppTab.REPORT },
                    icon = { Icon(imageVector = Icons.Default.Assessment, contentDescription = "রিপোর্ট") },
                    label = { Text("রিপোর্ট") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_report")
                )

                NavigationBarItem(
                    selected = currentTab == AppTab.LIST,
                    onClick = { currentTab = AppTab.LIST },
                    icon = { Icon(imageVector = Icons.Default.ListAlt, contentDescription = "খাতা") },
                    label = { Text("হিসাবের তালিকা") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("tab_list")
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    defaultTypeForAdd = "expense"
                    showAddSheet = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("fab_add_transaction")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "নতুন হিসাব যোগ করুন",
                    modifier = Modifier.size(26.dp)
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentTab) {
                AppTab.HOME -> {
                    HomeScreen(
                        viewModel = viewModel,
                        onAddNew = { type ->
                            itemToEdit = null
                            defaultTypeForAdd = type
                            showAddSheet = true
                        },
                        onEditTransaction = { item ->
                            itemToEdit = item
                            showAddSheet = true
                        },
                        onNavigateToReport = { currentTab = AppTab.REPORT },
                        onNavigateToList = { currentTab = AppTab.LIST }
                    )
                }
                AppTab.REPORT -> {
                    ReportScreen(
                        viewModel = viewModel,
                        onEditTransaction = { item ->
                            itemToEdit = item
                            showAddSheet = true
                        }
                    )
                }
                AppTab.LIST -> {
                    TransactionsListScreen(
                        viewModel = viewModel,
                        onEditTransaction = { item ->
                            itemToEdit = item
                            showAddSheet = true
                        }
                    )
                }
            }
        }
    }

    if (showAddSheet) {
        val currencySymbol by viewModel.currencySymbol.collectAsStateWithLifecycle()
        val budgetStatus by viewModel.budgetStatus.collectAsStateWithLifecycle()
        val initialItem = itemToEdit ?: TransactionEntity(
            type = defaultTypeForAdd,
            amount = 0.0,
            note = "",
            category = if (defaultTypeForAdd == "income") "বেতন" else "বাজার",
            date = com.example.util.FormatUtils.todayDateString()
        )

        AddEditTransactionSheet(
            itemToEdit = if (itemToEdit == null) initialItem else itemToEdit,
            currencySymbol = currencySymbol,
            budgetStatus = budgetStatus,
            onDismiss = {
                showAddSheet = false
                itemToEdit = null
            },
            onSave = { type, amount, note, category, date, paymentMethod, isRecurring, recurringDay ->
                val isEdit = itemToEdit != null
                if (isEdit) {
                    viewModel.updateTransaction(
                        id = itemToEdit!!.id,
                        type = type,
                        amount = amount,
                        note = note,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        isRecurring = isRecurring,
                        recurringDayOfMonth = recurringDay
                    )
                    saveSuccessTitle = "হিসাব সফলভাবে আপডেট হয়েছে!"
                } else {
                    viewModel.addTransaction(
                        type = type,
                        amount = amount,
                        note = note,
                        category = category,
                        date = date,
                        paymentMethod = paymentMethod,
                        isRecurring = isRecurring,
                        recurringDayOfMonth = recurringDay
                    )
                    saveSuccessTitle = "হিসাব সফলভাবে সেভ হয়েছে!"
                }
                val typeBn = if (type == "income") "জমা" else "খরচ"
                val moneyFormatted = com.example.util.FormatUtils.formatMoney(amount, currencySymbol)
                saveSuccessDetails = "$typeBn: $moneyFormatted • $note ($category) • $paymentMethod"
                showSaveSuccessDialog = true
                android.widget.Toast.makeText(context, "✅ $saveSuccessTitle", android.widget.Toast.LENGTH_SHORT).show()
                showAddSheet = false
                itemToEdit = null
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            viewModel = viewModel,
            onDismiss = { showSettingsDialog = false }
        )
    }

    if (showSaveSuccessDialog) {
        androidx.compose.runtime.LaunchedEffect(Unit) {
            kotlinx.coroutines.delay(2500)
            showSaveSuccessDialog = false
        }

        AlertDialog(
            onDismissRequest = { showSaveSuccessDialog = false },
            icon = {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color(0xFFE8F5E9),
                            shape = CircleShape
                        ),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "সফল",
                        tint = androidx.compose.ui.graphics.Color(0xFF2E7D32),
                        modifier = Modifier.size(36.dp)
                    )
                }
            },
            title = {
                Text(
                    text = saveSuccessTitle,
                    fontWeight = FontWeight.Bold,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = saveSuccessDetails,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "আপনার হিসাবটি নিরাপদে সংরক্ষণ করা হয়েছে।",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSaveSuccessDialog = false },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_success_dismiss_button"),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("ঠিক আছে", fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
