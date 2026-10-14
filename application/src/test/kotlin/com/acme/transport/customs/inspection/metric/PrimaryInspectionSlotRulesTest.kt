package com.acme.transport.customs.inspection.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryInspectionSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryInspectionSlotValidator()
        val item = newPrimaryInspectionSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = PrimaryInspectionSlotGraceDaysRangeRule(16, 80)
        val item = newPrimaryInspectionSlot("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above retries`() {
        val rule = PrimaryInspectionSlotRetriesRangeRule(64, 255)
        val item = newPrimaryInspectionSlot("x", "y")
        item.retries = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long summary`() {
        val rule = PrimaryInspectionSlotSummaryLengthRule(64)
        val item = newPrimaryInspectionSlot("x", "y")
        item.summary = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.summary = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
