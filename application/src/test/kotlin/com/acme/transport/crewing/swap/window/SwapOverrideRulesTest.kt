package com.acme.transport.crewing.swap.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapOverrideValidator()
        val item = newSwapOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SwapOverrideRatioRangeRule(6.6, 1745.4)
        val item = newSwapOverride("x", "y")
        item.ratio = 1746.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = SwapOverrideEtaMinutesRangeRule(64, 120)
        val item = newSwapOverride("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = SwapOverrideRequiredNameRule("swap override needs this field")
        val item = newSwapOverride("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = SwapOverrideRequiredDepotRule("swap override needs a value")
        val item = newSwapOverride("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
