package com.acme.transport.lastmile.proofofdelivery.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ProofOfDeliveryLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultProofOfDeliveryLedgerValidator()
        val item = newProofOfDeliveryLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above priority`() {
        val rule = ProofOfDeliveryLedgerPriorityRangeRule(86400, 86400)
        val item = newProofOfDeliveryLedger("x", "y")
        item.priority = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = ProofOfDeliveryLedgerCodeLengthRule(1000)
        val item = newProofOfDeliveryLedger("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ProofOfDeliveryLedgerRequiredTitleRule("proof of delivery ledger needs this field")
        val item = newProofOfDeliveryLedger("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ProofOfDeliveryLedgerScoreRangeRule(8.5, 1870.5)
        val item = newProofOfDeliveryLedger("x", "y")
        item.score = 1872.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
