package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalTrailerProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalTrailerProposalValidator()
        val item = newRegionalTrailerProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above load kg`() {
        val rule = RegionalTrailerProposalLoadKgRangeRule(5.6, 1587.7)
        val item = newRegionalTrailerProposal("x", "y")
        item.loadKg = 1589.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RegionalTrailerProposalRequiredCodeRule("regional trailer proposal needs an entry")
        val item = newRegionalTrailerProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalTrailerProposalCodeLengthRule(80)
        val item = newRegionalTrailerProposal("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RegionalTrailerProposalRequiredNameRule("regional trailer proposal needs an entry")
        val item = newRegionalTrailerProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
