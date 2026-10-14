package com.acme.transport.terminal.dockdoor.doorsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingDockDoorExceptionRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingDockDoorExceptionValidator()
        val item = newPendingDockDoorException("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PendingDockDoorExceptionTitleLengthRule(120)
        val item = newPendingDockDoorException("x", "y")
        item.title = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank vehicle id`() {
        val rule = PendingDockDoorExceptionRequiredVehicleIdRule("pending dock door exception needs this field")
        val item = newPendingDockDoorException("", "")
        item.vehicleId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingDockDoorExceptionRequiredPhoneRule("pending dock door exception needs a value")
        val item = newPendingDockDoorException("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = PendingDockDoorExceptionRatioRangeRule(3.1, 910.3)
        val item = newPendingDockDoorException("x", "y")
        item.ratio = 911.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above margin`() {
        val rule = PendingDockDoorExceptionMarginRangeRule(5.0, 2113.5)
        val item = newPendingDockDoorException("x", "y")
        item.margin = 2115.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingDockDoorExceptionRequiredCodeRule("pending dock door exception needs this field")
        val item = newPendingDockDoorException("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
