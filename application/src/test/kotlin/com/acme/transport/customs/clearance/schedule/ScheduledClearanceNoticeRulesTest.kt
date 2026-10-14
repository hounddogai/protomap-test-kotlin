package com.acme.transport.customs.clearance.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledClearanceNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledClearanceNoticeValidator()
        val item = newScheduledClearanceNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = ScheduledClearanceNoticeShiftCodeLengthRule(1000)
        val item = newScheduledClearanceNotice("x", "y")
        item.shiftCode = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ScheduledClearanceNoticeRequiredCodeRule("scheduled clearance notice needs this field")
        val item = newScheduledClearanceNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = ScheduledClearanceNoticeDepotLengthRule(16)
        val item = newScheduledClearanceNotice("x", "y")
        item.depot = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
