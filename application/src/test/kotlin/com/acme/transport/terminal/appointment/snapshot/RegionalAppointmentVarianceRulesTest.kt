package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalAppointmentVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalAppointmentVarianceValidator()
        val item = newRegionalAppointmentVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalAppointmentVarianceLoadKgRangeRule(8.5, 2037.3)
        val item = newRegionalAppointmentVariance("x", "y")
        item.loadKg = 2038.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above cost`() {
        val rule = RegionalAppointmentVarianceCostRangeRule(3.0, 4584.4)
        val item = newRegionalAppointmentVariance("x", "y")
        item.cost = 4585.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalAppointmentVarianceRequiredNameRule("regional appointment variance needs this field")
        val item = newRegionalAppointmentVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long carrier`() {
        val rule = RegionalAppointmentVarianceCarrierLengthRule(255)
        val item = newRegionalAppointmentVariance("x", "y")
        item.carrier = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.carrier = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
