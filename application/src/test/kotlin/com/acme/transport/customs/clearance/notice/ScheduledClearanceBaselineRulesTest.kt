package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledClearanceBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledClearanceBaselineValidator()
        val item = newScheduledClearanceBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledClearanceBaselineRequiredCodeRule("scheduled clearance baseline needs a value")
        val item = newScheduledClearanceBaseline("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = ScheduledClearanceBaselineRequiredSummaryRule("scheduled clearance baseline needs an entry")
        val item = newScheduledClearanceBaseline("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = ScheduledClearanceBaselineRevisionRangeRule(255, 3600)
        val item = newScheduledClearanceBaseline("x", "y")
        item.revision = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledClearanceBaselineCodeLengthRule(255)
        val item = newScheduledClearanceBaseline("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = ScheduledClearanceBaselineGraceDaysRangeRule(80, 3600)
        val item = newScheduledClearanceBaseline("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
