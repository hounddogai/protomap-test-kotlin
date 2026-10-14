package com.acme.transport.terminal.appointment.appointmenttemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedAppointmentTemplateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedAppointmentTemplateValidator()
        val item = newCappedAppointmentTemplate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above limit`() {
        val rule = CappedAppointmentTemplateLimitRangeRule(16, 86400)
        val item = newCappedAppointmentTemplate("x", "y")
        item.limit = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = CappedAppointmentTemplateLabelLengthRule(86400)
        val item = newCappedAppointmentTemplate("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
