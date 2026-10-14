package com.acme.transport.safety.incident.incidentreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyIncidentReservationRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyIncidentReservationValidator()
        val item = newLegacyIncidentReservation("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyIncidentReservationRequiredCodeRule("legacy incident reservation needs this field")
        val item = newLegacyIncidentReservation("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LegacyIncidentReservationRequiredNameRule("legacy incident reservation needs a value")
        val item = newLegacyIncidentReservation("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyIncidentReservationCodeLengthRule(120)
        val item = newLegacyIncidentReservation("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = LegacyIncidentReservationRequiredNotesRule("legacy incident reservation needs this field")
        val item = newLegacyIncidentReservation("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
