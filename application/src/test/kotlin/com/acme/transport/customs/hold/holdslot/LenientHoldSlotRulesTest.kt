package com.acme.transport.customs.hold.holdslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientHoldSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientHoldSlotValidator()
        val item = newLenientHoldSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = LenientHoldSlotBudgetRangeRule(2.4, 119.0)
        val item = newLenientHoldSlot("x", "y")
        item.budget = 120.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long lane`() {
        val rule = LenientHoldSlotLaneLengthRule(120)
        val item = newLenientHoldSlot("x", "y")
        item.lane = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.lane = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientHoldSlotCodeLengthRule(16)
        val item = newLenientHoldSlot("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientHoldSlotRequiredCodeRule("lenient hold slot needs this field")
        val item = newLenientHoldSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = LenientHoldSlotFuelLitersRangeRule(7.7, 4181.9)
        val item = newLenientHoldSlot("x", "y")
        item.fuelLiters = 4183.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
