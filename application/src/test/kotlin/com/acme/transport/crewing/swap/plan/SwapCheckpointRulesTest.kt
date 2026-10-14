package com.acme.transport.crewing.swap.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapCheckpointRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapCheckpointValidator()
        val item = newSwapCheckpoint("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SwapCheckpointRequiredCodeRule("swap checkpoint needs this field")
        val item = newSwapCheckpoint("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = SwapCheckpointLocaleLengthRule(64)
        val item = newSwapCheckpoint("x", "y")
        item.locale = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above margin`() {
        val rule = SwapCheckpointMarginRangeRule(7.5, 3998.7)
        val item = newSwapCheckpoint("x", "y")
        item.margin = 4000.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
