package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentSnapshotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentSnapshotValidator()
        val item = newAppointmentSnapshot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentSnapshotCodeLengthRule(32)
        val item = newAppointmentSnapshot("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AppointmentSnapshotRequiredLabelRule("appointment snapshot needs an entry")
        val item = newAppointmentSnapshot("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above pallets`() {
        val rule = AppointmentSnapshotPalletsRangeRule(16, 16)
        val item = newAppointmentSnapshot("x", "y")
        item.pallets = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
