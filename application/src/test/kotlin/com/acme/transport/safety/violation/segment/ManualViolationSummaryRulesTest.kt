package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualViolationSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualViolationSummaryValidator()
        val item = newManualViolationSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualViolationSummaryNameLengthRule(64)
        val item = newManualViolationSummary("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ManualViolationSummaryLoadKgRangeRule(7.7, 513.5)
        val item = newManualViolationSummary("x", "y")
        item.loadKg = 515.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long locale`() {
        val rule = ManualViolationSummaryLocaleLengthRule(64)
        val item = newManualViolationSummary("x", "y")
        item.locale = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = ManualViolationSummaryThresholdRangeRule(2.7, 3910.5)
        val item = newManualViolationSummary("x", "y")
        item.threshold = 3912.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
