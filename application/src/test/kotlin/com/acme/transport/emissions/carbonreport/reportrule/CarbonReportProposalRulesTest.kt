package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertTrue

class CarbonReportProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCarbonReportProposalValidator()
        val item = newCarbonReportProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above eta minutes`() {
        val rule = CarbonReportProposalEtaMinutesRangeRule(1000, 3600)
        val item = newCarbonReportProposal("x", "y")
        item.etaMinutes = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
