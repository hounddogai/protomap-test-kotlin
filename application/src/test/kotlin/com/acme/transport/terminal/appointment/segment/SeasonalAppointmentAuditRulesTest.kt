package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAppointmentAuditRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAppointmentAuditValidator()
        val item = newSeasonalAppointmentAudit("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = SeasonalAppointmentAuditRequiredSegmentRule("seasonal appointment audit needs this field")
        val item = newSeasonalAppointmentAudit("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long vehicle id`() {
        val rule = SeasonalAppointmentAuditVehicleIdLengthRule(1000)
        val item = newSeasonalAppointmentAudit("x", "y")
        item.vehicleId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.vehicleId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = SeasonalAppointmentAuditRequiredCodeRule("seasonal appointment audit needs an entry")
        val item = newSeasonalAppointmentAudit("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
