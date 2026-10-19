package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.TransactionEntity
import com.example.util.FormatUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app_name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("জমা-খরচ", appName)
  }

  @Test
  fun `test money formatting`() {
    val formatted = FormatUtils.formatMoney(5000.0, "₹")
    assertTrue(formatted.contains("5,000") || formatted.contains("5000"))
    assertTrue(formatted.startsWith("₹"))
  }

  @Test
  fun `test csv generation with recurring column`() {
    val items = listOf(
      TransactionEntity(
        id = 1,
        type = "expense",
        amount = 12000.0,
        note = "বাড়িভাড়া",
        category = "বাড়িভাড়া",
        date = "2026-10-01",
        paymentMethod = "ব্যাংক",
        isRecurring = true,
        recurringDayOfMonth = 1
      )
    )
    val csv = FormatUtils.generateCsv(items, "₹")
    assertTrue(csv.contains("মাসিক পুনরাবৃত্তি"))
    assertTrue(csv.contains("বাড়িভাড়া"))
    assertTrue(csv.contains("12000.0"))
  }

  @Test
  fun `test transaction with upi online payment method`() {
    val item = TransactionEntity(
      id = 2,
      type = "expense",
      amount = 850.0,
      note = "রেস্তোরাঁ ডিনার",
      category = "খাবার",
      date = "2026-10-18",
      paymentMethod = "UPI অনলাইন"
    )
    val csv = FormatUtils.generateCsv(listOf(item), "₹")
    assertTrue(csv.contains("UPI অনলাইন"))
    assertEquals("UPI অনলাইন", item.paymentMethod)
  }

  @Test
  fun `test monthly budget alert status thresholds`() {
    val monthlyBudget = 20000.0
    val safeExpense = 10000.0
    val warningExpense = 17000.0 // 85%
    val exceededExpense = 21500.0 // >100%

    // Safe
    val safeUsagePct = ((safeExpense / monthlyBudget) * 100).toInt()
    assertEquals(50, safeUsagePct)
    assertTrue(safeExpense <= monthlyBudget)
    assertTrue(safeUsagePct < 80)

    // Warning
    val warningUsagePct = ((warningExpense / monthlyBudget) * 100).toInt()
    assertEquals(85, warningUsagePct)
    assertTrue(warningUsagePct in 80..99)

    // Exceeded
    val isExceeded = exceededExpense > monthlyBudget
    assertTrue(isExceeded)
    assertEquals(1500.0, exceededExpense - monthlyBudget, 0.001)
  }
}
