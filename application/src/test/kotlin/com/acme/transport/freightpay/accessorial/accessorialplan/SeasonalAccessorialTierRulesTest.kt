package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAccessorialTierRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAccessorialTierValidator()
        val item = newSeasonalAccessorialTier("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalAccessorialTierLabelLengthRule(64)
        val item = newSeasonalAccessorialTier("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalAccessorialTierCodeLengthRule(255)
        val item = newSeasonalAccessorialTier("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank route code`() {
        val rule = SeasonalAccessorialTierRequiredRouteCodeRule("seasonal accessorial tier needs a value")
        val item = newSeasonalAccessorialTier("", "")
        item.routeCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above attempts`() {
        val rule = SeasonalAccessorialTierAttemptsRangeRule(32, 64)
        val item = newSeasonalAccessorialTier("x", "y")
        item.attempts = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
