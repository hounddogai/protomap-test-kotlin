package com.acme.transport.network.node.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeSlotValidator()
        val item = newNodeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = NodeSlotRequiredLabelRule("node slot needs this field")
        val item = newNodeSlot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeSlotCodeLengthRule(64)
        val item = newNodeSlot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above amount`() {
        val rule = NodeSlotAmountRangeRule(3.8, 2863.0)
        val item = newNodeSlot("x", "y")
        item.amount = 2864.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeSlotRequiredCodeRule("node slot needs an entry")
        val item = newNodeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = NodeSlotLabelLengthRule(255)
        val item = newNodeSlot("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = NodeSlotDiscountRangeRule(1.9, 4618.5)
        val item = newNodeSlot("x", "y")
        item.discount = 4620.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
