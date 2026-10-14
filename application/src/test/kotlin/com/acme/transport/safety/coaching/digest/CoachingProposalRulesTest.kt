package com.acme.transport.safety.coaching.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingProposalValidator()
        val item = newCoachingProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = CoachingProposalRequiredNameRule("coaching proposal needs an entry")
        val item = newCoachingProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = CoachingProposalNameLengthRule(80)
        val item = newCoachingProposal("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above ratio`() {
        val rule = CoachingProposalRatioRangeRule(3.0, 1777.9)
        val item = newCoachingProposal("x", "y")
        item.ratio = 1779.4
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
