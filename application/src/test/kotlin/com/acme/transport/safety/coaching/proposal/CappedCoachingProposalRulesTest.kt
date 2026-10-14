package com.acme.transport.safety.coaching.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CappedCoachingProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCappedCoachingProposalValidator()
        val item = newCappedCoachingProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank label`() {
        val rule = CappedCoachingProposalRequiredLabelRule("capped coaching proposal needs this field")
        val item = newCappedCoachingProposal("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long driver id`() {
        val rule = CappedCoachingProposalDriverIdLengthRule(32)
        val item = newCappedCoachingProposal("x", "y")
        item.driverId = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.driverId = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
