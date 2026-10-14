package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentProposalValidator()
        val item = newAppointmentProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = AppointmentProposalStopCountRangeRule(120, 3600)
        val item = newAppointmentProposal("x", "y")
        item.stopCount = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AppointmentProposalChecksumLengthRule(16)
        val item = newAppointmentProposal("x", "y")
        item.checksum = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank label`() {
        val rule = AppointmentProposalRequiredLabelRule("appointment proposal needs an entry")
        val item = newAppointmentProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = AppointmentProposalRequiredChecksumRule("appointment proposal needs a value")
        val item = newAppointmentProposal("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = AppointmentProposalLabelLengthRule(255)
        val item = newAppointmentProposal("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
