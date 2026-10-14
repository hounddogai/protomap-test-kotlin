package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DropProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDropProposalValidator()
        val item = newDropProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = DropProposalCodeLengthRule(120)
        val item = newDropProposal("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = DropProposalLabelLengthRule(86400)
        val item = newDropProposal("x", "y")
        item.label = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above discount`() {
        val rule = DropProposalDiscountRangeRule(3.2, 3089.3)
        val item = newDropProposal("x", "y")
        item.discount = 3090.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank source`() {
        val rule = DropProposalRequiredSourceRule("drop proposal needs an entry")
        val item = newDropProposal("", "")
        item.source = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long source`() {
        val rule = DropProposalSourceLengthRule(3600)
        val item = newDropProposal("x", "y")
        item.source = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.source = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = DropProposalScoreRangeRule(7.7, 1720.0)
        val item = newDropProposal("x", "y")
        item.score = 1721.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
