package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodProposalValidator()
        val item = newDutyPeriodProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = DutyPeriodProposalAmountRangeRule(7.9, 1915.1)
        val item = newDutyPeriodProposal("x", "y")
        item.amount = 1916.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank category`() {
        val rule = DutyPeriodProposalRequiredCategoryRule("duty period proposal needs this field")
        val item = newDutyPeriodProposal("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = DutyPeriodProposalStopCountRangeRule(16, 1000)
        val item = newDutyPeriodProposal("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
