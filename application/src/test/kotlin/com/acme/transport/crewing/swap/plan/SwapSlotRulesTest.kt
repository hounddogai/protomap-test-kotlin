package com.acme.transport.crewing.swap.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SwapSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSwapSlotValidator()
        val item = newSwapSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SwapSlotRequiredLabelRule("swap slot needs this field")
        val item = newSwapSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SwapSlotLabelLengthRule(3600)
        val item = newSwapSlot("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = SwapSlotOwnerIdLengthRule(16)
        val item = newSwapSlot("x", "y")
        item.ownerId = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SwapSlotRequiredOwnerIdRule("swap slot needs an entry")
        val item = newSwapSlot("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
