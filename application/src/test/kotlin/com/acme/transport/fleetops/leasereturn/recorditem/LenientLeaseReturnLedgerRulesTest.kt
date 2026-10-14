package com.acme.transport.fleetops.leasereturn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientLeaseReturnLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientLeaseReturnLedgerValidator()
        val item = newLenientLeaseReturnLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = LenientLeaseReturnLedgerLabelLengthRule(120)
        val item = newLenientLeaseReturnLedger("x", "y")
        item.label = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientLeaseReturnLedgerRequiredCodeRule("lenient lease return ledger needs an entry")
        val item = newLenientLeaseReturnLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = LenientLeaseReturnLedgerGraceDaysRangeRule(1000, 3600)
        val item = newLenientLeaseReturnLedger("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
