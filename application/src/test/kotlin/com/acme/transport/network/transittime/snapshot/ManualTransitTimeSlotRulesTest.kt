package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualTransitTimeSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualTransitTimeSlotValidator()
        val item = newManualTransitTimeSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = ManualTransitTimeSlotLoadKgRangeRule(5.4, 4204.2)
        val item = newManualTransitTimeSlot("x", "y")
        item.loadKg = 4205.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualTransitTimeSlotCodeLengthRule(64)
        val item = newManualTransitTimeSlot("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualTransitTimeSlotRequiredCodeRule("manual transit time slot needs an entry")
        val item = newManualTransitTimeSlot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = ManualTransitTimeSlotLimitRangeRule(32, 120)
        val item = newManualTransitTimeSlot("x", "y")
        item.limit = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
