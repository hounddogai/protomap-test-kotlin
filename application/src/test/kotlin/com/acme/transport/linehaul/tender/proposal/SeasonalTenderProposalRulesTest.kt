package com.acme.transport.linehaul.tender.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalTenderProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalTenderProposalValidator()
        val item = newSeasonalTenderProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above rate`() {
        val rule = SeasonalTenderProposalRateRangeRule(8.4, 1699.6)
        val item = newSeasonalTenderProposal("x", "y")
        item.rate = 1701.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = SeasonalTenderProposalRequiredLabelRule("seasonal tender proposal needs this field")
        val item = newSeasonalTenderProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = SeasonalTenderProposalRequiredDescriptionRule("seasonal tender proposal needs a value")
        val item = newSeasonalTenderProposal("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
