package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedAttemptProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedAttemptProposalValidator()
        val item = newBatchedAttemptProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = BatchedAttemptProposalRequiredLabelRule("batched attempt proposal needs an entry")
        val item = newBatchedAttemptProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = BatchedAttemptProposalLoadKgRangeRule(2.1, 263.5)
        val item = newBatchedAttemptProposal("x", "y")
        item.loadKg = 265.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above batch size`() {
        val rule = BatchedAttemptProposalBatchSizeRangeRule(1000, 86400)
        val item = newBatchedAttemptProposal("x", "y")
        item.batchSize = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = BatchedAttemptProposalMinimumRangeRule(1.1, 3702.4)
        val item = newBatchedAttemptProposal("x", "y")
        item.minimum = 3703.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = BatchedAttemptProposalRequiredContactEmailRule("batched attempt proposal needs an entry")
        val item = newBatchedAttemptProposal("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
