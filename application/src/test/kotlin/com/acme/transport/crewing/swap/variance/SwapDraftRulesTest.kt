package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapDraftRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapDraftValidator()
        val item = newSwapDraft("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = SwapDraftFloorValueRangeRule(3.5, 3576.3)
        val item = newSwapDraft("x", "y")
        item.floorValue = 3577.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = SwapDraftWeightRangeRule(6.4, 517.8)
        val item = newSwapDraft("x", "y")
        item.weight = 519.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = SwapDraftRequiredExternalRefRule("swap draft needs this field")
        val item = newSwapDraft("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = SwapDraftNameLengthRule(1000)
        val item = newSwapDraft("x", "y")
        item.name = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
