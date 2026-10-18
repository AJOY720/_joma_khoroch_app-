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
  fun `test csv generation contains headers and records`() {
    val items = listOf(
      TransactionEntity(
        id = 1,
        type = "income",
        amount = 15000.0,
        note = "মাসিক বেতন",
        category = "বেতন",
        date = "2026-10-01",
        paymentMethod = "ব্যাংক"
      )
    )
    val csv = FormatUtils.generateCsv(items, "₹")
    assertTrue(csv.contains("তারিখ,ধরন,বিবরণ,ক্যাটাগরি"))
    assertTrue(csv.contains("মাসিক বেতন"))
    assertTrue(csv.contains("15000.0"))
  }
}
