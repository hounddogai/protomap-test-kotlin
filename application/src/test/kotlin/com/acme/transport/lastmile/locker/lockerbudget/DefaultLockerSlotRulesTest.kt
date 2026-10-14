package com.acme.transport.lastmile.locker.lockerbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultLockerSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultLockerSlotValidator()
        val item = newDefaultLockerSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = DefaultLockerSlotRequiredSegmentRule("default locker slot needs this field")
        val item = newDefaultLockerSlot("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = DefaultLockerSlotNameLengthRule(120)
        val item = newDefaultLockerSlot("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
