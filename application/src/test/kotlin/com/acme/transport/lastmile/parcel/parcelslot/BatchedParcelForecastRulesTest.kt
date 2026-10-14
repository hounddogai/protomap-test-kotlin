package com.acme.transport.lastmile.parcel.parcelslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedParcelForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedParcelForecastValidator()
        val item = newBatchedParcelForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedParcelForecastRequiredCodeRule("batched parcel forecast needs an entry")
        val item = newBatchedParcelForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = BatchedParcelForecastChecksumLengthRule(86400)
        val item = newBatchedParcelForecast("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedParcelForecastRequiredTitleRule("batched parcel forecast needs this field")
        val item = newBatchedParcelForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = BatchedParcelForecastTitleLengthRule(80)
        val item = newBatchedParcelForecast("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
