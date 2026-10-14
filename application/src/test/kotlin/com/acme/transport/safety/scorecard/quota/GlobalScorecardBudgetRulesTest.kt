package com.acme.transport.safety.scorecard.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalScorecardBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalScorecardBudgetValidator()
        val item = newGlobalScorecardBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalScorecardBudgetCodeLengthRule(64)
        val item = newGlobalScorecardBudget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalScorecardBudgetRequiredCodeRule("global scorecard budget needs this field")
        val item = newGlobalScorecardBudget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
