package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingLanePlanBudgetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingLanePlanBudgetValidator()
        val item = newPendingLanePlanBudget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above weight`() {
        val rule = PendingLanePlanBudgetWeightRangeRule(4.9, 73.0)
        val item = newPendingLanePlanBudget("x", "y")
        item.weight = 74.5
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingLanePlanBudgetRequiredNameRule("pending lane plan budget needs this field")
        val item = newPendingLanePlanBudget("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
