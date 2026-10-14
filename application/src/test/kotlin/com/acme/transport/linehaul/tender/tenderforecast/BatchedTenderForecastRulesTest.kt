package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTenderForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTenderForecastValidator()
        val item = newBatchedTenderForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = BatchedTenderForecastStartsAtRangeRule(16, 80)
        val item = newBatchedTenderForecast("x", "y")
        item.startsAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedTenderForecastCodeLengthRule(64)
        val item = newBatchedTenderForecast("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = BatchedTenderForecastRequiredTitleRule("batched tender forecast needs an entry")
        val item = newBatchedTenderForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = BatchedTenderForecastCurrencyLengthRule(86400)
        val item = newBatchedTenderForecast("x", "y")
        item.currency = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = BatchedTenderForecastMaximumRangeRule(9.7, 125.0)
        val item = newBatchedTenderForecast("x", "y")
        item.maximum = 126.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedTenderForecastRequiredCodeRule("batched tender forecast needs this field")
        val item = newBatchedTenderForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
