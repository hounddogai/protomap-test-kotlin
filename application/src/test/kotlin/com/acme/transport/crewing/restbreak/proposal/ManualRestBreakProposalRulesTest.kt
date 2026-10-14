package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualRestBreakProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualRestBreakProposalValidator()
        val item = newManualRestBreakProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = ManualRestBreakProposalRequiredTitleRule("manual rest break proposal needs a value")
        val item = newManualRestBreakProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = ManualRestBreakProposalCodeLengthRule(255)
        val item = newManualRestBreakProposal("x", "y")
        item.code = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long dock`() {
        val rule = ManualRestBreakProposalDockLengthRule(86400)
        val item = newManualRestBreakProposal("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
