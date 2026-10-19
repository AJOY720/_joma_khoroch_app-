package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "income" or "expense"
    val amount: Double,
    val note: String,
    val category: String,
    val date: String, // "YYYY-MM-DD"
    val paymentMethod: String = "নগদ",
    val isRecurring: Boolean = false, // Toggle for recurring monthly entries (e.g. rent, bills)
    val recurringDayOfMonth: Int = 1, // Specific day of month (1 - 31)
    val lastGeneratedMonth: String = "", // e.g. "2026-10" to avoid duplicate generation
    val createdAt: Long = System.currentTimeMillis()
)
