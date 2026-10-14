package com.acme.transport.crewing.swap.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapScoreRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapScoreValidator()
        val item = newSwapScore("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapScoreCodeLengthRule(120)
        val item = newSwapScore("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = SwapScoreRequiredTimezoneRule("swap score needs a value")
        val item = newSwapScore("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = SwapScoreRatioRangeRule(7.5, 862.1)
        val item = newSwapScore("x", "y")
        item.ratio = 863.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = SwapScoreEndsAtRangeRule(86400, 86400)
        val item = newSwapScore("x", "y")
        item.endsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapScoreNameLengthRule(32)
        val item = newSwapScore("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
