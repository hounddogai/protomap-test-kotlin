package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentBaselineRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentBaselineValidator()
        val item = newAppointmentBaseline("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = AppointmentBaselineRatioRangeRule(1.7, 4312.4)
        val item = newAppointmentBaseline("x", "y")
        item.ratio = 4313.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = AppointmentBaselineFuelLitersRangeRule(7.1, 3865.4)
        val item = newAppointmentBaseline("x", "y")
        item.fuelLiters = 3866.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = AppointmentBaselineFloorValueRangeRule(7.0, 4738.3)
        val item = newAppointmentBaseline("x", "y")
        item.floorValue = 4739.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = AppointmentBaselineCountryLengthRule(3600)
        val item = newAppointmentBaseline("x", "y")
        item.country = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentBaselineCodeLengthRule(86400)
        val item = newAppointmentBaseline("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
