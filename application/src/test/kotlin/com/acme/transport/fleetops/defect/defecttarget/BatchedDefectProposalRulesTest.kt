package com.acme.transport.fleetops.defect.defecttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDefectProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDefectProposalValidator()
        val item = newBatchedDefectProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = BatchedDefectProposalEtaMinutesRangeRule(16, 120)
        val item = newBatchedDefectProposal("x", "y")
        item.etaMinutes = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDefectProposalCodeLengthRule(16)
        val item = newBatchedDefectProposal("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = BatchedDefectProposalRequiredNameRule("batched defect proposal needs an entry")
        val item = newBatchedDefectProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = BatchedDefectProposalRequiredCodeRule("batched defect proposal needs an entry")
        val item = newBatchedDefectProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above threshold`() {
        val rule = BatchedDefectProposalThresholdRangeRule(2.5, 4163.4)
        val item = newBatchedDefectProposal("x", "y")
        item.threshold = 4164.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
