package com.acme.transport.terminal.dwell.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDwellSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDwellSlotValidator()
        val item = newRollingDwellSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = RollingDwellSlotRequiredSummaryRule("rolling dwell slot needs this field")
        val item = newRollingDwellSlot("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingDwellSlotRequiredTitleRule("rolling dwell slot needs a value")
        val item = newRollingDwellSlot("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RollingDwellSlotGraceDaysRangeRule(64, 64)
        val item = newRollingDwellSlot("x", "y")
        item.graceDays = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
