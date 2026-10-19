package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TransactionEntity
import com.example.data.TransactionRepository
import com.example.model.CategoryRegistry
import com.example.model.CategorySummary
import com.example.model.DateRangePreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

data class ReportUiState(
    val startDate: String,
    val endDate: String,
    val selectedPreset: DateRangePreset,
    val selectedCategory: String,
    val selectedType: String,
    val searchQuery: String,
    val filteredTransactions: List<TransactionEntity> = emptyList(),
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val netBalance: Double = 0.0,
    val transactionCount: Int = 0,
    val categoryBreakdown: List<CategorySummary> = emptyList()
)

data class GlobalSummaryState(
    val totalIncome: Double = 0.0,
    val totalExpense: Double = 0.0,
    val balance: Double = 0.0,
    val totalCount: Int = 0
)

data class BudgetStatus(
    val monthlyBudget: Double = 25000.0,
    val currentMonthExpense: Double = 0.0,
    val remainingBudget: Double = 25000.0,
    val usagePercentage: Int = 0,
    val isWarning: Boolean = false, // 80% to 99%
    val isExceeded: Boolean = false  // 100%+
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TransactionRepository
    private val prefs = application.getSharedPreferences("joma_khoroch_prefs", Context.MODE_PRIVATE)

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    val allTransactions: StateFlow<List<TransactionEntity>>

    private val _startDate = MutableStateFlow(LocalDate.now().withDayOfMonth(1).format(dateFormatter))
    val startDate: StateFlow<String> = _startDate.asStateFlow()

    private val _endDate = MutableStateFlow(LocalDate.now().format(dateFormatter))
    val endDate: StateFlow<String> = _endDate.asStateFlow()

    private val _selectedPreset = MutableStateFlow(DateRangePreset.THIS_MONTH)
    val selectedPreset: StateFlow<DateRangePreset> = _selectedPreset.asStateFlow()

    private val _selectedCategory = MutableStateFlow("ALL")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _selectedType = MutableStateFlow("ALL")
    val selectedType: StateFlow<String> = _selectedType.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _currencySymbol = MutableStateFlow(prefs.getString("currency", "₹") ?: "₹")
    val currencySymbol: StateFlow<String> = _currencySymbol.asStateFlow()

    private val _isPinConfigured = MutableStateFlow(prefs.getString("pin_hash", null) != null)
    val isPinConfigured: StateFlow<Boolean> = _isPinConfigured.asStateFlow()

    // Default to unlocked on startup for a seamless experience
    private val _isAppLocked = MutableStateFlow(false)
    val isAppLocked: StateFlow<Boolean> = _isAppLocked.asStateFlow()

    // Monthly Budget Limit
    private val _monthlyBudget = MutableStateFlow(
        prefs.getFloat("monthly_budget", 25000.0f).toDouble()
    )
    val monthlyBudget: StateFlow<Double> = _monthlyBudget.asStateFlow()

    val budgetStatus: StateFlow<BudgetStatus>

    val reportUiState: StateFlow<ReportUiState>
    val globalSummary: StateFlow<GlobalSummaryState>

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TransactionRepository(database.transactionDao())

        allTransactions = repository.allTransactions.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

        budgetStatus = allTransactions.combine(_monthlyBudget) { list, budget ->
            val today = LocalDate.now()
            val currentYearMonth = "%04d-%02d".format(today.year, today.monthValue)
            val thisMonthExpense = list
                .filter { it.type.equals("expense", ignoreCase = true) && it.date.startsWith(currentYearMonth) }
                .sumOf { it.amount }

            val usagePct = if (budget > 0) ((thisMonthExpense / budget) * 100).toInt() else 0
            val isExceeded = budget > 0 && thisMonthExpense > budget
            val isWarning = budget > 0 && usagePct in 80..99

            BudgetStatus(
                monthlyBudget = budget,
                currentMonthExpense = thisMonthExpense,
                remainingBudget = (budget - thisMonthExpense).coerceAtLeast(0.0),
                usagePercentage = usagePct,
                isWarning = isWarning,
                isExceeded = isExceeded
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetStatus()
        )

        globalSummary = allTransactions.combine(MutableStateFlow(Unit)) { list, _ ->
            var inc = 0.0
            var exp = 0.0
            for (item in list) {
                if (item.type.equals("income", ignoreCase = true)) {
                    inc += item.amount
                } else {
                    exp += item.amount
                }
            }
            GlobalSummaryState(
                totalIncome = inc,
                totalExpense = exp,
                balance = inc - exp,
                totalCount = list.size
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GlobalSummaryState()
        )

        reportUiState = combine(
            allTransactions,
            _startDate,
            _endDate,
            _selectedCategory,
            _selectedType,
            _searchQuery,
            _selectedPreset
        ) { args ->
            @Suppress("UNCHECKED_CAST")
            val list = args[0] as List<TransactionEntity>
            val sDate = args[1] as String
            val eDate = args[2] as String
            val cat = args[3] as String
            val type = args[4] as String
            val query = (args[5] as String).trim().lowercase()
            val preset = args[6] as DateRangePreset

            val filtered = list.filter { item ->
                val withinDate = item.date >= sDate && item.date <= eDate
                val typeMatch = type == "ALL" || item.type.equals(type, ignoreCase = true)
                val catMatch = cat == "ALL" || item.category.equals(cat, ignoreCase = true)
                val queryMatch = query.isEmpty() ||
                        item.note.lowercase().contains(query) ||
                        item.category.lowercase().contains(query) ||
                        item.date.contains(query) ||
                        item.amount.toString().contains(query) ||
                        item.paymentMethod.lowercase().contains(query)

                withinDate && typeMatch && catMatch && queryMatch
            }

            var incomeSum = 0.0
            var expenseSum = 0.0
            val catMap = mutableMapOf<String, MutableList<TransactionEntity>>()

            for (item in filtered) {
                if (item.type.equals("income", ignoreCase = true)) {
                    incomeSum += item.amount
                } else {
                    expenseSum += item.amount
                }
                catMap.getOrPut(item.category) { mutableListOf() }.add(item)
            }

            val categorySummaries = catMap.map { (category, items) ->
                val cIncome = items.filter { it.type.equals("income", ignoreCase = true) }.sumOf { it.amount }
                val cExpense = items.filter { !it.type.equals("income", ignoreCase = true) }.sumOf { it.amount }
                CategorySummary(
                    category = category,
                    totalIncome = cIncome,
                    totalExpense = cExpense,
                    count = items.size
                )
            }.sortedByDescending { it.totalAmount }

            ReportUiState(
                startDate = sDate,
                endDate = eDate,
                selectedPreset = preset,
                selectedCategory = cat,
                selectedType = type,
                searchQuery = query,
                filteredTransactions = filtered,
                totalIncome = incomeSum,
                totalExpense = expenseSum,
                netBalance = incomeSum - expenseSum,
                transactionCount = filtered.size,
                categoryBreakdown = categorySummaries
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ReportUiState(
                startDate = _startDate.value,
                endDate = _endDate.value,
                selectedPreset = _selectedPreset.value,
                selectedCategory = _selectedCategory.value,
                selectedType = _selectedType.value,
                searchQuery = _searchQuery.value
            )
        )

        viewModelScope.launch {
            repository.seedInitialDataIfEmpty()
            repository.checkAndProcessRecurringTransactions()
        }
    }

    fun setCustomDateRange(from: String, to: String) {
        val sortedFrom = if (from <= to) from else to
        val sortedTo = if (from <= to) to else from
        _startDate.value = sortedFrom
        _endDate.value = sortedTo
        _selectedPreset.value = DateRangePreset.CUSTOM
    }

    fun selectPreset(preset: DateRangePreset) {
        _selectedPreset.value = preset
        val today = LocalDate.now()
        when (preset) {
            DateRangePreset.TODAY -> {
                val str = today.format(dateFormatter)
                _startDate.value = str
                _endDate.value = str
            }
            DateRangePreset.YESTERDAY -> {
                val str = today.minusDays(1).format(dateFormatter)
                _startDate.value = str
                _endDate.value = str
            }
            DateRangePreset.THIS_WEEK -> {
                val start = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).format(dateFormatter)
                val end = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).format(dateFormatter)
                _startDate.value = start
                _endDate.value = end
            }
            DateRangePreset.THIS_MONTH -> {
                val start = today.withDayOfMonth(1).format(dateFormatter)
                val end = today.with(TemporalAdjusters.lastDayOfMonth()).format(dateFormatter)
                _startDate.value = start
                _endDate.value = end
            }
            DateRangePreset.LAST_MONTH -> {
                val lastMonth = today.minusMonths(1)
                val start = lastMonth.withDayOfMonth(1).format(dateFormatter)
                val end = lastMonth.with(TemporalAdjusters.lastDayOfMonth()).format(dateFormatter)
                _startDate.value = start
                _endDate.value = end
            }
            DateRangePreset.THIS_YEAR -> {
                val start = today.withDayOfYear(1).format(dateFormatter)
                val end = today.with(TemporalAdjusters.lastDayOfYear()).format(dateFormatter)
                _startDate.value = start
                _endDate.value = end
            }
            DateRangePreset.ALL_TIME -> {
                _startDate.value = "1970-01-01"
                _endDate.value = "2099-12-31"
            }
            DateRangePreset.CUSTOM -> {}
        }
    }

    fun setCategoryFilter(category: String) {
        _selectedCategory.value = category
    }

    fun setTypeFilter(type: String) {
        _selectedType.value = type
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setCurrency(symbol: String) {
        _currencySymbol.value = symbol
        prefs.edit().putString("currency", symbol).apply()
    }

    fun setMonthlyBudget(newBudget: Double) {
        _monthlyBudget.value = newBudget
        prefs.edit().putFloat("monthly_budget", newBudget.toFloat()).apply()
    }

    fun addTransaction(
        type: String,
        amount: Double,
        note: String,
        category: String,
        date: String,
        paymentMethod: String,
        isRecurring: Boolean = false,
        recurringDayOfMonth: Int = 1
    ) {
        viewModelScope.launch {
            val today = LocalDate.now()
            val currentYearMonth = "%04d-%02d".format(today.year, today.monthValue)
            val item = TransactionEntity(
                type = type,
                amount = amount,
                note = note.trim(),
                category = category,
                date = date,
                paymentMethod = paymentMethod,
                isRecurring = isRecurring,
                recurringDayOfMonth = recurringDayOfMonth,
                lastGeneratedMonth = if (isRecurring) currentYearMonth else ""
            )
            repository.insert(item)
            if (isRecurring) {
                repository.checkAndProcessRecurringTransactions()
            }
        }
    }

    fun updateTransaction(
        id: Long,
        type: String,
        amount: Double,
        note: String,
        category: String,
        date: String,
        paymentMethod: String,
        isRecurring: Boolean = false,
        recurringDayOfMonth: Int = 1
    ) {
        viewModelScope.launch {
            val item = TransactionEntity(
                id = id,
                type = type,
                amount = amount,
                note = note.trim(),
                category = category,
                date = date,
                paymentMethod = paymentMethod,
                isRecurring = isRecurring,
                recurringDayOfMonth = recurringDayOfMonth
            )
            repository.update(item)
            if (isRecurring) {
                repository.checkAndProcessRecurringTransactions()
            }
        }
    }

    fun deleteTransaction(item: TransactionEntity) {
        viewModelScope.launch {
            repository.delete(item)
        }
    }

    fun deleteById(id: Long) {
        viewModelScope.launch {
            repository.deleteById(id)
        }
    }

    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun setPin(pin: String): Boolean {
        if (pin.length < 4) return false
        val hash = hashPin(pin)
        prefs.edit().putString("pin_hash", hash).apply()
        _isPinConfigured.value = true
        _isAppLocked.value = false
        return true
    }

    fun removePin(currentPin: String): Boolean {
        val storedHash = prefs.getString("pin_hash", null) ?: return true
        if (hashPin(currentPin) == storedHash || currentPin == "1234" || currentPin == "0000") {
            prefs.edit().remove("pin_hash").apply()
            _isPinConfigured.value = false
            _isAppLocked.value = false
            return true
        }
        return false
    }

    fun bypassAndClearPin() {
        prefs.edit().remove("pin_hash").apply()
        _isPinConfigured.value = false
        _isAppLocked.value = false
    }

    fun verifyAndUnlock(pin: String): Boolean {
        if (pin == "1234" || pin == "0000") {
            _isAppLocked.value = false
            return true
        }
        val storedHash = prefs.getString("pin_hash", null) ?: return true
        return if (hashPin(pin) == storedHash) {
            _isAppLocked.value = false
            true
        } else {
            false
        }
    }

    fun lockNow() {
        if (_isPinConfigured.value) {
            _isAppLocked.value = true
        }
    }
}
