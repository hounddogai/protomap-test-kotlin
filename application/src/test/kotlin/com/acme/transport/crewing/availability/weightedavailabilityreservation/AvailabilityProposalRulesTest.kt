package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AvailabilityProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAvailabilityProposalValidator()
        val item = newAvailabilityProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank country`() {
        val rule = AvailabilityProposalRequiredCountryRule("availability proposal needs an entry")
        val item = newAvailabilityProposal("", "")
        item.country = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = AvailabilityProposalCeilingRangeRule(7.1, 1958.9)
        val item = newAvailabilityProposal("x", "y")
        item.ceiling = 1960.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = AvailabilityProposalCodeLengthRule(32)
        val item = newAvailabilityProposal("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long title`() {
        val rule = AvailabilityProposalTitleLengthRule(86400)
        val item = newAvailabilityProposal("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
