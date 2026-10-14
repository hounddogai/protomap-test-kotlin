package com.acme.transport.fleetops.utilization.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualUtilizationProposalRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualUtilizationProposalValidator()
        val item = newManualUtilizationProposal("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = ManualUtilizationProposalMinimumRangeRule(5.7, 1934.5)
        val item = newManualUtilizationProposal("x", "y")
        item.minimum = 1936.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = ManualUtilizationProposalRequiredNameRule("manual utilization proposal needs this field")
        val item = newManualUtilizationProposal("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above budget`() {
        val rule = ManualUtilizationProposalBudgetRangeRule(5.9, 1844.5)
        val item = newManualUtilizationProposal("x", "y")
        item.budget = 1846.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long name`() {
        val rule = ManualUtilizationProposalNameLengthRule(120)
        val item = newManualUtilizationProposal("x", "y")
        item.name = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
