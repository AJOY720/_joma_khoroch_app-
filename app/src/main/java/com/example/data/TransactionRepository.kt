package com.example.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class TransactionRepository(private val transactionDao: TransactionDao) {

    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactions()

    fun getTransactionsByDateRange(startDate: String, endDate: String): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsByDateRange(startDate, endDate)
    }

    suspend fun insert(transaction: TransactionEntity): Long {
        return transactionDao.insertTransaction(transaction)
    }

    suspend fun update(transaction: TransactionEntity) {
        transactionDao.updateTransaction(transaction)
    }

    suspend fun delete(transaction: TransactionEntity) {
        transactionDao.deleteTransaction(transaction)
    }

    suspend fun deleteById(id: Long) {
        transactionDao.deleteById(id)
    }

    suspend fun seedInitialDataIfEmpty() {
        if (transactionDao.getCount() == 0) {
            val today = LocalDate.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val todayStr = today.format(formatter)
            val yesterdayStr = today.minusDays(1).format(formatter)
            val dayBeforeYesterdayStr = today.minusDays(3).format(formatter)
            val lastWeekStr = today.minusDays(7).format(formatter)
            val twoWeeksAgoStr = today.minusDays(14).format(formatter)
            val firstOfMonthStr = today.withDayOfMonth(1).format(formatter)

            val samples = listOf(
                TransactionEntity(
                    type = "income",
                    amount = 45000.0,
                    note = "মাসিক বেতন",
                    category = "বেতন",
                    date = firstOfMonthStr,
                    paymentMethod = "ব্যাংক"
                ),
                TransactionEntity(
                    type = "expense",
                    amount = 12500.0,
                    note = "মাসিক বাড়িভাড়া",
                    category = "বাড়িভাড়া",
                    date = firstOfMonthStr,
                    paymentMethod = "ব্যাংক"
                ),
                TransactionEntity(
                    type = "expense",
                    amount = 3200.0,
                    note = "সাপ্তাহিক কাঁচাবাজার ও মাছ",
                    category = "বাজার",
                    date = twoWeeksAgoStr,
                    paymentMethod = "নগদ"
                ),
                TransactionEntity(
                    type = "expense",
                    amount = 1450.0,
                    note = "বিদ্যুৎ ও ওয়াইফাই ইন্টারনেট বিল",
                    category = "বিল",
                    date = lastWeekStr,
                    paymentMethod = "বিকাশ"
                ),
                TransactionEntity(
                    type = "income",
                    amount = 8500.0,
                    note = "সাইড প্রজেক্ট ও ফ্রিল্যান্সিং আয়",
                    category = "ব্যবসা",
                    date = dayBeforeYesterdayStr,
                    paymentMethod = "ব্যাংক"
                ),
                TransactionEntity(
                    type = "expense",
                    amount = 1800.0,
                    note = "ওষুধ ও ডাক্তারের পরামর্শ ফি",
                    category = "চিকিৎসা",
                    date = yesterdayStr,
                    paymentMethod = "নগদ"
                ),
                TransactionEntity(
                    type = "expense",
                    amount = 650.0,
                    note = "অফিস যাতায়াত সিএনজি/মেট্রোরেল ভাড়া",
                    category = "যাতায়াত",
                    date = todayStr,
                    paymentMethod = "নগদ"
                ),
                TransactionEntity(
                    type = "income",
                    amount = 5000.0,
                    note = "মাসিক ডিপিএস সঞ্চয় রিটার্ন",
                    category = "সঞ্চয়",
                    date = todayStr,
                    paymentMethod = "বিকাশ"
                )
            )
            transactionDao.insertAll(samples)
        }
    }
}
