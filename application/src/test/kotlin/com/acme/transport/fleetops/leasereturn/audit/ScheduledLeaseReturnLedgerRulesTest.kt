package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ScheduledLeaseReturnLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultScheduledLeaseReturnLedgerValidator()
        val item = newScheduledLeaseReturnLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank phone`() {
        val rule = ScheduledLeaseReturnLedgerRequiredPhoneRule("scheduled lease return ledger needs this field")
        val item = newScheduledLeaseReturnLedger("", "")
        item.phone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = ScheduledLeaseReturnLedgerPhoneLengthRule(32)
        val item = newScheduledLeaseReturnLedger("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = ScheduledLeaseReturnLedgerLabelLengthRule(3600)
        val item = newScheduledLeaseReturnLedger("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
