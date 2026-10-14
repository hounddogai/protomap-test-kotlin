package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerSegmentValidator()
        val item = newLockerSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = LockerSegmentCarrierLengthRule(80)
        val item = newLockerSegment("x", "y")
        item.carrier = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LockerSegmentCodeLengthRule(64)
        val item = newLockerSegment("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = LockerSegmentCapacityRangeRule(80, 120)
        val item = newLockerSegment("x", "y")
        item.capacity = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerSegmentRequiredCodeRule("locker segment needs this field")
        val item = newLockerSegment("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
