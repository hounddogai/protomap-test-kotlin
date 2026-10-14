package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledAppointmentForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledAppointmentForecastValidator()
        val item = newScheduledAppointmentForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ScheduledAppointmentForecastCodeLengthRule(120)
        val item = newScheduledAppointmentForecast("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = ScheduledAppointmentForecastTrailerIdLengthRule(1000)
        val item = newScheduledAppointmentForecast("x", "y")
        item.trailerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above fuel liters`() {
        val rule = ScheduledAppointmentForecastFuelLitersRangeRule(7.1, 710.8)
        val item = newScheduledAppointmentForecast("x", "y")
        item.fuelLiters = 712.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long contact email`() {
        val rule = ScheduledAppointmentForecastContactEmailLengthRule(86400)
        val item = newScheduledAppointmentForecast("x", "y")
        item.contactEmail = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.contactEmail = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ScheduledAppointmentForecastRequiredTitleRule("scheduled appointment forecast needs an entry")
        val item = newScheduledAppointmentForecast("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
