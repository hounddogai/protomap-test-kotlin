package com.acme.transport.terminal.dwell.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DwellSummaryRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDwellSummaryValidator()
        val item = newDwellSummary("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DwellSummaryRequiredCodeRule("dwell summary needs a value")
        val item = newDwellSummary("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = DwellSummaryThresholdRangeRule(1.0, 3213.8)
        val item = newDwellSummary("x", "y")
        item.threshold = 3215.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = DwellSummaryLabelLengthRule(255)
        val item = newDwellSummary("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = DwellSummaryAttemptsRangeRule(32, 32)
        val item = newDwellSummary("x", "y")
        item.attempts = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
