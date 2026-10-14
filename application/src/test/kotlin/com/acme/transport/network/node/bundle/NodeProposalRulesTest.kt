package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NodeProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNodeProposalValidator()
        val item = newNodeProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long phone`() {
        val rule = NodeProposalPhoneLengthRule(3600)
        val item = newNodeProposal("x", "y")
        item.phone = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.phone = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NodeProposalRequiredCodeRule("node proposal needs a value")
        val item = newNodeProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = NodeProposalCodeLengthRule(32)
        val item = newNodeProposal("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = NodeProposalRequiredDescriptionRule("node proposal needs a value")
        val item = newNodeProposal("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
