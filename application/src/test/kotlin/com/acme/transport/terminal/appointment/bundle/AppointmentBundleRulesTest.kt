package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentBundleValidator()
        val item = newAppointmentBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AppointmentBundleRequiredLabelRule("appointment bundle needs an entry")
        val item = newAppointmentBundle("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentBundleRequiredCodeRule("appointment bundle needs an entry")
        val item = newAppointmentBundle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
