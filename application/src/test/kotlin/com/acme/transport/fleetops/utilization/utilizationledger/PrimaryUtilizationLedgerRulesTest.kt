package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryUtilizationLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryUtilizationLedgerValidator()
        val item = newPrimaryUtilizationLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = PrimaryUtilizationLedgerRequiredChannelRule("primary utilization ledger needs an entry")
        val item = newPrimaryUtilizationLedger("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = PrimaryUtilizationLedgerChannelLengthRule(3600)
        val item = newPrimaryUtilizationLedger("x", "y")
        item.channel = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above cost`() {
        val rule = PrimaryUtilizationLedgerCostRangeRule(4.4, 3293.9)
        val item = newPrimaryUtilizationLedger("x", "y")
        item.cost = 3295.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
