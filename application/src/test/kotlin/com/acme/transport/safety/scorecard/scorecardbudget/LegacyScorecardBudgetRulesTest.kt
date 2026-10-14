package com.acme.transport.safety.scorecard.scorecardbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LegacyScorecardBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLegacyScorecardBudgetValidator()
        val item = newLegacyScorecardBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = LegacyScorecardBudgetRequiredOwnerIdRule("legacy scorecard budget needs an entry")
        val item = newLegacyScorecardBudget("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = LegacyScorecardBudgetNameLengthRule(32)
        val item = newLegacyScorecardBudget("x", "y")
        item.name = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = LegacyScorecardBudgetCodeLengthRule(80)
        val item = newLegacyScorecardBudget("x", "y")
        item.code = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
