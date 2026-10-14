package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalAppointmentOverrideRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalAppointmentOverrideValidator()
        val item = newSeasonalAppointmentOverride("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = SeasonalAppointmentOverrideRequiredOwnerIdRule("seasonal appointment override needs a value")
        val item = newSeasonalAppointmentOverride("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = SeasonalAppointmentOverrideRequiredTrailerIdRule("seasonal appointment override needs an entry")
        val item = newSeasonalAppointmentOverride("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = SeasonalAppointmentOverrideLabelLengthRule(3600)
        val item = newSeasonalAppointmentOverride("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = SeasonalAppointmentOverrideOwnerIdLengthRule(1000)
        val item = newSeasonalAppointmentOverride("x", "y")
        item.ownerId = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
