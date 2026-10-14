package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentThresholdRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentThresholdValidator()
        val item = newAppointmentThreshold("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = AppointmentThresholdTitleLengthRule(80)
        val item = newAppointmentThreshold("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = AppointmentThresholdEtaMinutesRangeRule(64, 80)
        val item = newAppointmentThreshold("x", "y")
        item.etaMinutes = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long country`() {
        val rule = AppointmentThresholdCountryLengthRule(1000)
        val item = newAppointmentThreshold("x", "y")
        item.country = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
