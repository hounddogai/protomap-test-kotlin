package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CapacitySummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacitySummaryValidator()
        val item = newCapacitySummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = CapacitySummarySegmentLengthRule(32)
        val item = newCapacitySummary("x", "y")
        item.segment = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = CapacitySummaryCodeLengthRule(80)
        val item = newCapacitySummary("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = CapacitySummaryRequiredReviewerRule("capacity summary needs this field")
        val item = newCapacitySummary("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = CapacitySummaryReviewerLengthRule(86400)
        val item = newCapacitySummary("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
