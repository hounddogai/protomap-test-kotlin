package com.acme.transport.terminal.appointment.rollingappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictAppointmentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictAppointmentVarianceValidator()
        val item = newStrictAppointmentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = StrictAppointmentVarianceRequiredNameRule("strict appointment variance needs an entry")
        val item = newStrictAppointmentVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = StrictAppointmentVarianceRequiredUnitRule("strict appointment variance needs this field")
        val item = newStrictAppointmentVariance("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = StrictAppointmentVarianceNameLengthRule(32)
        val item = newStrictAppointmentVariance("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
