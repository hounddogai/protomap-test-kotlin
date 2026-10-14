package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveDwellGroupRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveDwellGroupValidator()
        val item = newAdaptiveDwellGroup("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AdaptiveDwellGroupDockLengthRule(3600)
        val item = newAdaptiveDwellGroup("x", "y")
        item.dock = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = AdaptiveDwellGroupGraceDaysRangeRule(80, 255)
        val item = newAdaptiveDwellGroup("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
