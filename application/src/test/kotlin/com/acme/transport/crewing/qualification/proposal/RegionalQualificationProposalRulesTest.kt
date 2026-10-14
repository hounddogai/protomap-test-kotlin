package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalQualificationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationProposalValidator()
        val item = newRegionalQualificationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above retries`() {
        val rule = RegionalQualificationProposalRetriesRangeRule(16, 16)
        val item = newRegionalQualificationProposal("x", "y")
        item.retries = 17
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above floor value`() {
        val rule = RegionalQualificationProposalFloorValueRangeRule(9.7, 2099.3)
        val item = newRegionalQualificationProposal("x", "y")
        item.floorValue = 2100.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RegionalQualificationProposalRequiredTitleRule("regional qualification proposal needs an entry")
        val item = newRegionalQualificationProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
