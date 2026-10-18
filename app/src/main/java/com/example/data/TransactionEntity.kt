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
    val paymentMethod: String = "নগদ", // e.g. "নগদ", "ব্যাংক", "বিকাশ", "নগদ"
    val createdAt: Long = System.currentTimeMillis()
)
