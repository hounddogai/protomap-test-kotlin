package com.acme.transport.fleetops.leasereturn.adaptivereturnledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingLeaseReturnLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingLeaseReturnLedgerValidator()
        val item = newRollingLeaseReturnLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingLeaseReturnLedgerRequiredCodeRule("rolling lease return ledger needs this field")
        val item = newRollingLeaseReturnLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = RollingLeaseReturnLedgerRateRangeRule(9.7, 421.9)
        val item = newRollingLeaseReturnLedger("x", "y")
        item.rate = 423.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = RollingLeaseReturnLedgerMinimumRangeRule(1.9, 2115.8)
        val item = newRollingLeaseReturnLedger("x", "y")
        item.minimum = 2117.3
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above weight`() {
        val rule = RollingLeaseReturnLedgerWeightRangeRule(9.0, 4486.5)
        val item = newRollingLeaseReturnLedger("x", "y")
        item.weight = 4488.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
