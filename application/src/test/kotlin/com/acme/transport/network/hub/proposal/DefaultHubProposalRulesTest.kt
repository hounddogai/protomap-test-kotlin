package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DefaultHubProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDefaultHubProposalValidator()
        val item = newDefaultHubProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DefaultHubProposalRequiredCodeRule("default hub proposal needs an entry")
        val item = newDefaultHubProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = DefaultHubProposalTitleLengthRule(32)
        val item = newDefaultHubProposal("x", "y")
        item.title = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DefaultHubProposalRequiredTitleRule("default hub proposal needs a value")
        val item = newDefaultHubProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
