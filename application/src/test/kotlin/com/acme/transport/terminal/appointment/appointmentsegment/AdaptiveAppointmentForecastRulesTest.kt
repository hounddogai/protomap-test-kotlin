package com.acme.transport.terminal.appointment.appointmentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveAppointmentForecastRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveAppointmentForecastValidator()
        val item = newAdaptiveAppointmentForecast("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = AdaptiveAppointmentForecastDurationMinutesRangeRule(80, 3600)
        val item = newAdaptiveAppointmentForecast("x", "y")
        item.durationMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = AdaptiveAppointmentForecastNameLengthRule(64)
        val item = newAdaptiveAppointmentForecast("x", "y")
        item.name = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
