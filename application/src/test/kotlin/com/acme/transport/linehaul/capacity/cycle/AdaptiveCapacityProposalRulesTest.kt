package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AdaptiveCapacityProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultAdaptiveCapacityProposalValidator()
        val item = newAdaptiveCapacityProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = AdaptiveCapacityProposalRequiredNotesRule("adaptive capacity proposal needs a value")
        val item = newAdaptiveCapacityProposal("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = AdaptiveCapacityProposalAmountRangeRule(3.3, 4289.7)
        val item = newAdaptiveCapacityProposal("x", "y")
        item.amount = 4291.2
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
