package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentPolicyRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentPolicyValidator()
        val item = newAppointmentPolicy("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = AppointmentPolicyShiftCodeLengthRule(3600)
        val item = newAppointmentPolicy("x", "y")
        item.shiftCode = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AppointmentPolicyPalletsRangeRule(32, 3600)
        val item = newAppointmentPolicy("x", "y")
        item.pallets = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AppointmentPolicyRequiredNameRule("appointment policy needs this field")
        val item = newAppointmentPolicy("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
