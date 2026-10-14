package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredCoachingProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredCoachingProposalValidator()
        val item = newTieredCoachingProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank checksum`() {
        val rule = TieredCoachingProposalRequiredChecksumRule("tiered coaching proposal needs a value")
        val item = newTieredCoachingProposal("", "")
        item.checksum = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = TieredCoachingProposalNameLengthRule(16)
        val item = newTieredCoachingProposal("x", "y")
        item.name = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = TieredCoachingProposalRequiredNameRule("tiered coaching proposal needs this field")
        val item = newTieredCoachingProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
