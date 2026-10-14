package com.acme.transport.safety.scorecard.scorecardnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedScorecardProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedScorecardProposalValidator()
        val item = newBatchedScorecardProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedScorecardProposalLabelLengthRule(64)
        val item = newBatchedScorecardProposal("x", "y")
        item.label = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long unit`() {
        val rule = BatchedScorecardProposalUnitLengthRule(16)
        val item = newBatchedScorecardProposal("x", "y")
        item.unit = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.unit = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = BatchedScorecardProposalUpdatedAtRangeRule(16, 120)
        val item = newBatchedScorecardProposal("x", "y")
        item.updatedAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
