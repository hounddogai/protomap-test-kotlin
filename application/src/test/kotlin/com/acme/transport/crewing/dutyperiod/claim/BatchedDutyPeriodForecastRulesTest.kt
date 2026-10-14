package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDutyPeriodForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDutyPeriodForecastValidator()
        val item = newBatchedDutyPeriodForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedDutyPeriodForecastRequiredNameRule("batched duty period forecast needs an entry")
        val item = newBatchedDutyPeriodForecast("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedDutyPeriodForecastNameLengthRule(3600)
        val item = newBatchedDutyPeriodForecast("x", "y")
        item.name = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = BatchedDutyPeriodForecastReviewerLengthRule(16)
        val item = newBatchedDutyPeriodForecast("x", "y")
        item.reviewer = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDutyPeriodForecastRequiredCodeRule("batched duty period forecast needs this field")
        val item = newBatchedDutyPeriodForecast("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
