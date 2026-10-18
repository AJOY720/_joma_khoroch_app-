package com.example.model

enum class TransactionType(val key: String, val bnLabel: String, val enLabel: String) {
    INCOME("income", "জমা", "Deposit / Income"),
    EXPENSE("expense", "খরচ", "Expense");

    companion object {
        fun fromKey(key: String): TransactionType {
            return if (key.equals("expense", ignoreCase = true)) EXPENSE else INCOME
        }
    }
}

data class CategoryItem(
    val id: String,
    val nameBn: String,
    val defaultType: TransactionType? = null
)

object CategoryRegistry {
    val defaultCategories = listOf(
        CategoryItem("salary", "বেতন", TransactionType.INCOME),
        CategoryItem("business", "ব্যবসা", TransactionType.INCOME),
        CategoryItem("market", "বাজার", TransactionType.EXPENSE),
        CategoryItem("rent", "বাড়িভাড়া", TransactionType.EXPENSE),
        CategoryItem("transport", "যাতায়াত", TransactionType.EXPENSE),
        CategoryItem("bill", "বিল", TransactionType.EXPENSE),
        CategoryItem("medical", "চিকিৎসা", TransactionType.EXPENSE),
        CategoryItem("education", "শিক্ষা", TransactionType.EXPENSE),
        CategoryItem("savings", "সঞ্চয়"),
        CategoryItem("deposit_savings", "জমা/সঞ্চয়", TransactionType.INCOME),
        CategoryItem("loan", "ঋণ"),
        CategoryItem("gift", "উপহার"),
        CategoryItem("entertainment", "বিনোদন", TransactionType.EXPENSE),
        CategoryItem("other", "অন্যান্য")
    )

    val categoryNames: List<String> = defaultCategories.map { it.nameBn }
}

enum class DateRangePreset(val bnLabel: String) {
    TODAY("আজ"),
    YESTERDAY("গতকাল"),
    THIS_WEEK("এই সপ্তাহ"),
    THIS_MONTH("এই মাস"),
    LAST_MONTH("গত মাস"),
    THIS_YEAR("এই বছর"),
    ALL_TIME("সব সময়"),
    CUSTOM("তারিখ পছন্দ করুন")
}

data class CategorySummary(
    val category: String,
    val totalIncome: Double,
    val totalExpense: Double,
    val count: Int
) {
    val totalAmount: Double get() = totalIncome + totalExpense
}
