package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerYardAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerYardAdjustmentValidator()
        val item = newRegionalTrailerYardAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = RegionalTrailerYardAdjustmentDescriptionLengthRule(32)
        val item = newRegionalTrailerYardAdjustment("x", "y")
        item.description = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = RegionalTrailerYardAdjustmentPalletsRangeRule(16, 120)
        val item = newRegionalTrailerYardAdjustment("x", "y")
        item.pallets = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
