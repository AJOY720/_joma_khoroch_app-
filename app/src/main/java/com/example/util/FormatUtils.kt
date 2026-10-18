package com.example.util

import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

object FormatUtils {

    private val numberFormat = NumberFormat.getNumberInstance(Locale("en", "IN")).apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }

    fun formatMoney(amount: Double, symbol: String = "₹"): String {
        return "$symbol${numberFormat.format(amount)}"
    }

    fun todayDateString(): String {
        return LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
    }

    fun formatDateDisplay(dateStr: String): String {
        return try {
            val date = LocalDate.parse(dateStr)
            val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", Locale("bn", "BD"))
            date.format(formatter)
        } catch (e: Exception) {
            dateStr
        }
    }

    fun generateCsv(
        transactions: List<com.example.data.TransactionEntity>,
        currencySymbol: String
    ): String {
        val sb = StringBuilder()
        // UTF-8 BOM for Excel / Google Sheets Bengali character support
        sb.append('\uFEFF')
        sb.append("তারিখ,ধরন,বিবরণ,ক্যাটাগরি,টাকার পরিমাণ ($currencySymbol),পেমেন্ট মাধ্যম\n")

        for (t in transactions) {
            val typeStr = if (t.type.equals("income", ignoreCase = true)) "জমা" else "খরচ"
            val safeNote = t.note.replace("\"", "\"\"")
            val safeCat = t.category.replace("\"", "\"\"")
            val safeMethod = t.paymentMethod.replace("\"", "\"\"")
            sb.append("\"${t.date}\",\"$typeStr\",\"$safeNote\",\"$safeCat\",${t.amount},\"$safeMethod\"\n")
        }
        return sb.toString()
    }
}
