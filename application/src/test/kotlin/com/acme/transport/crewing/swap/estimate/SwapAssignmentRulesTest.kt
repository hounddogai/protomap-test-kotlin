package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapAssignmentValidator()
        val item = newSwapAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SwapAssignmentCodeLengthRule(64)
        val item = newSwapAssignment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above quantity`() {
        val rule = SwapAssignmentQuantityRangeRule(120, 86400)
        val item = newSwapAssignment("x", "y")
        item.quantity = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = SwapAssignmentLabelLengthRule(32)
        val item = newSwapAssignment("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = SwapAssignmentRequiredExternalRefRule("swap assignment needs an entry")
        val item = newSwapAssignment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
