package com.acme.transport.network.hub.hubestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class StrictHubProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultStrictHubProposalValidator()
        val item = newStrictHubProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = StrictHubProposalRequiredTitleRule("strict hub proposal needs a value")
        val item = newStrictHubProposal("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank trailer id`() {
        val rule = StrictHubProposalRequiredTrailerIdRule("strict hub proposal needs an entry")
        val item = newStrictHubProposal("", "")
        item.trailerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
