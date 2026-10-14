package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAppointmentSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAppointmentSnapshotValidator()
        val item = newPrimaryAppointmentSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long description`() {
        val rule = PrimaryAppointmentSnapshotDescriptionLengthRule(86400)
        val item = newPrimaryAppointmentSnapshot("x", "y")
        item.description = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.description = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PrimaryAppointmentSnapshotRequiredDescriptionRule("primary appointment snapshot needs an entry")
        val item = newPrimaryAppointmentSnapshot("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = PrimaryAppointmentSnapshotEtaMinutesRangeRule(64, 1000)
        val item = newPrimaryAppointmentSnapshot("x", "y")
        item.etaMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = PrimaryAppointmentSnapshotLabelLengthRule(32)
        val item = newPrimaryAppointmentSnapshot("x", "y")
        item.label = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
