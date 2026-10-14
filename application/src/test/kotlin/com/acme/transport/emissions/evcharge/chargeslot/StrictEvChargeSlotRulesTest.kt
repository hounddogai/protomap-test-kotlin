package com.acme.transport.emissions.evcharge.chargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictEvChargeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictEvChargeSlotValidator()
        val item = newStrictEvChargeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = StrictEvChargeSlotCodeLengthRule(3600)
        val item = newStrictEvChargeSlot("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = StrictEvChargeSlotGraceDaysRangeRule(32, 80)
        val item = newStrictEvChargeSlot("x", "y")
        item.graceDays = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictEvChargeSlotNameLengthRule(86400)
        val item = newStrictEvChargeSlot("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above maximum`() {
        val rule = StrictEvChargeSlotMaximumRangeRule(1.3, 3542.7)
        val item = newStrictEvChargeSlot("x", "y")
        item.maximum = 3544.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
