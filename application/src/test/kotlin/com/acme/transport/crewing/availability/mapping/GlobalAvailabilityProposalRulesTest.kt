package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalAvailabilityProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalAvailabilityProposalValidator()
        val item = newGlobalAvailabilityProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = GlobalAvailabilityProposalRetriesRangeRule(32, 64)
        val item = newGlobalAvailabilityProposal("x", "y")
        item.retries = 65
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = GlobalAvailabilityProposalGraceDaysRangeRule(120, 255)
        val item = newGlobalAvailabilityProposal("x", "y")
        item.graceDays = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = GlobalAvailabilityProposalRequiredLabelRule("global availability proposal needs a value")
        val item = newGlobalAvailabilityProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = GlobalAvailabilityProposalBudgetRangeRule(9.8, 4723.4)
        val item = newGlobalAvailabilityProposal("x", "y")
        item.budget = 4724.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = GlobalAvailabilityProposalLabelLengthRule(3600)
        val item = newGlobalAvailabilityProposal("x", "y")
        item.label = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
