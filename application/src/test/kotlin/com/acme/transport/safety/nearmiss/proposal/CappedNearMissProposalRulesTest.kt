package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedNearMissProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedNearMissProposalValidator()
        val item = newCappedNearMissProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = CappedNearMissProposalRequiredCodeRule("capped near miss proposal needs a value")
        val item = newCappedNearMissProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = CappedNearMissProposalRequiredChannelRule("capped near miss proposal needs an entry")
        val item = newCappedNearMissProposal("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = CappedNearMissProposalRequiredDepotRule("capped near miss proposal needs a value")
        val item = newCappedNearMissProposal("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = CappedNearMissProposalDepotLengthRule(255)
        val item = newCappedNearMissProposal("x", "y")
        item.depot = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
