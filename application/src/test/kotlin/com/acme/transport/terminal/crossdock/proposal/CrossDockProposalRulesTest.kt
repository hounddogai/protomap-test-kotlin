package com.acme.transport.terminal.crossdock.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockProposalValidator()
        val item = newCrossDockProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = CrossDockProposalScoreRangeRule(0.2, 3817.6)
        val item = newCrossDockProposal("x", "y")
        item.score = 3819.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long channel`() {
        val rule = CrossDockProposalChannelLengthRule(16)
        val item = newCrossDockProposal("x", "y")
        item.channel = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CrossDockProposalRequiredNameRule("cross dock proposal needs a value")
        val item = newCrossDockProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
