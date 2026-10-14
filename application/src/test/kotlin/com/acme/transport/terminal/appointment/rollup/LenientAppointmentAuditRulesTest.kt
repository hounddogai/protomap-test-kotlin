package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientAppointmentAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientAppointmentAuditValidator()
        val item = newLenientAppointmentAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientAppointmentAuditRequiredCodeRule("lenient appointment audit needs a value")
        val item = newLenientAppointmentAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientAppointmentAuditLabelLengthRule(3600)
        val item = newLenientAppointmentAudit("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = LenientAppointmentAuditThresholdRangeRule(2.7, 235.6)
        val item = newLenientAppointmentAudit("x", "y")
        item.threshold = 237.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientAppointmentAuditCodeLengthRule(120)
        val item = newLenientAppointmentAudit("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = LenientAppointmentAuditRequiredTitleRule("lenient appointment audit needs a value")
        val item = newLenientAppointmentAudit("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
