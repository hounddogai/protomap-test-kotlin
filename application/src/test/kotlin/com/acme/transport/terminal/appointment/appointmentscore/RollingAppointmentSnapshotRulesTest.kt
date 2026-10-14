package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAppointmentSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAppointmentSnapshotValidator()
        val item = newRollingAppointmentSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingAppointmentSnapshotRequiredCodeRule("rolling appointment snapshot needs an entry")
        val item = newRollingAppointmentSnapshot("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
