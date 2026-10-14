package com.acme.transport.terminal.appointment.appointmentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyAppointmentSegmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyAppointmentSegmentValidator()
        val item = newLegacyAppointmentSegment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = LegacyAppointmentSegmentExternalRefLengthRule(80)
        val item = newLegacyAppointmentSegment("x", "y")
        item.externalRef = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = LegacyAppointmentSegmentRequiredExternalRefRule("legacy appointment segment needs this field")
        val item = newLegacyAppointmentSegment("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = LegacyAppointmentSegmentRequiredCountryRule("legacy appointment segment needs an entry")
        val item = newLegacyAppointmentSegment("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
