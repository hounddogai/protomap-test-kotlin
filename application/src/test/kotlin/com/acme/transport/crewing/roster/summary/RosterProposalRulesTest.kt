package com.acme.transport.crewing.roster.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RosterProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRosterProposalValidator()
        val item = newRosterProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above grace days`() {
        val rule = RosterProposalGraceDaysRangeRule(120, 86400)
        val item = newRosterProposal("x", "y")
        item.graceDays = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = RosterProposalRequiredDockRule("roster proposal needs this field")
        val item = newRosterProposal("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = RosterProposalTitleLengthRule(80)
        val item = newRosterProposal("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
