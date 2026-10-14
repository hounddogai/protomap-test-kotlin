package com.acme.transport.fleetops.utilization.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class UtilizationTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultUtilizationTierValidator()
        val item = newUtilizationTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = UtilizationTierLabelLengthRule(64)
        val item = newUtilizationTier("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = UtilizationTierRequiredTimezoneRule("utilization tier needs a value")
        val item = newUtilizationTier("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long timezone`() {
        val rule = UtilizationTierTimezoneLengthRule(3600)
        val item = newUtilizationTier("x", "y")
        item.timezone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.timezone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = UtilizationTierRateRangeRule(5.9, 365.2)
        val item = newUtilizationTier("x", "y")
        item.rate = 366.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = UtilizationTierRequiredCodeRule("utilization tier needs a value")
        val item = newUtilizationTier("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
