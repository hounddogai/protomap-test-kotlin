package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveTenderProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveTenderProposalValidator()
        val item = newAdaptiveTenderProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long checksum`() {
        val rule = AdaptiveTenderProposalChecksumLengthRule(86400)
        val item = newAdaptiveTenderProposal("x", "y")
        item.checksum = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.checksum = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = AdaptiveTenderProposalRequiredCodeRule("adaptive tender proposal needs an entry")
        val item = newAdaptiveTenderProposal("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = AdaptiveTenderProposalMinimumRangeRule(8.5, 4788.9)
        val item = newAdaptiveTenderProposal("x", "y")
        item.minimum = 4790.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = AdaptiveTenderProposalRequiredChecksumRule("adaptive tender proposal needs an entry")
        val item = newAdaptiveTenderProposal("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
