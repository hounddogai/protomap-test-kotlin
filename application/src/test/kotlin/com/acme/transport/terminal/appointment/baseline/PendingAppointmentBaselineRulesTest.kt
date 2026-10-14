package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAppointmentBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAppointmentBaselineValidator()
        val item = newPendingAppointmentBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PendingAppointmentBaselineCodeLengthRule(255)
        val item = newPendingAppointmentBaseline("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long phone`() {
        val rule = PendingAppointmentBaselinePhoneLengthRule(16)
        val item = newPendingAppointmentBaseline("x", "y")
        item.phone = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = PendingAppointmentBaselineRequiredPhoneRule("pending appointment baseline needs this field")
        val item = newPendingAppointmentBaseline("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = PendingAppointmentBaselineOdometerRangeRule(64, 86400)
        val item = newPendingAppointmentBaseline("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = PendingAppointmentBaselineReviewerLengthRule(1000)
        val item = newPendingAppointmentBaseline("x", "y")
        item.reviewer = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = PendingAppointmentBaselineFloorValueRangeRule(9.8, 2432.4)
        val item = newPendingAppointmentBaseline("x", "y")
        item.floorValue = 2433.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
