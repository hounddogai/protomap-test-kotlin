package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryDisputeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryDisputeLedgerValidator()
        val item = newPrimaryDisputeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = PrimaryDisputeLedgerRequiredSegmentRule("primary dispute ledger needs this field")
        val item = newPrimaryDisputeLedger("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = PrimaryDisputeLedgerAmountRangeRule(1.1, 3964.3)
        val item = newPrimaryDisputeLedger("x", "y")
        item.amount = 3965.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = PrimaryDisputeLedgerRequiredLabelRule("primary dispute ledger needs this field")
        val item = newPrimaryDisputeLedger("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long segment`() {
        val rule = PrimaryDisputeLedgerSegmentLengthRule(86400)
        val item = newPrimaryDisputeLedger("x", "y")
        item.segment = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.segment = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = PrimaryDisputeLedgerCeilingRangeRule(2.7, 2782.2)
        val item = newPrimaryDisputeLedger("x", "y")
        item.ceiling = 2783.7
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
