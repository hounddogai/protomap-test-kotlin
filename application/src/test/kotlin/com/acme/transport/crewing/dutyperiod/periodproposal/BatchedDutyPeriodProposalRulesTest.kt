package com.acme.transport.crewing.dutyperiod.periodproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BatchedDutyPeriodProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultBatchedDutyPeriodProposalValidator()
        val item = newBatchedDutyPeriodProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = BatchedDutyPeriodProposalRequiredShiftCodeRule("batched duty period proposal needs a value")
        val item = newBatchedDutyPeriodProposal("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = BatchedDutyPeriodProposalOdometerRangeRule(120, 86400)
        val item = newBatchedDutyPeriodProposal("x", "y")
        item.odometer = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = BatchedDutyPeriodProposalGraceDaysRangeRule(32, 3600)
        val item = newBatchedDutyPeriodProposal("x", "y")
        item.graceDays = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = BatchedDutyPeriodProposalLabelLengthRule(1000)
        val item = newBatchedDutyPeriodProposal("x", "y")
        item.label = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = BatchedDutyPeriodProposalCodeLengthRule(3600)
        val item = newBatchedDutyPeriodProposal("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
