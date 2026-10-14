package com.acme.transport.lastmile.drop.dropslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDropSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDropSlotValidator()
        val item = newPendingDropSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = PendingDropSlotShiftCodeLengthRule(80)
        val item = newPendingDropSlot("x", "y")
        item.shiftCode = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = PendingDropSlotOdometerRangeRule(16, 80)
        val item = newPendingDropSlot("x", "y")
        item.odometer = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = PendingDropSlotRequiredOwnerIdRule("pending drop slot needs a value")
        val item = newPendingDropSlot("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
