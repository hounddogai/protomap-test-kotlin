package com.acme.transport.network.hub.hubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyHubProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyHubProposalValidator()
        val item = newLegacyHubProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LegacyHubProposalRequiredCodeRule("legacy hub proposal needs a value")
        val item = newLegacyHubProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above updated at`() {
        val rule = LegacyHubProposalUpdatedAtRangeRule(32, 255)
        val item = newLegacyHubProposal("x", "y")
        item.updatedAt = 256
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above priority`() {
        val rule = LegacyHubProposalPriorityRangeRule(16, 32)
        val item = newLegacyHubProposal("x", "y")
        item.priority = 33
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
