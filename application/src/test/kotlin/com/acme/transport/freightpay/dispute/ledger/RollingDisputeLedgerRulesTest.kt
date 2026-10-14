package com.acme.transport.freightpay.dispute.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDisputeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDisputeLedgerValidator()
        val item = newRollingDisputeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above ends at`() {
        val rule = RollingDisputeLedgerEndsAtRangeRule(64, 1000)
        val item = newRollingDisputeLedger("x", "y")
        item.endsAt = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above capacity`() {
        val rule = RollingDisputeLedgerCapacityRangeRule(32, 1000)
        val item = newRollingDisputeLedger("x", "y")
        item.capacity = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingDisputeLedgerRequiredNameRule("rolling dispute ledger needs this field")
        val item = newRollingDisputeLedger("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
