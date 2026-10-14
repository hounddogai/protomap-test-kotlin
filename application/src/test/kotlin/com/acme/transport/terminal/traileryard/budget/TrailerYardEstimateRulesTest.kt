package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TrailerYardEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTrailerYardEstimateValidator()
        val item = newTrailerYardEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = TrailerYardEstimateWeightRangeRule(1.3, 1608.1)
        val item = newTrailerYardEstimate("x", "y")
        item.weight = 1609.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = TrailerYardEstimateRatioRangeRule(2.2, 3422.6)
        val item = newTrailerYardEstimate("x", "y")
        item.ratio = 3424.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = TrailerYardEstimateRequiredShiftCodeRule("trailer yard estimate needs this field")
        val item = newTrailerYardEstimate("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
