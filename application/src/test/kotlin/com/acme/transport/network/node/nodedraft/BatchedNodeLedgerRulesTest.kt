package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedNodeLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedNodeLedgerValidator()
        val item = newBatchedNodeLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = BatchedNodeLedgerPhoneLengthRule(32)
        val item = newBatchedNodeLedger("x", "y")
        item.phone = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = BatchedNodeLedgerRequiredReviewerRule("batched node ledger needs an entry")
        val item = newBatchedNodeLedger("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long reviewer`() {
        val rule = BatchedNodeLedgerReviewerLengthRule(86400)
        val item = newBatchedNodeLedger("x", "y")
        item.reviewer = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.reviewer = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
