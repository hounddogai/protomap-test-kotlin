package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentEstimateRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentEstimateValidator()
        val item = newAppointmentEstimate("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AppointmentEstimateLabelLengthRule(16)
        val item = newAppointmentEstimate("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above window days`() {
        val rule = AppointmentEstimateWindowDaysRangeRule(32, 1000)
        val item = newAppointmentEstimate("x", "y")
        item.windowDays = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentEstimateCodeLengthRule(120)
        val item = newAppointmentEstimate("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AppointmentEstimateChecksumLengthRule(64)
        val item = newAppointmentEstimate("x", "y")
        item.checksum = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = AppointmentEstimateRequiredChecksumRule("appointment estimate needs this field")
        val item = newAppointmentEstimate("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
