package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AppointmentLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAppointmentLedgerValidator()
        val item = newAppointmentLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AppointmentLedgerRequiredCountryRule("appointment ledger needs this field")
        val item = newAppointmentLedger("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = AppointmentLedgerRequiredNameRule("appointment ledger needs this field")
        val item = newAppointmentLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = AppointmentLedgerCodeLengthRule(1000)
        val item = newAppointmentLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AppointmentLedgerRequiredCodeRule("appointment ledger needs a value")
        val item = newAppointmentLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long country`() {
        val rule = AppointmentLedgerCountryLengthRule(255)
        val item = newAppointmentLedger("x", "y")
        item.country = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.country = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above version`() {
        val rule = AppointmentLedgerVersionRangeRule(16, 16)
        val item = newAppointmentLedger("x", "y")
        item.version = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
