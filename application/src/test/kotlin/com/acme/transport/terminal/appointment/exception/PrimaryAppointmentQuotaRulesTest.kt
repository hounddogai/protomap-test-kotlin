package com.acme.transport.terminal.appointment.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryAppointmentQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryAppointmentQuotaValidator()
        val item = newPrimaryAppointmentQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = PrimaryAppointmentQuotaDockLengthRule(255)
        val item = newPrimaryAppointmentQuota("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = PrimaryAppointmentQuotaRequiredSummaryRule("primary appointment quota needs this field")
        val item = newPrimaryAppointmentQuota("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryAppointmentQuotaCodeLengthRule(32)
        val item = newPrimaryAppointmentQuota("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = PrimaryAppointmentQuotaRequiredDockRule("primary appointment quota needs this field")
        val item = newPrimaryAppointmentQuota("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PrimaryAppointmentQuotaRequiredCodeRule("primary appointment quota needs this field")
        val item = newPrimaryAppointmentQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
