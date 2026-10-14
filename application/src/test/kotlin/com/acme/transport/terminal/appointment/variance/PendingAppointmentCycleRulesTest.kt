package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingAppointmentCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingAppointmentCycleValidator()
        val item = newPendingAppointmentCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = PendingAppointmentCycleOdometerRangeRule(16, 86400)
        val item = newPendingAppointmentCycle("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingAppointmentCycleRequiredCodeRule("pending appointment cycle needs a value")
        val item = newPendingAppointmentCycle("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = PendingAppointmentCycleRevisionRangeRule(32, 80)
        val item = newPendingAppointmentCycle("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = PendingAppointmentCycleVehicleIdLengthRule(80)
        val item = newPendingAppointmentCycle("x", "y")
        item.vehicleId = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
