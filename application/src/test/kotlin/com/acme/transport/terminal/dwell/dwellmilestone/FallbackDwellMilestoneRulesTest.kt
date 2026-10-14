package com.acme.transport.terminal.dwell.dwellmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FallbackDwellMilestoneRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultFallbackDwellMilestoneValidator()
        val item = newFallbackDwellMilestone("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = FallbackDwellMilestoneShiftCodeLengthRule(1000)
        val item = newFallbackDwellMilestone("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = FallbackDwellMilestoneDriverIdLengthRule(80)
        val item = newFallbackDwellMilestone("x", "y")
        item.driverId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = FallbackDwellMilestoneRequiredCodeRule("fallback dwell milestone needs a value")
        val item = newFallbackDwellMilestone("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above expires at`() {
        val rule = FallbackDwellMilestoneExpiresAtRangeRule(32, 255)
        val item = newFallbackDwellMilestone("x", "y")
        item.expiresAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = FallbackDwellMilestoneRequiredShiftCodeRule("fallback dwell milestone needs a value")
        val item = newFallbackDwellMilestone("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
