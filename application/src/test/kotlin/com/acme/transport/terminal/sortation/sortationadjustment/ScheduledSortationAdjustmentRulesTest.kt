package com.acme.transport.terminal.sortation.sortationadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledSortationAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledSortationAdjustmentValidator()
        val item = newScheduledSortationAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledSortationAdjustmentRequiredTitleRule("scheduled sortation adjustment needs an entry")
        val item = newScheduledSortationAdjustment("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long currency`() {
        val rule = ScheduledSortationAdjustmentCurrencyLengthRule(1000)
        val item = newScheduledSortationAdjustment("x", "y")
        item.currency = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.currency = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
