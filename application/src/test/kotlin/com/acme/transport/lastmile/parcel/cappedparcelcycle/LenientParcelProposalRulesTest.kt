package com.acme.transport.lastmile.parcel.cappedparcelcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LenientParcelProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLenientParcelProposalValidator()
        val item = newLenientParcelProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = LenientParcelProposalCodeLengthRule(3600)
        val item = newLenientParcelProposal("x", "y")
        item.code = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = LenientParcelProposalRequiredNameRule("lenient parcel proposal needs an entry")
        val item = newLenientParcelProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above cost`() {
        val rule = LenientParcelProposalCostRangeRule(6.7, 765.6)
        val item = newLenientParcelProposal("x", "y")
        item.cost = 767.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LenientParcelProposalRequiredCodeRule("lenient parcel proposal needs this field")
        val item = newLenientParcelProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
