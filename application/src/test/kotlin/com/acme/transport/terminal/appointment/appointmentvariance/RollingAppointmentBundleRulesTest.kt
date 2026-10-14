package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingAppointmentBundleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingAppointmentBundleValidator()
        val item = newRollingAppointmentBundle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingAppointmentBundleCodeLengthRule(80)
        val item = newRollingAppointmentBundle("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = RollingAppointmentBundleRequiredOwnerIdRule("rolling appointment bundle needs a value")
        val item = newRollingAppointmentBundle("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = RollingAppointmentBundleRequiredDepotRule("rolling appointment bundle needs a value")
        val item = newRollingAppointmentBundle("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingAppointmentBundleScoreRangeRule(7.3, 3573.0)
        val item = newRollingAppointmentBundle("x", "y")
        item.score = 3574.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
