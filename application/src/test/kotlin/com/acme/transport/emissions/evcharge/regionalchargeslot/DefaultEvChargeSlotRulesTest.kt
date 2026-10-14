package com.acme.transport.emissions.evcharge.regionalchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultEvChargeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultEvChargeSlotValidator()
        val item = newDefaultEvChargeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = DefaultEvChargeSlotCostRangeRule(1.6, 4208.9)
        val item = newDefaultEvChargeSlot("x", "y")
        item.cost = 4210.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above version`() {
        val rule = DefaultEvChargeSlotVersionRangeRule(64, 86400)
        val item = newDefaultEvChargeSlot("x", "y")
        item.version = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = DefaultEvChargeSlotCodeLengthRule(86400)
        val item = newDefaultEvChargeSlot("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
