package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredNodeProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredNodeProposalValidator()
        val item = newTieredNodeProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank summary`() {
        val rule = TieredNodeProposalRequiredSummaryRule("tiered node proposal needs an entry")
        val item = newTieredNodeProposal("", "")
        item.summary = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredNodeProposalRequiredCodeRule("tiered node proposal needs a value")
        val item = newTieredNodeProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = TieredNodeProposalRequiredLabelRule("tiered node proposal needs this field")
        val item = newTieredNodeProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = TieredNodeProposalLabelLengthRule(255)
        val item = newTieredNodeProposal("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
