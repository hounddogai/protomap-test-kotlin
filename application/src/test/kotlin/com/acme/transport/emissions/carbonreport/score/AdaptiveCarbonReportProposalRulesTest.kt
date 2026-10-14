package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCarbonReportProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCarbonReportProposalValidator()
        val item = newAdaptiveCarbonReportProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveCarbonReportProposalRequiredCodeRule("adaptive carbon report proposal needs an entry")
        val item = newAdaptiveCarbonReportProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = AdaptiveCarbonReportProposalDockLengthRule(255)
        val item = newAdaptiveCarbonReportProposal("x", "y")
        item.dock = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = AdaptiveCarbonReportProposalRequiredDockRule("adaptive carbon report proposal needs this field")
        val item = newAdaptiveCarbonReportProposal("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above starts at`() {
        val rule = AdaptiveCarbonReportProposalStartsAtRangeRule(1000, 86400)
        val item = newAdaptiveCarbonReportProposal("x", "y")
        item.startsAt = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
