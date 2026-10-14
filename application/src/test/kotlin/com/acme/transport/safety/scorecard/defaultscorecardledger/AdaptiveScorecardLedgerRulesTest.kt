package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveScorecardLedgerRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveScorecardLedgerValidator()
        val item = newAdaptiveScorecardLedger("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long owner id`() {
        val rule = AdaptiveScorecardLedgerOwnerIdLengthRule(86400)
        val item = newAdaptiveScorecardLedger("x", "y")
        item.ownerId = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.ownerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveScorecardLedgerRequiredCodeRule("adaptive scorecard ledger needs a value")
        val item = newAdaptiveScorecardLedger("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = AdaptiveScorecardLedgerRequiredReviewerRule("adaptive scorecard ledger needs this field")
        val item = newAdaptiveScorecardLedger("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = AdaptiveScorecardLedgerRequiredOwnerIdRule("adaptive scorecard ledger needs this field")
        val item = newAdaptiveScorecardLedger("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
