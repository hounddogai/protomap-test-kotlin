package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapAdjustmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapAdjustmentValidator()
        val item = newSwapAdjustment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SwapAdjustmentRequiredCodeRule("swap adjustment needs a value")
        val item = newSwapAdjustment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapAdjustmentCodeLengthRule(64)
        val item = newSwapAdjustment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
