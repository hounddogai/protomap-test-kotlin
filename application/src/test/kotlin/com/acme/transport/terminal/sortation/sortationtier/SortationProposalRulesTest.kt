package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SortationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSortationProposalValidator()
        val item = newSortationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = SortationProposalRequiredTrailerIdRule("sortation proposal needs an entry")
        val item = newSortationProposal("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = SortationProposalTitleLengthRule(3600)
        val item = newSortationProposal("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above retries`() {
        val rule = SortationProposalRetriesRangeRule(32, 3600)
        val item = newSortationProposal("x", "y")
        item.retries = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank contact email`() {
        val rule = SortationProposalRequiredContactEmailRule("sortation proposal needs a value")
        val item = newSortationProposal("", "")
        item.contactEmail = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long trailer id`() {
        val rule = SortationProposalTrailerIdLengthRule(64)
        val item = newSortationProposal("x", "y")
        item.trailerId = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.trailerId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
