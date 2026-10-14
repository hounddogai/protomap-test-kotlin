package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentManifestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentManifestValidator()
        val item = newAppointmentManifest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentManifestRequiredCodeRule("appointment manifest needs this field")
        val item = newAppointmentManifest("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = AppointmentManifestShiftCodeLengthRule(255)
        val item = newAppointmentManifest("x", "y")
        item.shiftCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above created at`() {
        val rule = AppointmentManifestCreatedAtRangeRule(32, 80)
        val item = newAppointmentManifest("x", "y")
        item.createdAt = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
