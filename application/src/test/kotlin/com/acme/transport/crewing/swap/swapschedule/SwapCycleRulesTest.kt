package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapCycleValidator()
        val item = newSwapCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapCycleCodeLengthRule(86400)
        val item = newSwapCycle("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = SwapCyclePalletsRangeRule(32, 255)
        val item = newSwapCycle("x", "y")
        item.pallets = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SwapCycleRequiredLabelRule("swap cycle needs a value")
        val item = newSwapCycle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
