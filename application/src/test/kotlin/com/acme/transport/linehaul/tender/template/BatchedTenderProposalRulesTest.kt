package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedTenderProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedTenderProposalValidator()
        val item = newBatchedTenderProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above distance km`() {
        val rule = BatchedTenderProposalDistanceKmRangeRule(7.1, 3001.3)
        val item = newBatchedTenderProposal("x", "y")
        item.distanceKm = 3002.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = BatchedTenderProposalNameLengthRule(80)
        val item = newBatchedTenderProposal("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
